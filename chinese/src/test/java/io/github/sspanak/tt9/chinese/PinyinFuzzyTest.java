package io.github.sspanak.tt9.chinese;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class PinyinFuzzyTest {
	@Test public void editingGuardRejectsStaleShortReadingsButKeepsEnabledFuzzyAlternatives() {
		assertFalse(PinyinFuzzy.matches("9426", "", "Z", 0));
		assertFalse(PinyinFuzzy.matches("9426", "", "XI", 0));
		assertTrue(PinyinFuzzy.matches("9426", "", "XIAN", 0));
		assertTrue(PinyinFuzzy.matches("64", "", "LI", PinyinFuzzy.NL));
		assertTrue(PinyinFuzzy.matches("9664", "", "ZHONG", PinyinFuzzy.Z_ZH));
	}

	@Test public void unfinishedSyllablesKeepTheirComposition() {
		assertTrue(PinyinFuzzy.matchesPrefix("644", "", "NIHAO"));
		assertTrue(PinyinFuzzy.matchesPrefix("6442", "NI", "NIHAO"));
		assertFalse(PinyinFuzzy.matchesPrefix("644", "LI", "NIHAO"));
		assertFalse(PinyinFuzzy.matchesPrefix("6442", "", "XIAN"));
		assertFalse(PinyinFuzzy.matchesPrefix("", "", "NIHAO"));
	}
	@Test public void aSelectedSyllablePrefixCanGrowWithoutLosingItsBoundaries() {
		assertTrue(PinyinSyllables.matchesSelectedReading("XIANSHENG先生", "XIAN"));
		assertFalse(PinyinSyllables.matchesSelectedReading("XIANSHENG先生", "XI'AN"));
		assertTrue(PinyinSyllables.matchesSelectedReading("XIANSHENG西安声", "XI'AN"));
	}
	@Test public void disabledKeepsOnlyOriginalSequence() {
		assertEquals(List.of("94"), PinyinFuzzy.sequences("94", "", 0));
		assertFalse(PinyinFuzzy.matches("94", "", "ZHI", 0));
	}
	@Test public void initialsAreBidirectionalAndSyllableValid() {
		assertTrue(PinyinFuzzy.variants("ZI", PinyinFuzzy.Z_ZH).contains("ZHI"));
		assertTrue(PinyinFuzzy.variants("ZHI", PinyinFuzzy.Z_ZH).contains("ZI"));
		assertFalse(PinyinFuzzy.variants("ZHI", PinyinFuzzy.Z_ZH).contains("ZHHI"));
		assertTrue(PinyinFuzzy.sequences("94", "", PinyinFuzzy.Z_ZH).contains("944"));
		assertTrue(PinyinFuzzy.matches("94", "", "ZHI", PinyinFuzzy.Z_ZH));
		assertFalse(PinyinFuzzy.matches("94", "", "XING", PinyinFuzzy.Z_ZH));
	}
	@Test public void optionalRulesDoNotChangeOtherSounds() {
		assertTrue(PinyinFuzzy.matches("54", "", "NI", PinyinFuzzy.NL));
		assertFalse(PinyinFuzzy.matches("54", "", "NI", PinyinFuzzy.IN_ING));
		assertTrue(PinyinFuzzy.matches("936", "", "ZHENG", PinyinFuzzy.Z_ZH | PinyinFuzzy.EN_ENG));
		assertTrue(PinyinFuzzy.matches("546", "", "JING", PinyinFuzzy.IN_ING));
	}
	@Test public void fullPhrasesRespectSyllablesAndSelectedSpelling() {
		assertTrue(PinyinFuzzy.matches("64426", "", "LIHAO", PinyinFuzzy.NL));
		assertFalse(PinyinFuzzy.matches("64426", "NI", "LIHAO", PinyinFuzzy.NL));
		assertTrue(PinyinFuzzy.matches("64426", "NI", "NIHAO", PinyinFuzzy.NL));
	}
	@Test public void queriesAndPathologicalInputAreBounded() {
		assertTrue(PinyinFuzzy.sequences("946426946426946426946426946426", "", 63).size() <= 32);
		assertEquals(List.of("1"), PinyinFuzzy.sequences("1", "", 63));
		assertEquals(List.of("9".repeat(100)), PinyinFuzzy.sequences("9".repeat(100), "", 63));
	}
	@Test public void combinedRulesAreBidirectionalEvenWhenOneIntermediateSyllableIsInvalid() {
		int options = PinyinFuzzy.NL | PinyinFuzzy.EN_ENG;
		assertTrue(PinyinFuzzy.variants("NEN", options).contains("LENG"));
		assertTrue(PinyinFuzzy.variants("LENG", options).contains("NEN"));
		assertFalse(PinyinFuzzy.variants("NEN", options).contains("LEN"));
		assertTrue(PinyinFuzzy.sequences("5364", "", options).contains("636"));
		assertTrue(PinyinFuzzy.matches("5364", "", "NEN", options));
		assertTrue(PinyinFuzzy.matches("636", "", "LENG", options));
	}
}
