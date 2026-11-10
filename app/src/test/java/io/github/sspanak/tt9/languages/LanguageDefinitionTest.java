package io.github.sspanak.tt9.languages;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LanguageDefinitionTest {
	private static String pinyinDefinition;


	@BeforeClass
	public static void readDefinition() throws IOException {
		String dictionary = System.getProperty("tt9.chineseDictionary");
		assertNotNull(dictionary);
		pinyinDefinition = Files.readString(Paths.get(dictionary).getParent().getParent()
			.resolve("definitions/ChineseSimplifiedPinyin.yml")).replace("\r\n", "\n");
	}


	@Test
	public void pinyinLayoutLoadsWithUnixLineEndings() throws Exception {
		assertPinyinLayout(pinyinDefinition);
	}


	@Test
	public void pinyinLayoutLoadsWithWindowsLineEndings() throws Exception {
		assertPinyinLayout(pinyinDefinition.replace("\n", "\r\n"));
	}


	@Test
	public void pinyinLayoutLoadsWithMixedGeneratedLineEndings() throws Exception {
		assertPinyinLayout(pinyinDefinition.replace("layout:\n", "layout:\r\n"));
	}


	@Test
	public void windowsSeparatorsKeepLanguageDefinitionsSeparate() {
		String definitions = pinyinDefinition.replace("\n", "\r\n") + "\r\n---\r\nlocale: en\r\ndictionaryFile: en-utf8.csv\r\n";
		ArrayList<LanguageDefinition> parsed = LanguageDefinition.parseDefinitions(definitions.split("\n"));
		assertEquals(2, parsed.size());
		assertEquals("zh-CN", parsed.get(0).locale);
		assertEquals("en", parsed.get(1).locale);
		assertEquals(10, parsed.get(0).layout.size());
		assertTrue(parsed.get(1).layout.isEmpty());
	}


	private void assertPinyinLayout(String definition) throws Exception {
		ArrayList<LanguageDefinition> parsed = LanguageDefinition.parseDefinitions(definition.split("\n"));
		assertEquals(1, parsed.size());
		NaturalLanguage language = NaturalLanguage.fromDefinition(parsed.get(0));
		String[] keyLetters = {"abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
		for (int key = 2; key <= 9; key++) {
			ArrayList<String> expected = new ArrayList<>(Arrays.asList(keyLetters[key - 2].split("")));
			assertEquals("Key " + key, expected, language.getKeyCharacters(key));
			language.updateKeyCharacters(key, "", false);
			assertEquals("Updated key " + key, expected, language.getKeyCharacters(key));
		}
		assertEquals(462106, language.getId());
		assertFalse(language.hasABC());
		assertTrue(language.hasTranscriptionsEmbedded());
		assertTrue(language.getKeyCharacters(1).contains("。"));
	}
}
