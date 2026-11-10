package io.github.sspanak.tt9.chinese;

import org.junit.Test;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class CommonHanCharactersTest {
	@Test public void officialCoreKeepsCommonWordsAndRejectsRareCharacters() throws Exception {
		try (InputStream stream = Files.newInputStream(Path.of(System.getProperty("tt9.commonHanTable")))) {
			CommonHanCharacters common = CommonHanCharacters.load(stream);
			assertTrue(common.containsOnlyCommonHan("你好，我们在西安。"));
			assertFalse(common.containsOnlyCommonHan("龘"));
			assertFalse(common.containsOnlyCommonHan("𰻞"));
			assertFalse(common.containsOnlyCommonHan("你好龘"));
		}
	}
	@Test public void punctuationAndLatinAreNotClassifiedAsRareHan() {
		assertTrue(new CommonHanCharacters("你").containsOnlyCommonHan("你A1!?🙂"));
	}
}
