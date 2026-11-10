package io.github.sspanak.tt9.chinese;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChinesePrefixFactoryTest {
	private static ChinesePrefixIndex index;
	private static Path indexPath;
	private static CommonHanCharacters common;
	private static final Map<String, List<String>> factoryReadings = new HashMap<>();

	@BeforeClass
	public static void loadGeneratedIndex() throws IOException {
		String path = System.getProperty("tt9.chinesePrefixIndex");
		assertNotNull("The test task must provide the generated Chinese prefix index", path);
		indexPath = Path.of(path);
		try (InputStream stream = Files.newInputStream(indexPath)) { index = ChinesePrefixIndex.load(stream); }
		Path commonPath = Path.of(System.getProperty("tt9.commonHanTable"));
		try (InputStream stream = Files.newInputStream(commonPath)) { common = CommonHanCharacters.load(stream); }
		Set<String> readings = Set.of("NI", "SHI", "WO", "DE", "LE", "HAO", "XIAN", "ZHONG", "XIONG", "LV", "NIHAO", "WOMEN", "ZHONGGUO", "LVSE");
		try (Stream<String> lines = Files.lines(commonPath.resolveSibling("../dictionaries/zh-simplified-pinyin.csv").normalize())) {
			lines.forEach(line -> {
				String[] fields = line.split("\t", 3);
				if (fields.length == 3 && readings.contains(fields[1])) {
					factoryReadings.computeIfAbsent(fields[1], ignored -> new ArrayList<>())
						.add(String.format(java.util.Locale.ROOT, "%04d", Integer.parseInt(fields[2])) + fields[1] + fields[0]);
				}
			});
		}
		factoryReadings.replaceAll((reading, words) -> words.stream().sorted(Comparator.reverseOrder()).limit(20).map(raw -> raw.substring(4)).collect(Collectors.toList()));
	}

	@Test
	public void commonGreetingAndQuestionAreBothInTheFirstTwelve() {
		List<ChinesePrefixIndex.Entry> entries = index.lookup("你", 12);
		int hello = -1;
		for (int i = 0; i < entries.size(); i++) if (entries.get(i).word.equals("你好")) hello = i;
		assertTrue("你好 must be among the first twelve completions", hello >= 0 && hello + 1 < entries.size());
		assertEquals("你好吗", entries.get(hello + 1).word);
		assertEquals("NIHAO", entries.get(hello).transcription);
		assertEquals("64426", entries.get(hello).sequence);
		assertEquals(7002, entries.get(hello).frequency);
		assertEquals("NIHAOMA", entries.get(hello + 1).transcription);
		assertEquals("6442662", entries.get(hello + 1).sequence);
		assertEquals(5560, entries.get(hello + 1).frequency);
	}

	@Test
	public void longerPrefixHasItsOwnQuestionContinuation() {
		assertEquals("你好吗", index.lookup("你好", 12).get(0).word);
	}

	@Test public void firstKeyFindsCommonCharactersBeyondExactVowelReadings() {
		assertTrue(index.lookupFirstKey("3", "", 32).stream().anyMatch(entry -> entry.word.equals("的") && entry.transcription.equals("DE")));
		assertTrue(index.lookupFirstKey("6", "", 32).stream().anyMatch(entry -> entry.word.equals("你") && entry.transcription.equals("NI")));
		for (int key = 2; key <= 9; key++) {
			String digits = Integer.toString(key);
			List<ChinesePrefixIndex.Entry> entries = index.lookupFirstKey(digits, "", 1000);
			assertTrue(!entries.isEmpty() && entries.size() <= ChinesePrefixIndex.MAX_FIRST_KEY_RESULTS);
			int lastFrequency = Integer.MAX_VALUE;
			for (ChinesePrefixIndex.Entry entry : entries) {
				assertTrue(entry.sequence.startsWith(digits));
				assertTrue(entry.frequency <= lastFrequency);
				lastFrequency = entry.frequency;
			}
		}
	}

	@Test
	public void factoryIndexFitsTheFlipPhoneMemoryBudget() throws IOException {
		assertTrue("Keep the raw index below 3 MiB", Files.size(indexPath) < 3 * 1024 * 1024);
	}

	@Test
	public void frequentSingleCharacterReadingsUseOriginalCorpusWeights() {
		assertTrue(index.hasSingleCharacterFrequencies());
		assertEquals(7801, index.singleCharacterFrequency("NI你"));
		assertEquals(5966, index.singleCharacterFrequency("NI拟"));
		assertEquals(5866, index.singleCharacterFrequency("NI腻"));
		assertEquals(0, index.singleCharacterFrequency("NI倪"));
		assertEquals(0, index.singleCharacterFrequency("NIHAO你好"));
		assertEquals(6412, index.singleCharacterFrequency("CHONG重"));
		assertEquals(7961, index.singleCharacterFrequency("ZHONG重"));
		assertEquals(6834, index.singleCharacterFrequency("LV绿"));
		assertEquals(0, index.singleCharacterFrequency("LU绿"));
	}

	@Test
	public void niRanksOrdinaryCharactersBeforePhrasesAndSecondaryCharactersAfter() {
		List<String> words = List.of("NI你", "NI尼", "NI妮", "NI泥", "NI逆", "NI拟", "NI腻", "NI倪", "NI霓", "NI匿", "NI溺", "NI昵", "NI睨");
		List<String> completions = index.lookup("你", 12).stream().map(entry -> entry.transcription + entry.word).collect(Collectors.toList());
		List<String> ranked = ChineseCandidateRanking.merge(words, completions, index::singleCharacterFrequency,
			raw -> index.singleCharacterFrequency(raw) > 0 && common.containsOnlyCommonHan(raw));
		assertEquals(words.subList(0, 7), ranked.subList(0, 7));
		assertTrue(ranked.indexOf("NIHAO你好") > ranked.indexOf("NI腻"));
		assertTrue(ranked.indexOf("NIHAOMA你好吗") < ranked.indexOf("NI倪"));
		assertTrue(ranked.indexOf("NIHAOMA你好吗") < ranked.indexOf("NI睨"));
	}

	@Test
	public void representativeSyllablesKeepCommonCharactersBeforeCompletions() {
		Map<String, String> firstWords = Map.of("SHI", "是", "WO", "我", "DE", "的", "LE", "了", "HAO", "好", "XIAN", "先", "ZHONG", "中", "XIONG", "兄", "LV", "绿");
		for (Map.Entry<String, String> sample : firstWords.entrySet()) {
			List<String> words = factoryReadings.get(sample.getKey());
			List<String> completions = completions(sample.getValue());
			List<String> ranked = rankExact(words, completions);
			assertEquals(sample.getKey() + sample.getValue(), ranked.get(0));
			if (completions.isEmpty()) continue;
			int firstCompletion = ranked.indexOf(completions.get(0));
			for (String raw : words) {
				if (index.singleCharacterFrequency(raw) > 0 && common.containsOnlyCommonHan(raw)) {
					assertTrue(sample.getKey() + ": ordinary " + raw + " must precede completions", ranked.indexOf(raw) < firstCompletion);
				}
			}
		}
		// XIAN can be one syllable or the fully typed two-character place name.
		List<String> xian = rankExact(factoryReadings.get("XIAN"), completions("先"));
		assertTrue(xian.indexOf("XIAN西安") < xian.indexOf(completions("先").get(0)));
	}

	@Test
	public void fullPhraseQueriesAndLearnedRareCharactersRetainPriority() {
		for (Map.Entry<String, String> sample : Map.of("NIHAO", "你好", "WOMEN", "我们", "ZHONGGUO", "中国", "LVSE", "绿色").entrySet()) {
			assertEquals(sample.getKey() + sample.getValue(), rankExact(factoryReadings.get(sample.getKey()), completions(sample.getValue())).get(0));
		}
		ArrayList<String> learned = new ArrayList<>(factoryReadings.get("NI"));
		learned.remove("NI睨");
		learned.remove("NI倪");
		learned.add(0, "NI倪");
		learned.add(0, "NI睨");
		assertEquals(List.of("NI睨", "NI倪", "NI你"), rankExact(learned, completions("你")).subList(0, 3));
	}

	private List<String> completions(String prefix) {
		return index.lookup(prefix, 12).stream().map(entry -> entry.transcription + entry.word).collect(Collectors.toList());
	}

	private List<String> rankExact(List<String> words, List<String> completions) {
		return ChineseCandidateRanking.merge(words, completions, index::singleCharacterFrequency,
			raw -> raw.codePoints().filter(code -> Character.UnicodeScript.of(code) == Character.UnicodeScript.HAN).count() > 1
				|| index.singleCharacterFrequency(raw) > 0 && common.containsOnlyCommonHan(raw));
	}
}
