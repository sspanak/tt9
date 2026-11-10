package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ChineseDomainIndexTest {
	@Test public void metadataAndPerDomainCountsIncludeSharedReadings() throws IOException {
		ChineseDomainIndex index = fixture();
		assertEquals(3, index.getDomains().size());
		ChineseDomainIndex.Domain medical = index.getDomains().get(0);
		assertEquals("medical", medical.id);
		assertEquals("Medical", medical.labelEn);
		assertEquals("医学", medical.labelZh);
		assertEquals("CC0-1.0", medical.license);
		assertEquals("https://example.test/medical", medical.source);
		assertEquals(5, medical.wordCount);
		assertEquals(5, index.count(medical));
		assertEquals(4, index.count("engineering"));
		assertEquals(2, index.count("finance"));
		assertEquals(0, index.count("missing"));
		assertEquals(0, index.count((ChineseDomainIndex.Domain) null));
	}

	@Test public void inactiveDomainsNeverContributeAndStableIdsProduceMasks() throws IOException {
		ChineseDomainIndex index = fixture();
		assertEquals(0, index.maskFor(null));
		assertEquals(0, index.maskFor(Set.of()));
		assertEquals(0, index.maskFor(Set.of("unknown")));
		assertEquals(5, index.maskFor(Set.of("finance", "medical")));
		assertTrue(index.lookupExact("74498", 0, 10).isEmpty());
		assertTrue(index.lookupExact("74498", 2, 10).isEmpty());
		assertEquals(List.of("事务"), words(index.lookupExact("74498", 4, 10)));
		assertEquals(List.of("事物"), words(index.lookupExact("74498", 1, 10)));
		assertEquals(List.of("事务", "事物"), words(index.lookupExact("74498", 5, 10)));
	}

	@Test public void exactLookupKeepsWeightOrderAndNeverReturnsAnotherSequence() throws IOException {
		ChineseDomainIndex index = fixture();
		assertEquals(List.of("事务"), words(index.lookupExact("74498", 7, 1)));
		assertTrue(index.lookupExact("7449", 7, 10).isEmpty());
		assertEquals(List.of("编译器"), words(index.lookupExact("24269474", 7, 10)));
		assertTrue(index.lookupExact("242694749", 7, 10).isEmpty());
	}

	@Test public void explicitBoundariesPolyphonesAndSupplementaryHanSurviveLookup() throws IOException {
		ChineseDomainIndex index = fixture();
		ChineseDomainIndex.Entry compiler = index.lookupExact("24269474", 2, 2).get(0);
		assertEquals("BIAN'YI'QI", compiler.reading);
		assertEquals("BIANYIQI编译器", compiler.raw());
		assertEquals(2, compiler.domainMask);
		assertEquals(100, compiler.weight);
		assertEquals("CHONG", index.lookupExact("24664", 1, 2).get(0).reading);
		assertEquals("ZHONG", index.lookupExact("94664", 2, 2).get(0).reading);
		assertEquals(List.of("CHANG'AN", "CHAN'GAN"), index.lookupExact("2426426", 7, 4).stream().map(entry -> entry.reading).collect(Collectors.toList()));
		assertEquals("𫠜", index.lookupExact("64", 1, 2).get(0).word);
	}

	@Test public void prefixFallbackExcludesExactAndUnrelatedReadings() throws IOException {
		ChineseDomainIndex index = fixture();
		assertEquals(List.of("你好"), words(index.lookupPrefix("64", 1, 10)));
		assertEquals(List.of("你好"), words(index.lookupPrefix("644", 2, 10)));
		assertTrue(index.lookupPrefix("64426", 7, 10).isEmpty());
		assertTrue(index.lookupPrefix("999", 7, 10).isEmpty());
		assertTrue(index.lookupPrefix("64", 0, 10).isEmpty());
	}

	@Test public void malformedQueriesAndLimitsHaveNoResults() throws IOException {
		ChineseDomainIndex index = fixture();
		for (String sequence : Arrays.asList(null, "", "01", "64'", "64A", "6 ", "9".repeat(193))) {
			assertTrue(index.lookupExact(sequence, 7, 10).isEmpty());
			assertTrue(index.lookupPrefix(sequence, 7, 10).isEmpty());
		}
		assertTrue(index.lookupExact("64", 7, 0).isEmpty());
		assertTrue(index.lookupPrefix("64", 7, -1).isEmpty());
	}

	@Test public void fallbackWorkAndReturnedCandidatesHaveHardBounds() throws IOException {
		List<Row> rows = new ArrayList<>();
		for (int i = 0; i <= ChineseDomainIndex.MAX_PREFIX_SCAN; i++) {
			rows.add(new Row("你" + (char) (0x4e00 + i), "NI'NI", i == ChineseDomainIndex.MAX_PREFIX_SCAN ? 2 : 1, ChineseDomainIndex.MAX_PREFIX_SCAN - i));
		}
		ChineseDomainIndex index = load(rows, true);
		assertTrue(index.lookupPrefix("64", 2, Integer.MAX_VALUE).isEmpty());
		assertEquals(1, index.lookupExact("6464", 2, 1).size());
		assertEquals(ChineseDomainIndex.MAX_RESULTS, index.lookupExact("6464", 1, Integer.MAX_VALUE).size());
		assertEquals(ChineseDomainIndex.MAX_RESULTS, index.lookupPrefix("64", 1, Integer.MAX_VALUE).size());
	}

	@Test public void catalogIsImmutableAndEmptyVocabularyIsSafe() throws IOException {
		ChineseDomainIndex index = load(List.of(), true);
		assertEquals(0, index.count("medical"));
		assertTrue(index.lookupExact("64", 7, 10).isEmpty());
		try { index.getDomains().clear(); fail("Catalog must be immutable"); }
		catch (UnsupportedOperationException expected) { assertFalse(index.getDomains().isEmpty()); }
	}

	@Test public void corruptedHeadersTruncationAndTrailingBytesAreRejected() throws IOException {
		byte[] valid = fixtureBytes();
		for (byte[] invalid : List.of(new byte[0], Arrays.copyOf(valid, 10), Arrays.copyOf(valid, valid.length - 1), Arrays.copyOf(valid, valid.length + 1))) expectInvalid(invalid);
		byte[] badMagic = valid.clone(); badMagic[0] = 0; expectInvalid(badMagic);
		byte[] badVersion = valid.clone(); badVersion[7] = 2; expectInvalid(badVersion);
		byte[] badDomainCount = valid.clone(); badDomainCount[11] = 31; expectInvalid(badDomainCount);
	}

	@Test public void invalidUtf8IsRejectedWithoutReplacementCharacters() throws IOException {
		byte[] invalid = fixtureBytes();
		invalid[14] = (byte) 0xc3; invalid[15] = 0x28;
		try { ChineseDomainIndex.load(new ByteArrayInputStream(invalid)); fail("Invalid UTF-8 accepted"); }
		catch (IOException expected) { assertTrue(expected.getMessage().contains("UTF-8")); }
	}

	@Test public void unsafeDuplicateAndUnattributedDomainsAreRejected() throws IOException {
		for (String id : List.of("../medical", "Medical", "", "has space")) expectInvalid(bytes(List.of(), true, new String[]{id, "engineering", "finance"}, "CC0-1.0"));
		expectInvalid(bytes(List.of(), true, new String[]{"medical", "medical", "finance"}, "CC0-1.0"));
		expectInvalid(bytes(List.of(), true, IDS, ""));
	}

	@Test public void masksWeightsAndReadingValidationRejectMalformedEntries() throws IOException {
		for (Row row : List.of(new Row("你", "NI", 0, 1), new Row("你", "NI", 8, 1), new Row("你", "NI", -1, 1),
			new Row("你", "NI", 1, -1), new Row("GPU", "JI'PI'YOU", 1, 1), new Row("你好", "NI", 1, 1),
			new Row("你", "NI3", 1, 1), new Row("你", "ni", 1, 1), new Row(" 你", "NI", 1, 1), new Row("长安", "CHANGAN", 1, 1))) {
			expectInvalid(bytes(List.of(row), false, IDS, "CC0-1.0"));
		}
		Row mismatch = new Row("你", "NI", 1, 1); mismatch.sequence = "54";
		expectInvalid(bytes(List.of(mismatch), false, IDS, "CC0-1.0"));
	}

	@Test public void duplicateReadingsMustBeMergedByTheBuilder() throws IOException {
		expectInvalid(bytes(List.of(new Row("你好", "NI'HAO", 1, 10), new Row("你好", "NI'HAO", 2, 9)), true, IDS, "CC0-1.0"));
	}

	@Test public void unsortedSequenceWeightAndStableTiesAreRejected() throws IOException {
		expectInvalid(bytes(List.of(new Row("你好", "NI'HAO", 1, 1), new Row("编译器", "BIAN'YI'QI", 1, 1)), false, IDS, "CC0-1.0"));
		expectInvalid(bytes(List.of(new Row("事务", "SHI'WU", 1, 1), new Row("事物", "SHI'WU", 1, 2)), false, IDS, "CC0-1.0"));
		expectInvalid(bytes(List.of(new Row("事物", "SHI'WU", 1, 1), new Row("事务", "SHI'WU", 1, 1)), false, IDS, "CC0-1.0"));
	}

	@Test public void fileAndEntryLimitsAreEnforcedBeforeAllocation() throws IOException {
		expectInvalid(new byte[ChineseDomainIndex.MAX_BYTES + 1]);
		byte[] bytes = bytes(List.of(), true, IDS, "CC0-1.0");
		int offset = bytes.length - 4;
		int count = ChineseDomainIndex.MAX_ENTRIES + 1;
		for (int i = 0; i < 4; i++) bytes[offset + i] = (byte) (count >>> (24 - i * 8));
		expectInvalid(bytes);
	}

	static final String[] IDS = {"medical", "engineering", "finance"};
	static final class Row {
		final String word, reading;
		String sequence;
		final int mask, weight;
		Row(String word, String reading, int mask, int weight) {
			this.word = word; this.reading = reading; this.mask = mask; this.weight = weight;
			try { sequence = PinyinSyllables.getSequence(reading); } catch (IllegalArgumentException invalid) { sequence = "64"; }
		}
		String raw() { return reading.replace("'", "") + word; }
	}
	static List<Row> rows() {
		return List.of(new Row("重", "CHONG", 1, 5), new Row("重", "ZHONG", 2, 6), new Row("你好", "NI'HAO", 3, 9),
			new Row("编译器", "BIAN'YI'QI", 2, 100), new Row("事务", "SHI'WU", 4, 50), new Row("事物", "SHI'WU", 1, 20),
			new Row("医学", "YI'XUE", 1, 9), new Row("𫠜", "NI", 1, 1), new Row("长安", "CHANG'AN", 2, 50), new Row("长安", "CHAN'GAN", 4, 40));
	}
	static ChineseDomainIndex fixture() throws IOException { return ChineseDomainIndex.load(new ByteArrayInputStream(fixtureBytes())); }
	static byte[] fixtureBytes() throws IOException { return bytes(rows(), true, IDS, "CC0-1.0"); }
	static ChineseDomainIndex load(List<Row> rows, boolean sort) throws IOException { return ChineseDomainIndex.load(new ByteArrayInputStream(bytes(rows, sort, IDS, "CC0-1.0"))); }
	static byte[] bytes(List<Row> input, boolean sort, String[] ids, String license) throws IOException {
		ArrayList<Row> rows = new ArrayList<>(input);
		if (sort) rows.sort(Comparator.comparing((Row row) -> row.sequence).thenComparing(Comparator.comparingInt((Row row) -> row.weight).reversed()).thenComparing(Row::raw).thenComparing(row -> row.reading));
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(0x54543944); output.writeInt(1); output.writeInt(ids.length);
			for (String id : ids) { string(output, id); string(output, id.equals("medical") ? "Medical" : id); string(output, "医学"); string(output, license); string(output, "https://example.test/" + id); }
			output.writeInt(rows.size());
			for (Row row : rows) { string(output, row.sequence); output.writeInt(row.mask); string(output, row.word); string(output, row.reading); output.writeInt(row.weight); }
		}
		return bytes.toByteArray();
	}
	private static void string(DataOutputStream output, String value) throws IOException { byte[] bytes = value.getBytes(StandardCharsets.UTF_8); output.writeShort(bytes.length); output.write(bytes); }
	private static List<String> words(List<ChineseDomainIndex.Entry> entries) { return entries.stream().map(entry -> entry.word).collect(Collectors.toList()); }
	private static void expectInvalid(byte[] bytes) throws IOException {
		try { ChineseDomainIndex.load(new ByteArrayInputStream(bytes)); fail("Invalid domain data accepted"); }
		catch (IOException expected) { /* Rejection must occur before any candidates are exposed. */ }
	}
}
