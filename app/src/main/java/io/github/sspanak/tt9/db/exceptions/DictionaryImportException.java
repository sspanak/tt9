package io.github.sspanak.tt9.db.exceptions;

public class DictionaryImportException extends Exception {
	public final long line;

	public DictionaryImportException(long line) {
		super("Dictionary import failed");
		this.line = line;
	}
}
