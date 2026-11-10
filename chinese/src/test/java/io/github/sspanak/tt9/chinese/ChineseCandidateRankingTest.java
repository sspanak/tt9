package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ChineseCandidateRankingTest {
	private final Map<String, Integer> weights = Map.of("NI你", 7801, "NI尼", 6891, "NI泥", 6443, "NI逆", 6172, "NI拟", 5966);
	private final List<String> phrases = List.of("NIHAO你好", "NIHAOMA你好吗");

	@Test public void ordinarySingleCharactersPrecedeCompletionsAndSecondaryCharactersFollow() {
		List<String> words = List.of("NI你", "NI尼", "NI泥", "NI逆", "NI拟", "NI倪", "NI睨");
		assertEquals(List.of("NI你", "NI尼", "NI泥", "NI逆", "NI拟", "NIHAO你好", "NIHAOMA你好吗", "NI倪", "NI睨"), merge(words, Set.of()));
	}

	@Test public void multipleLearnedSecondaryCharactersRetainTheirPromotedOrder() {
		List<String> words = List.of("NI睨", "NI倪", "NI你", "NI尼", "NI泥");
		assertEquals(List.of("NI睨", "NI倪", "NI你", "NI尼", "NI泥", "NIHAO你好", "NIHAOMA你好吗"), merge(words, Set.of()));
	}

	@Test public void explicitCustomWordsRemainAheadOfCompletions() {
		List<String> words = List.of("NI你", "NI尼", "NI倪", "NI睨");
		assertEquals(List.of("NI你", "NI尼", "NI睨", "NIHAO你好", "NIHAOMA你好吗", "NI倪"), merge(words, Set.of("NI睨")));
	}

	@Test public void fullWordInputStaysAheadOfItsLongerCompletion() {
		assertEquals(List.of("NIHAO你好", "NIHAOMA你好吗"), ChineseCandidateRanking.merge(
			List.of("NIHAO你好"), List.of("NIHAOMA你好吗"), ignored -> 0, ignored -> true));
	}

	@Test public void disablingOrMissingCompletionsLeavesDatabaseRankingIntact() {
		List<String> words = List.of("NI你", "NI倪", "NI尼");
		assertEquals(words, ChineseCandidateRanking.merge(words, List.of(), raw -> weights.getOrDefault(raw, 0), weights::containsKey));
	}

	@Test public void noCandidatesDoesNotInventCompletionsAndDuplicatesAreRemoved() {
		assertEquals(List.of(), merge(List.of(), Set.of()));
		assertEquals(List.of("NI你", "NIHAO你好", "NIHAOMA你好吗"), merge(List.of("NI你", "NIHAO你好"), Set.of("NIHAO你好")));
	}

	private List<String> merge(List<String> words, Set<String> custom) {
		return ChineseCandidateRanking.merge(words, phrases, raw -> weights.getOrDefault(raw, 0), raw -> weights.containsKey(raw) || custom.contains(raw));
	}
}
