package io.github.sspanak.tt9.db.exceptions;

public class DictionaryImportException extends Exception {
	public final String file;
	public final String word;
	public final long line;

	public DictionaryImportException(String file, String word, long line) {
		super("Dictionary import failed");
		this.file = file;
		this.word = word;
		this.line = line;
	}
}
