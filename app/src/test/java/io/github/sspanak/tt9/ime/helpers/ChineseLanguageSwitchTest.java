package io.github.sspanak.tt9.ime.helpers;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChineseLanguageSwitchTest {
	@Test public void commitsOnlyAnActiveHanCandidateWhenLeavingChinese() {
		assertTrue(ChineseLanguageSwitch.shouldCommit(true, true, true, true, false, "你好"));
		assertTrue(ChineseLanguageSwitch.shouldCommit(true, true, true, true, false, "𠮷"));
		assertFalse(ChineseLanguageSwitch.shouldCommit(true, true, false, true, false, "你好"));
		assertFalse(ChineseLanguageSwitch.shouldCommit(true, false, true, true, false, "你好"));
		assertFalse(ChineseLanguageSwitch.shouldCommit(false, true, true, true, false, "你好"));
		assertFalse(ChineseLanguageSwitch.shouldCommit(true, true, true, false, false, "你好"));
	}

	@Test public void spellingEditingAndNonHanCandidatesAreNeverCommitted() {
		assertFalse(ChineseLanguageSwitch.shouldCommit(true, true, true, true, true, "你好"));
		for (String candidate : new String[]{null, "", "NI", "XI'AN", "N|I", "12", "😀", "你好!", "NI你"}) {
			assertFalse(ChineseLanguageSwitch.shouldCommit(true, true, true, true, false, candidate));
		}
	}
}
