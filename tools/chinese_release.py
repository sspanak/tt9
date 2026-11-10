#!/usr/bin/env python3
"""Prepare verified, immutable Chinese release assets using only the standard library."""
import argparse
import gzip
import hashlib
import io
import json
import re
import shutil
import subprocess
import tempfile
import zipfile
from pathlib import Path

FIELDS = {"schema", "packageName", "versionCode", "variant", "certificateSha256",
          "baseSha256", "featureSha256", "uiSha256"}
HEX = re.compile(r"[0-9a-f]{64}")


def sha256(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for block in iter(lambda: stream.read(65536), b""):
            digest.update(block)
    return digest.hexdigest()


def delivery_properties(data):
    result = {}
    for line in data.decode("utf-8").splitlines():
        if not line or line.startswith("#"):
            continue
        key, separator, value = line.partition("=")
        if not separator or key in result:
            raise ValueError("Malformed or duplicate delivery property")
        result[key] = value
    if result.keys() != FIELDS or result["schema"] != "1":
        raise ValueError("Unsupported/incomplete delivery metadata")
    if (not re.fullmatch(r"[A-Za-z0-9_.]+", result["packageName"])
            or not result["versionCode"].isdigit() or int(result["versionCode"]) <= 0):
        raise ValueError("Invalid package/version")
    if not re.fullmatch(r"(?:full|lite|premium)(?:Debug|Release)", result["variant"]):
        raise ValueError("Invalid variant")
    if any(not HEX.fullmatch(result[key]) for key in FIELDS if key.endswith("Sha256")):
        raise ValueError("Invalid SHA-256 metadata")
    return result


def asset_name(delivery):
    flavor, build_type = re.fullmatch(r"(full|lite|premium)(Debug|Release)", delivery["variant"]).groups()
    return (f"tt9-chinese-{delivery['packageName']}-{delivery['versionCode']}-"
            f"{flavor}-{build_type.lower()}-{delivery['certificateSha256']}.zip")


def verified_variant(repo, variant):
    flavor, build_type = re.fullmatch(r"(full|lite|premium)(Debug|Release)", variant).groups()
    base_directory = repo / "app/build/outputs/apk" / flavor / build_type.lower()
    metadata = json.loads((base_directory / "output-metadata.json").read_text(encoding="utf-8"))
    if len(metadata["elements"]) != 1 or metadata["variantName"] != variant:
        raise ValueError("Unexpected base output metadata")
    element = metadata["elements"][0]
    base = base_directory / element["outputFile"]
    if base.resolve().parent != base_directory.resolve() or not base.is_file():
        raise ValueError("Missing or unsafe base output")
    directory = repo / "app/build/outputs/chinese-packs" / variant
    stable = directory / "zh-simplified-pinyin.zip"
    with zipfile.ZipFile(stable) as archive:
        names = archive.namelist()
        if not names or len(set(names)) != len(names) or names[0] != "zh-simplified-pinyin.txt":
            raise ValueError("Invalid dictionary entry order or duplicate ZIP entries")
        delivery = delivery_properties(archive.read("chinese/delivery.properties"))
        if (delivery["variant"] != variant or delivery["packageName"] != metadata["applicationId"]
                or delivery["versionCode"] != str(element["versionCode"])):
            raise ValueError("Delivery/base metadata mismatch")
        if delivery["baseSha256"] != sha256(base):
            raise ValueError("Delivery base hash mismatch")
        for entry, key in (("chinese/chinese.apk", "featureSha256"), ("chinese/chinese-ui.apk", "uiSha256")):
            if hashlib.sha256(archive.read(entry)).hexdigest() != delivery[key]:
                raise ValueError(f"Delivery payload hash mismatch: {entry}")
        notices = {name: archive.read(name) for name in names
                   if name.startswith("licenses/") or name == "chinese/UNICODE-LICENSE.txt"}
        if not {"licenses/rime-ice-license.txt", "licenses/rime-ice-notice.txt",
                "licenses/chinese-domains-notice.txt", "licenses/thuocl-license.txt",
                "chinese/UNICODE-LICENSE.txt"}.issubset(notices):
            raise ValueError("Missing source/license notices")
        if archive.testzip() is not None:
            raise ValueError("ZIP checksum failed")
    named = directory / asset_name(delivery)
    if not named.is_file() or sha256(named) != sha256(stable):
        raise ValueError("Missing or stale deterministic release copy")
    return base, named, delivery, notices


def immutable_copy(source, destination):
    if destination.exists():
        if sha256(source) != sha256(destination):
            raise ValueError(f"Refusing to overwrite a different asset: {destination.name}")
    else:
        shutil.copyfile(source, destination)


def immutable_bytes(data, destination):
    if destination.exists() and destination.read_bytes() != data:
        raise ValueError(f"Refusing to overwrite a different asset: {destination.name}")
    destination.write_bytes(data)


def notice_zip(notices):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name, content in sorted(notices.items()):
            entry = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(entry, content)
    return output.getvalue()


def verify_inventory(output):
    """Require a closed set of regular assets; the checksum file never lists itself."""
    checksum = output / "SHA256SUMS"
    if checksum.is_symlink() or not checksum.is_file():
        raise ValueError("Missing regular SHA256SUMS file")
    entries = {}
    for line in checksum.read_text(encoding="ascii").splitlines():
        match = re.fullmatch(r"([0-9a-f]{64})  ([A-Za-z0-9][A-Za-z0-9._-]*)", line)
        if not match:
            raise ValueError("Malformed checksum inventory")
        digest, name = match.groups()
        if name == "SHA256SUMS" or name in entries:
            raise ValueError("Checksum inventory contains self-listing or duplicate assets")
        entries[name] = digest
    if not entries:
        raise ValueError("Empty checksum inventory")
    files = list(output.iterdir())
    if any(path.is_symlink() or not path.is_file() for path in files):
        raise ValueError("Release inventory contains a non-regular file")
    if {path.name for path in files} != entries.keys() | {"SHA256SUMS"}:
        raise ValueError("Unexpected or missing files in release inventory")
    for name, digest in entries.items():
        if sha256(output / name) != digest:
            raise ValueError(f"Release inventory hash mismatch: {name}")
    return sorted(entries)


def prepare(repo, output, tag, variants, include_source=True):
    if not re.fullmatch(r"v[0-9][A-Za-z0-9._-]*", tag):
        raise ValueError("Release tag must be a safe existing v-prefixed version tag")
    output.mkdir(parents=True, exist_ok=True)
    records, notices, assets = [], {"LICENSE.txt": (repo / "LICENSE.txt").read_bytes()}, []
    identity = None
    for variant in variants:
        base, pack, delivery, variant_notices = verified_variant(repo, variant)
        current = tuple(delivery[key] for key in ("packageName", "versionCode", "certificateSha256"))
        if identity is not None and identity != current:
            raise ValueError("Release variants must use one package/version/signing key")
        identity = current
        for name, content in variant_notices.items():
            if name in notices and notices[name] != content:
                raise ValueError("Conflicting variant notices")
            notices[name] = content
        for asset in (base, pack):
            immutable_copy(asset, output / asset.name)
            assets.append(asset.name)
        records.append(dict(delivery, baseAsset=base.name, packAsset=pack.name,
                            packSha256=sha256(pack), packBytes=pack.stat().st_size))
    commit = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=repo, text=True).strip()
    if include_source:
        tagged = subprocess.check_output(["git", "rev-parse", "--verify", f"refs/tags/{tag}^{{commit}}"],
                                         cwd=repo, text=True).strip()
        if tagged != commit:
            raise ValueError("Release tag does not point to this source commit")
        source = output / f"tt9-source-{tag}.tar.gz"
        with tempfile.TemporaryDirectory(dir=output.parent) as temporary:
            staged = Path(temporary) / "source.tar.gz"
            with staged.open("wb") as raw, gzip.GzipFile(fileobj=raw, mode="wb", filename="", mtime=0) as compressed:
                process = subprocess.Popen(["git", "archive", "--format=tar", f"--prefix=tt9-{tag}/", commit],
                                           cwd=repo, stdout=subprocess.PIPE)
                try:
                    shutil.copyfileobj(process.stdout, compressed)
                finally:
                    process.stdout.close()
                if process.wait() != 0:
                    raise ValueError("Source archive failed")
            immutable_copy(staged, source)
        assets.append(source.name)
    notice_name = f"tt9-chinese-notices-{tag}.zip"
    immutable_bytes(notice_zip(notices), output / notice_name)
    assets.append(notice_name)
    inventory = {"schema": 1, "releaseTag": tag, "sourceCommit": commit, "variants": records}
    immutable_bytes((json.dumps(inventory, indent=2, sort_keys=True) + "\n").encode(), output / "chinese-delivery.json")
    assets.append("chinese-delivery.json")
    sums = "".join(f"{sha256(output / name)}  {name}\n" for name in sorted(set(assets)))
    immutable_bytes(sums.encode(), output / "SHA256SUMS")
    verify_inventory(output)
    return inventory


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--output", type=Path, required=True)
    operation = parser.add_mutually_exclusive_group(required=True)
    operation.add_argument("--tag")
    operation.add_argument("--verify-inventory", action="store_true")
    parser.add_argument("--variant", action="append", choices=["fullRelease", "liteRelease", "fullDebug", "liteDebug"])
    parser.add_argument("--skip-source", action="store_true", help="CI packaging validation only")
    args = parser.parse_args()
    if args.verify_inventory:
        names = verify_inventory(args.output.resolve())
        print(json.dumps({"verifiedAssets": len(names), "output": str(args.output)}, sort_keys=True))
        return
    report = prepare(args.repo.resolve(), args.output.resolve(), args.tag,
                     args.variant or ["liteRelease", "fullRelease"], not args.skip_source)
    print(json.dumps({"sourceCommit": report["sourceCommit"], "variants": len(report["variants"]),
                      "output": str(args.output)}, sort_keys=True))


if __name__ == "__main__":
    main()
