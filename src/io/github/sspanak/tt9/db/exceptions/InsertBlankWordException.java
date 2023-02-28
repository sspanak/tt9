package io.github.sspanak.tt9.db.exceptions;

public class InsertBlankWordException extends Exception {
	public InsertBlankWordException() {
		super("Cannot insert a blank word.");
	}
}
