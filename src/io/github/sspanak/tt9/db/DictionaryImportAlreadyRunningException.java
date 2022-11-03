package io.github.sspanak.tt9.db;

public class DictionaryImportAlreadyRunningException extends Exception{
	public DictionaryImportAlreadyRunningException() {
		super("Dictionary import is already running.");
	}
}
