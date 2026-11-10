package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import static org.junit.Assert.*;

public class ChineseUserWordTest {
	@Test public void createsCanonicalStorageAndReadableExport() {
		ChineseUserWord word = ChineseUserWord.create("西安", "xi an");
		assertEquals("XIAN西安", word.storedWord());
		assertEquals("9426", word.sequence());
		assertEquals("西安\t462106\tXI AN", word.exportLine());
	}

	@Test public void polyphonicEntriesStayDistinct() {
		ChineseUserWord chong = ChineseUserWord.create("重庆", "chong qing");
		ChineseUserWord zhong = ChineseUserWord.create("重庆", "zhong qing");
		assertNotEquals(chong.storedWord(), zhong.storedWord());
		assertNotEquals(chong.sequence(), zhong.sequence());
		assertEquals("重庆", chong.word());
	}

	@Test public void storedReadingRoundTripsWithoutChangingLetters() {
		ChineseUserWord word = ChineseUserWord.fromStored("CHANGAN长安");
		assertEquals("CHANGAN长安", ChineseUserWord.create(word.word(), word.reading()).storedWord());
	}

	@Test public void countsSupplementaryHanAsOneCharacter() {
		assertEquals("JI𠮷", ChineseUserWord.create("𠮷", "ji").storedWord());
	}

	@Test(expected = IllegalArgumentException.class) public void rejectsMixedWordText() { ChineseUserWord.create("西安abc", "xi an"); }
	@Test(expected = IllegalArgumentException.class) public void rejectsPunctuationInWord() { ChineseUserWord.create("西安！", "xi an"); }
	@Test(expected = IllegalArgumentException.class) public void requiresReadingForEachHan() { ChineseUserWord.create("西安", "xi"); }
	@Test(expected = IllegalArgumentException.class) public void refusesMalformedStoredWord() { ChineseUserWord.fromStored("西安"); }
}
