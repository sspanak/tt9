package io.github.sspanak.tt9.db.exceptions;

public class DictionaryImportAlreadyRunningException extends Exception{
	public DictionaryImportAlreadyRunningException() {
		super("Dictionary import is already running.");
	}
}
