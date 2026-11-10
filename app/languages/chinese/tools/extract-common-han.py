"""Extract the official level-one character set from a pinned Unicode archive."""
import hashlib
from pathlib import Path
import re
import sys
import zipfile

archive = Path(sys.argv[1])
expected = "f7a48b2b545acfaa77b2d607ae28747404ce02baefee16396c5d2d7a8ef34b5e"
if hashlib.sha256(archive.read_bytes()).hexdigest() != expected:
    raise SystemExit("Expected the official Unicode 17.0.0 Unihan.zip archive")
with zipfile.ZipFile(archive) as source:
    mappings = source.read("Unihan_OtherMappings.txt").decode("utf-8")
characters = {}
for code, ordinal in re.findall(r"^U\+([0-9A-F]+)\s+kTGH\s+2013:(\d+)\s*$", mappings, re.M):
    if int(ordinal) <= 3500:
        characters[int(ordinal)] = chr(int(code, 16))
if set(characters) != set(range(1, 3501)):
    raise SystemExit("Incomplete or duplicate official character table")
text = "".join(characters[index] for index in range(1, 3501))
if len(set(text)) != 3500:
    raise SystemExit("Duplicate characters")
Path(__file__).resolve().parents[1].joinpath("common-3500.txt").write_bytes(text.encode("utf-8"))
