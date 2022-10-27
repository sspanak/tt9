package io.github.sspanak.tt9.db;

public class DictionaryImportAbortedException extends Exception{
	public DictionaryImportAbortedException() {
		super("Dictionary import stopped by request.");
	}
}
