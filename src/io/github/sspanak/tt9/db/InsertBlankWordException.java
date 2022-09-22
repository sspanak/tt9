package io.github.sspanak.tt9.db;

public class InsertBlankWordException extends Exception {
	protected InsertBlankWordException() {
		super("Cannot insert a blank word.");
	}
}
