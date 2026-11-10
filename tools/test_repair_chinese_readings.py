import importlib.util
import unittest
from pathlib import Path
from unittest.mock import patch


spec = importlib.util.spec_from_file_location("repair_chinese_readings", Path(__file__).with_name("repair_chinese_readings.py"))
repair = importlib.util.module_from_spec(spec)
spec.loader.exec_module(repair)


class ChineseReadingsRepairTest(unittest.TestCase):
    def test_alternative_readings_preserve_every_original_byte_and_weight(self):
        original = "说\tSHUI\t382\r\n长\tCHANG\t8231\r\n旧词\tJIUCI".encode()
        source = "...\n说\tshui\t1\n说\tshuo\t12000460\n长\tzhang\t3100060\n".encode()
        rows = repair.source_readings(source, repair.factory_entries(original))
        result, additions = repair.repair_dictionary(original, rows)
        self.assertTrue(result.startswith(original + b"\r\n"))
        self.assertEqual([("说", "SHUO"), ("长", "ZHANG")], [r[:2] for r in additions])
        self.assertEqual(382, repair.factory_entries(result)[("说", "SHUI")])
        self.assertEqual(8231, repair.factory_entries(result)[("长", "CHANG")])
        self.assertEqual(8976, repair.factory_entries(result)[("说", "SHUO")])
        self.assertEqual((result, []), repair.repair_dictionary(result, rows))
        self.assertEqual(repair.reading_manifest(rows, repair.factory_entries(original)),
                         repair.reading_manifest(rows, repair.factory_entries(result)))

    def test_new_characters_supplementary_han_and_commented_readings_stay_excluded(self):
        original = "行\tHANG\t8166\n𦝼\tLOU\t0".encode()
        source = "...\n#行\theng\t1\n行\txing\t2756706\n新\txin\t100\n𦝼\tlv\t0\n".encode()
        rows = repair.source_readings(source, repair.factory_entries(original))
        self.assertEqual([("行", "XING", 2756706, 3)], rows)
        result, _ = repair.repair_dictionary(original, rows)
        self.assertEqual({"行", "𦝼"}, {word for word, _ in repair.factory_entries(result)})

    def test_zero_source_weight_does_not_discard_an_active_reading(self):
        original = "了\tLE\t9000\n".encode()
        rows = repair.source_readings("...\n了\tliao\t0\n".encode(), repair.factory_entries(original))
        result, _ = repair.repair_dictionary(original, rows)
        self.assertEqual(0, repair.factory_entries(result)[("了", "LIAO")])
        self.assertEqual(9999, repair.factory_weight(repair.SOURCE_MAX_WEIGHT))

    def test_duplicate_pairs_are_rejected_but_distinct_readings_are_valid(self):
        with self.assertRaisesRegex(ValueError, "Duplicate factory"):
            repair.factory_entries("行\tHANG\t1\n行\tHANG\t2".encode())
        entries = repair.factory_entries("行\tHANG\t1\n行\tXING\t2".encode())
        self.assertEqual(2, len(entries))
        with self.assertRaisesRegex(ValueError, "Duplicate source"):
            repair.source_readings("...\n行\txing\t1\n行\txing\t2".encode(), entries)

    def test_unreviewed_source_is_rejected(self):
        with patch.object(Path, "read_bytes", return_value=b"unreviewed source"):
            with self.assertRaisesRegex(ValueError, "SHA-256"):
                repair.verified_source(Path("source.yaml"))

    def test_factory_contains_every_manifest_pair_once(self):
        data = repair.DEFAULT_DICTIONARY.read_bytes()
        entries = repair.factory_entries(data)
        expected = set()
        for line in repair.DEFAULT_MANIFEST.read_text(encoding="utf-8").splitlines():
            if line.startswith("#"):
                continue
            word, reading, weight, number = line.split("\t")
            pair = word, reading
            self.assertNotIn(pair, expected)
            expected.add(pair)
            self.assertIn(pair, entries)
            self.assertGreaterEqual(int(weight), 0)
            self.assertGreater(int(number), 0)
        self.assertEqual(772, len(expected))
        self.assertEqual(375, len({word for word, _ in expected}))


if __name__ == "__main__":
    unittest.main()
