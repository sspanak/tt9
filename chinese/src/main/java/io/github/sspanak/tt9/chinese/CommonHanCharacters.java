package io.github.sspanak.tt9.chinese;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/** The 3,500 level-one characters in the 2013 national table, sourced through Unicode kTGH. */
public final class CommonHanCharacters {
	public static final String FILE_NAME = "common-3500.txt";
	private final BitSet characters;

	public CommonHanCharacters(String text) {
		characters = new BitSet();
		text.codePoints().filter(code -> !Character.isWhitespace(code)).forEach(characters::set);
	}

	public static CommonHanCharacters load(InputStream stream) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(10500);
		byte[] buffer = new byte[4096];
		for (int size; (size = stream.read(buffer)) >= 0;) bytes.write(buffer, 0, size);
		CommonHanCharacters result = new CommonHanCharacters(bytes.toString(StandardCharsets.UTF_8.name()));
		if (result.characters.cardinality() != 3500) throw new IOException("Invalid common-character table");
		return result;
	}

	public boolean containsOnlyCommonHan(String word) {
		return word.codePoints().allMatch(code -> Character.UnicodeScript.of(code) != Character.UnicodeScript.HAN || characters.get(code));
	}
}
