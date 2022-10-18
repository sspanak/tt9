package io.github.sspanak.tt9.db;

public class DictionaryImportException extends Exception {
	public final String file;
	public final String word;
	public final long line;

	DictionaryImportException(String file, String word, long line) {
		super("Dictionary import failed");
		this.file = file;
		this.word = word;
		this.line = line;
	}
}
