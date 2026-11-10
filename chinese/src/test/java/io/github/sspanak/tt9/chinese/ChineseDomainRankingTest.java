package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ChineseDomainRankingTest {
	@Test public void enabledDomainPromotesExistingTermsBehindFirstAndProtectedChoices() throws IOException {
		List<String> base = List.of("SHIWU食物", "CUSTOM个人词", "SHIWU事物", "OTHER其他", "SHIWU事务");
		List<ChineseDomainIndex.Entry> entries = ChineseDomainIndexTest.fixture().lookupExact("74498", 7, 20);
		assertEquals(List.of("SHIWU食物", "CUSTOM个人词", "SHIWU事务", "SHIWU事物", "OTHER其他"),
			ChineseDomainRanking.prioritize(base, entries, entry -> 0, raw -> raw.equals("CUSTOM个人词"), 20));
	}

	@Test public void learnedFirstAndCommonSingleChoicesSurviveDomainPromotionAndLimits() throws IOException {
		List<String> base = List.of("LEARNED常用", "SINGLE字", "SHIWU事物", "SHIWU事务", "OTHER其他");
		List<ChineseDomainIndex.Entry> entries = ChineseDomainIndexTest.fixture().lookupExact("74498", 7, 20);
		assertEquals(List.of("LEARNED常用", "SINGLE字", "SHIWU事务", "SHIWU事物", "OTHER其他"),
			ChineseDomainRanking.prioritize(base, entries, entry -> 0, raw -> raw.equals("SINGLE字"), 1));
		assertEquals(List.of("LEARNED常用", "SINGLE字", "SHIWU事物", "SHIWU事务", "OTHER其他"),
			ChineseDomainRanking.prioritize(base, entries, entry -> entry.word.equals("事物") ? 2 : 0, raw -> raw.equals("SINGLE字"), 20));
	}

	@Test public void switchingOffRestoresBaseOrderWithoutConsultingDomainLearningOrProtection() throws IOException {
		List<String> base = List.of("SHIWU食物", "SHIWU事物", "SHIWU事务");
		assertEquals(base, ChineseDomainRanking.prioritize(base, List.of(), entry -> { throw new AssertionError(); }, raw -> { throw new AssertionError(); }, 20));
		assertEquals(base, ChineseDomainRanking.prioritize(base, ChineseDomainIndexTest.fixture().lookupExact("74498", 7, 20),
			entry -> { throw new AssertionError(); }, raw -> { throw new AssertionError(); }, 0));
	}

	@Test public void promotionDeduplicatesOverlappingSourcesAndWorksWithoutBaseWords() throws IOException {
		List<ChineseDomainIndex.Entry> entries = ChineseDomainIndexTest.fixture().lookupExact("2426426", 7, 20);
		assertEquals(List.of("CHANGAN长安"), ChineseDomainRanking.prioritize(List.of("CHANGAN长安"), entries, entry -> 0, raw -> true, 20));
		assertEquals(List.of("CHANGAN长安"), ChineseDomainRanking.prioritize(List.of(), entries, entry -> 0, raw -> false, 20));
	}

	@Test public void baseFactoryAndPersonalOrderRemainUntouched() throws IOException {
		List<String> base = List.of("SHIWU事物", "SHIWU食物", "SHIWU事务");
		assertEquals(base, ChineseDomainRanking.merge(base, ChineseDomainIndexTest.fixture().lookupExact("74498", 7, 20), entry -> 100, 20));
		assertEquals(List.of("CUSTOM个人词", "SHIWU食物", "SHIWU事务", "SHIWU事物"), ChineseDomainRanking.merge(
			List.of("CUSTOM个人词", "SHIWU食物"), ChineseDomainIndexTest.fixture().lookupExact("74498", 7, 20), entry -> 0, 20));
	}

	@Test public void learningReordersOnlyDomainAdditionsAndIsReadOncePerEntry() throws IOException {
		List<ChineseDomainIndex.Entry> entries = ChineseDomainIndexTest.fixture().lookupExact("74498", 7, 20);
		AtomicInteger reads = new AtomicInteger();
		assertEquals(List.of("SHIWU食物", "SHIWU事物", "SHIWU事务"), ChineseDomainRanking.merge(List.of("SHIWU食物"), entries,
			entry -> { reads.incrementAndGet(); return entry.word.equals("事物") ? 2 : 0; }, 20));
		assertEquals(entries.size(), reads.get());
	}

	@Test public void deduplicatedAdditionsKeepTheSelectedExplicitReadingMetadata() throws IOException {
		List<ChineseDomainIndex.Entry> entries = ChineseDomainIndexTest.fixture().lookupExact("2426426", 7, 20);
		List<ChineseDomainIndex.Entry> additions = ChineseDomainRanking.additions(List.of(), entries, entry -> 0, 20);
		assertEquals(1, additions.size());
		assertSame(entries.get(0), additions.get(0));
		assertEquals("CHANG'AN", additions.get(0).reading);
		assertEquals(2, additions.get(0).domainMask);
		assertTrue(ChineseDomainRanking.additions(List.of("CHANGAN长安"), entries, entry -> 1, 20).isEmpty());
	}

	@Test public void resultLimitDoesNotTruncateExistingCandidatesAndZeroDoesNoLearningWork() throws IOException {
		List<ChineseDomainIndex.Entry> entries = ChineseDomainIndexTest.fixture().lookupExact("74498", 7, 20);
		assertEquals(List.of("A", "B", "C", "SHIWU事务"), ChineseDomainRanking.merge(List.of("A", "B", "C"), entries, entry -> 0, 1));
		assertEquals(List.of("A", "B", "C"), ChineseDomainRanking.merge(List.of("A", "B", "C"), entries, entry -> { throw new AssertionError("Disabled additions must not consult learning"); }, 0));
	}

	@Test public void equalWeightsHaveDeterministicOrderAndNegativeLearningDoesNotDemoteBase() throws IOException {
		List<ChineseDomainIndex.Entry> entries = ChineseDomainIndexTest.load(List.of(new ChineseDomainIndexTest.Row("事物", "SHI'WU", 1, 10),
			new ChineseDomainIndexTest.Row("事务", "SHI'WU", 1, 10)), true).lookupExact("74498", 1, 20);
		assertEquals(List.of("事务", "事物"), ChineseDomainRanking.additions(List.of(), entries, entry -> -1, 20).stream().map(entry -> entry.word).collect(Collectors.toList()));
	}
}
