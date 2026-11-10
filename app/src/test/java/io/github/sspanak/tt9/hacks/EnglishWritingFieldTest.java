package io.github.sspanak.tt9.hacks;

import android.view.inputmethod.EditorInfo;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnglishWritingFieldTest {
	@Test public void acceptsOrdinaryTextAndMessageEditors() {
		for (int variation : new int[]{EditorInfo.TYPE_TEXT_VARIATION_NORMAL, EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE,
			EditorInfo.TYPE_TEXT_VARIATION_LONG_MESSAGE, EditorInfo.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT,
			EditorInfo.TYPE_TEXT_VARIATION_EMAIL_SUBJECT}) {
			assertTrue(field(EditorInfo.TYPE_CLASS_TEXT | variation).allowsWritingSuggestions());
		}
		assertTrue(field(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE).allowsWritingSuggestions());
	}

	@Test public void rejectsIdentifiersPasswordsNamesAddressesAndNoSuggestionEditors() {
		for (int variation : new int[]{EditorInfo.TYPE_TEXT_VARIATION_URI, EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
			EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS, EditorInfo.TYPE_TEXT_VARIATION_PASSWORD,
			EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD,
			EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME, EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
			EditorInfo.TYPE_TEXT_VARIATION_FILTER, EditorInfo.TYPE_TEXT_VARIATION_PHONETIC}) {
			assertFalse(field(EditorInfo.TYPE_CLASS_TEXT | variation).allowsWritingSuggestions());
		}
		assertFalse(field(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS).allowsWritingSuggestions());
		assertFalse(field(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS).allowsWritingSuggestions());
	}

	@Test public void rejectsMissingNumericDatePhoneAndLimitedEditors() {
		for (int type : new int[]{EditorInfo.TYPE_CLASS_NUMBER, EditorInfo.TYPE_CLASS_PHONE, EditorInfo.TYPE_CLASS_DATETIME,
			EditorInfo.TYPE_NULL}) assertFalse(field(type).allowsWritingSuggestions());
		assertFalse(new InputType(null, null).allowsWritingSuggestions());
	}

	private static InputType field(int type) {
		EditorInfo editor = new EditorInfo();
		editor.packageName = "example.writer";
		editor.inputType = type;
		return new InputType(null, editor);
	}
}
