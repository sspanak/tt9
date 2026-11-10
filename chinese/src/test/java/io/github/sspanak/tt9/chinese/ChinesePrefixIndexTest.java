package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ChinesePrefixIndexTest {
	@Test
	public void completionRetainsFullReadingAndLearningSequence() throws IOException {
		ChinesePrefixIndex index = fixture();
		ChinesePrefixIndex.Entry hello = index.lookup("你", 9).get(0);
		assertEquals("你好", hello.word);
		assertEquals("NIHAO", hello.transcription);
		assertEquals("64426", hello.sequence);
		assertEquals(7002, hello.frequency);
	}

	@Test
	public void existingQuestionIsAdjacentToItsParentWithoutChangingFactoryWeight() throws IOException {
		List<ChinesePrefixIndex.Entry> entries = fixture().lookup("你", 9);
		assertEquals(List.of("你好", "你好吗", "你们"), entries.stream().map(entry -> entry.word).collect(Collectors.toList()));
		assertEquals("NIHAOMA", entries.get(1).transcription);
		assertEquals("6442662", entries.get(1).sequence);
		assertEquals(5560, entries.get(1).frequency);
	}

	@Test
	public void longerPrefixUsesItsOwnRankedContinuations() throws IOException {
		List<ChinesePrefixIndex.Entry> entries = fixture().lookup("你好", 9);
		assertEquals(1, entries.size());
		assertEquals("你好吗", entries.get(0).word);
	}

	@Test
	public void unknownExactAndEmptyPrefixesDoNotInventWords() throws IOException {
		ChinesePrefixIndex index = fixture();
		assertTrue(index.lookup("你好吗", 9).isEmpty());
		assertTrue(index.lookup("不存在", 9).isEmpty());
		assertTrue(index.lookup("", 9).isEmpty());
		assertTrue(index.lookup(null, 9).isEmpty());
		assertTrue(index.lookup("你", 0).isEmpty());
	}

	@Test
	public void resultLimitIncludesQuestionForms() throws IOException {
		List<ChinesePrefixIndex.Entry> entries = fixture().lookup("你", 2);
		assertEquals(List.of("你好", "你好吗"), entries.stream().map(entry -> entry.word).collect(Collectors.toList()));
	}

	@Test(expected = IOException.class)
	public void truncatedIndexFailsBeforeLookup() throws IOException {
		ChinesePrefixIndex.load(new ByteArrayInputStream(new byte[]{0x54, 0x54, 0x39, 0x50}));
	}

	@Test(expected = IOException.class)
	public void unsupportedFormatIsRejected() throws IOException {
		ChinesePrefixIndex.load(new ByteArrayInputStream(new byte[16]));
	}

	@Test
	public void oldPacksStillLoadWithoutCharacterWeights() throws IOException {
		ChinesePrefixIndex index = fixture();
		assertFalse(index.hasSingleCharacterFrequencies());
		assertEquals(0, index.singleCharacterFrequency("NI你"));
		assertTrue(index.lookupFirstKey("6", "", 9).isEmpty());
	}

	@Test public void firstKeyIncludesFrequentCharactersAlongsideExactVowels() throws IOException {
		ChinesePrefixIndex index = characterFixture(Map.of("DE的", 9999, "DEI得", 9000, "E饿", 7332, "EN嗯", 7000));
		assertEquals(List.of("的", "得", "饿", "嗯"), index.lookupFirstKey("3", "", 9).stream().map(entry -> entry.word).collect(Collectors.toList()));
		assertEquals("33", index.lookupFirstKey("3", "", 1).get(0).sequence);
		assertEquals(List.of("E饿", "DE的", "DEI得", "EN嗯"), ChineseCandidateRanking.merge(List.of("E饿"),
			List.of("DE的", "DEI得", "E饿", "EN嗯"), index::singleCharacterFrequency, word -> false));
	}

	@Test public void firstKeyBucketsCoverTheKeypadWithoutCrossingSelectedReadings() throws IOException {
		ChinesePrefixIndex index = characterFixture(Map.of("BA八", 6000, "DE的", 9999, "GUO国", 8000, "LE了", 9500,
			"NI你", 7801, "SHI是", 9506, "TA他", 9000, "WO我", 9472));
		String[] expected = {"八", "的", "国", "了", "你", "是", "他", "我"};
		for (int key = 2; key <= 9; key++) assertEquals(expected[key - 2], index.lookupFirstKey(Integer.toString(key), "", 9).get(0).word);
		for (String digits : new String[]{null, "", "1", "0", "64", "a"}) assertTrue(index.lookupFirstKey(digits, "", 9).isEmpty());
		assertTrue(index.lookupFirstKey("6", "NI", 9).isEmpty());
		assertTrue(index.lookupFirstKey("6", "", 0).isEmpty());
	}

	@Test public void firstKeyResultsAreBoundedDeduplicatedAndKeepPromotedLeadingRows() throws IOException {
		Map<String, Integer> weights = new TreeMap<>();
		for (int i = 0; i < 50; i++) weights.put("NI" + (char) (0x4e00 + i), 6000 + i);
		weights.put("NE" + (char) (0x4e00 + 49), 7000);
		ChinesePrefixIndex index = characterFixture(weights);
		List<ChinesePrefixIndex.Entry> entries = index.lookupFirstKey("6", "", 1000);
		assertEquals(ChinesePrefixIndex.MAX_FIRST_KEY_RESULTS, entries.size());
		assertEquals(7000, entries.get(0).frequency);
		assertEquals(entries.size(), entries.stream().map(entry -> entry.word).distinct().count());
		assertEquals(List.of("NI罕", "NI你", "NI自", "NI新"), ChineseCandidateRanking.merge(
			List.of("NI罕", "NI你", "NI自"), List.of("NI新"), word -> "NI你".equals(word) ? 7801 : 0,
			word -> "NI自".equals(word)));
	}

	@Test
	public void newPackFindsReadingSpecificCharacterWeights() throws IOException {
		ChinesePrefixIndex index = characterFixture(Map.of("CHONG重", 6412, "NI你", 7801, "ZHONG重", 7961));
		assertTrue(index.hasSingleCharacterFrequencies());
		assertEquals(6412, index.singleCharacterFrequency("CHONG重"));
		assertEquals(7801, index.singleCharacterFrequency("NI你"));
		assertEquals(7961, index.singleCharacterFrequency("ZHONG重"));
		assertEquals(0, index.singleCharacterFrequency("NI倪"));
		assertEquals(0, index.singleCharacterFrequency(null));
	}

	@Test(expected = IOException.class)
	public void invalidCharacterWeightIsRejectedWhileLoading() throws IOException {
		characterFixture(Map.of("NI你", -1));
	}

	private ChinesePrefixIndex characterFixture(Map<String, Integer> weights) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(0x54543950);
			output.writeInt(2);
			output.writeInt(0);
			output.writeInt(0);
			output.writeInt(weights.size());
			for (Map.Entry<String, Integer> entry : new TreeMap<>(weights).entrySet()) {
				string(output, entry.getKey());
				output.writeInt(entry.getValue());
			}
		}
		return ChinesePrefixIndex.load(new ByteArrayInputStream(bytes.toByteArray()));
	}

	private ChinesePrefixIndex fixture() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(0x54543950);
			output.writeInt(1);
			output.writeInt(3);
			output.writeInt(2);
			word(output, "你好", "NIHAO", 7002);
			word(output, "你好吗", "NIHAOMA", 5560);
			word(output, "你们", "NIMEN", 7227);
			Map<String, int[]> prefixes = new TreeMap<>();
			prefixes.put("你", new int[]{0, 2});
			prefixes.put("你好", new int[]{1});
			for (Map.Entry<String, int[]> prefix : prefixes.entrySet()) {
				string(output, prefix.getKey());
				output.writeByte(prefix.getValue().length);
				for (int id : prefix.getValue()) output.writeInt(id);
			}
		}
		return ChinesePrefixIndex.load(new ByteArrayInputStream(bytes.toByteArray()));
	}

	private void word(DataOutputStream output, String word, String reading, int frequency) throws IOException {
		string(output, word);
		string(output, reading);
		output.writeInt(frequency);
	}

	private void string(DataOutputStream output, String value) throws IOException {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		output.writeShort(bytes.length);
		output.write(bytes);
	}
}
