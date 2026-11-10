import hashlib
import importlib.util
import json
import os
import subprocess
import tempfile
import unittest
import zipfile
from pathlib import Path

spec = importlib.util.spec_from_file_location("chinese_release", Path(__file__).with_name("chinese_release.py"))
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ChineseReleaseTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(dir=os.environ.get("TT9_RELEASE_TEST_TMP"))
        self.repo = Path(self.temporary.name)
        self.delivery = dict(schema="1", packageName="io.github.sspanak.tt9", versionCode="1660",
                             variant="liteRelease", certificateSha256="ab" * 32)
        self.base = self.repo / "app/build/outputs/apk/lite/release/base.apk"
        self.base.parent.mkdir(parents=True)
        self.base.write_bytes(b"signed-base-fixture")
        self.feature, self.ui = b"signed-feature-fixture", b"signed-ui-fixture"
        for key, content in (("baseSha256", self.base.read_bytes()), ("featureSha256", self.feature), ("uiSha256", self.ui)):
            self.delivery[key] = hashlib.sha256(content).hexdigest()
        (self.base.parent / "output-metadata.json").write_text(json.dumps(dict(
            variantName="liteRelease", applicationId="io.github.sspanak.tt9",
            elements=[dict(outputFile="base.apk", versionCode=1660)])))
        self.directory = self.repo / "app/build/outputs/chinese-packs/liteRelease"
        self.directory.mkdir(parents=True)
        self.write_pack()

    def tearDown(self):
        self.temporary.cleanup()

    def properties(self):
        return "".join(f"{key}={value}\n" for key, value in self.delivery.items()).encode()

    def write_pack(self, feature=None, first="zh-simplified-pinyin.txt"):
        stable = self.directory / "zh-simplified-pinyin.zip"
        with zipfile.ZipFile(stable, "w") as archive:
            archive.writestr(first, "64 NI你".encode())
            archive.writestr("chinese/chinese.apk", self.feature if feature is None else feature)
            archive.writestr("chinese/chinese-ui.apk", self.ui)
            archive.writestr("chinese/delivery.properties", self.properties())
            for name in ("licenses/rime-ice-license.txt", "licenses/rime-ice-notice.txt",
                         "licenses/chinese-domains-notice.txt", "licenses/thuocl-license.txt", "chinese/UNICODE-LICENSE.txt"):
                archive.writestr(name, b"license fixture")
        (self.directory / release.asset_name(self.delivery)).write_bytes(stable.read_bytes())

    def test_real_archive_and_named_copy_validate(self):
        base, pack, delivery, notices = release.verified_variant(self.repo, "liteRelease")
        self.assertEqual(self.base, base)
        self.assertEqual(self.delivery, delivery)
        self.assertEqual(5, len(notices))
        self.assertEqual(release.asset_name(delivery), pack.name)

    def test_duplicate_or_extra_metadata_is_rejected(self):
        for extra in (b"schema=1\n", b"unexpected=value\n"):
            with self.subTest(extra=extra), self.assertRaises(ValueError):
                release.delivery_properties(self.properties() + extra)

    def test_old_local_pack_has_no_online_delivery_identity(self):
        with self.assertRaises(ValueError):
            release.delivery_properties(b"schema=1\n")

    def test_modified_feature_is_rejected(self):
        self.write_pack(feature=b"changed feature")
        with self.assertRaisesRegex(ValueError, "payload hash"):
            release.verified_variant(self.repo, "liteRelease")

    def test_different_base_is_rejected(self):
        self.base.write_bytes(b"different base")
        with self.assertRaisesRegex(ValueError, "base hash"):
            release.verified_variant(self.repo, "liteRelease")

    def test_other_variant_is_rejected(self):
        self.delivery["variant"] = "fullRelease"
        self.write_pack()
        with self.assertRaisesRegex(ValueError, "metadata mismatch"):
            release.verified_variant(self.repo, "liteRelease")

    def test_stale_named_copy_is_rejected(self):
        (self.directory / release.asset_name(self.delivery)).write_bytes(b"old")
        with self.assertRaisesRegex(ValueError, "deterministic"):
            release.verified_variant(self.repo, "liteRelease")

    def test_dictionary_remains_first(self):
        self.write_pack(first="not-the-dictionary.txt")
        with self.assertRaises(ValueError):
            release.verified_variant(self.repo, "liteRelease")

    def test_immutable_copy_preserves_existing_asset(self):
        target = self.repo / "immutable.apk"
        release.immutable_copy(self.base, target)
        release.immutable_copy(self.base, target)
        self.base.write_bytes(b"replacement")
        with self.assertRaisesRegex(ValueError, "overwrite"):
            release.immutable_copy(self.base, target)
        self.assertEqual(b"signed-base-fixture", target.read_bytes())

    def test_notice_archive_is_deterministic_and_complete(self):
        notices = {"licenses/test.txt": b"license", "LICENSE.txt": b"project"}
        self.assertEqual(release.notice_zip(notices), release.notice_zip(dict(reversed(list(notices.items())))))

    def test_inventory_rejects_unchecksummed_extra_file(self):
        output = self.repo / "inventory"
        output.mkdir()
        (output / "base.apk").write_bytes(b"verified")
        (output / "SHA256SUMS").write_text(f"{release.sha256(output / 'base.apk')}  base.apk\n")
        self.assertEqual(["base.apk"], release.verify_inventory(output))
        (output / "stale-unverified.txt").write_text("must never be uploaded")
        with self.assertRaisesRegex(ValueError, "Unexpected"):
            release.verify_inventory(output)

    def test_inventory_rejects_duplicate_and_self_listing(self):
        output = self.repo / "inventory"
        output.mkdir()
        (output / "base.apk").write_bytes(b"verified")
        line = f"{release.sha256(output / 'base.apk')}  base.apk\n"
        for suffix in (line, f"{'ab' * 32}  SHA256SUMS\n"):
            with self.subTest(suffix=suffix), self.assertRaisesRegex(ValueError, "self-listing or duplicate"):
                (output / "SHA256SUMS").write_text(line + suffix)
                release.verify_inventory(output)

    def test_inventory_rejects_directories_and_modified_assets(self):
        output = self.repo / "inventory"
        output.mkdir()
        (output / "base.apk").write_bytes(b"verified")
        (output / "SHA256SUMS").write_text(f"{release.sha256(output / 'base.apk')}  base.apk\n")
        (output / "unexpected").mkdir()
        with self.assertRaisesRegex(ValueError, "non-regular"):
            release.verify_inventory(output)
        (output / "unexpected").rmdir()
        (output / "base.apk").write_bytes(b"changed")
        with self.assertRaisesRegex(ValueError, "hash mismatch"):
            release.verify_inventory(output)

    def test_prepare_includes_source_notices_and_checksum_inventory(self):
        (self.repo / "LICENSE.txt").write_text("project license")
        subprocess.run(["git", "init", "--quiet"], cwd=self.repo, check=True)
        subprocess.run(["git", "add", "LICENSE.txt"], cwd=self.repo, check=True)
        subprocess.run(["git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid",
                        "commit", "--quiet", "-m", "fixture"], cwd=self.repo, check=True)
        subprocess.run(["git", "tag", "v65.0"], cwd=self.repo, check=True)
        output = self.repo / "prepared"
        report = release.prepare(self.repo, output, "v65.0", ["liteRelease"])
        self.assertEqual(1, len(report["variants"]))
        self.assertTrue((output / "tt9-source-v65.0.tar.gz").is_file())
        self.assertTrue((output / "tt9-chinese-notices-v65.0.zip").is_file())
        for line in (output / "SHA256SUMS").read_text().splitlines():
            digest, name = line.split("  ")
            self.assertEqual(digest, release.sha256(output / name))
        release.prepare(self.repo, output, "v65.0", ["liteRelease"])
        (output / "stale-unverified.txt").write_text("must never be uploaded")
        with self.assertRaisesRegex(ValueError, "Unexpected"):
            release.prepare(self.repo, output, "v65.0", ["liteRelease"])
        (output / "stale-unverified.txt").unlink()
        (self.repo / "LICENSE.txt").write_text("new source commit")
        subprocess.run(["git", "add", "LICENSE.txt"], cwd=self.repo, check=True)
        subprocess.run(["git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid",
                        "commit", "--quiet", "-m", "new source"], cwd=self.repo, check=True)
        with self.assertRaisesRegex(ValueError, "tag does not point"):
            release.prepare(self.repo, self.repo / "wrong-commit", "v65.0", ["liteRelease"])


if __name__ == "__main__":
    unittest.main()
