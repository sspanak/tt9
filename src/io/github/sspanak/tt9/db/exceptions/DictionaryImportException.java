package io.github.sspanak.tt9.db.exceptions;

public class DictionaryImportException extends Exception {
	public final String word;
	public final long line;

	public DictionaryImportException(String word, long line) {
		super("Dictionary import failed");
		this.word = word;
		this.line = line;
	}
}
