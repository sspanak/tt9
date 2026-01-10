package io.github.sspanak.tt9.ui.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.EdgeToEdgeActivity;
import io.github.sspanak.tt9.util.Logger;

public class EditWordDialog extends EdgeToEdgeActivity {
	private static final String LOG_TAG = EditWordDialog.class.getSimpleName();

	private static final String PARAMETER_LANGUAGE = "language";
	private static final String PARAMETER_WORD = "word";

	private TextView stringALabel;
	private TextView stringBLabel;
	private EditText currentLetterInput;

	private boolean isWordTooShort;
	@Nullable private Language language;
	@Nullable private String word;
	private int position;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		processIntent(getIntent());
		createView();
		edit(position, getWordLetter(position));
	}


	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		preventEdgeToEdge(findViewById(R.id.tutorial_popup_container));
	}


	private void processIntent(@NonNull Intent intent) {
		LanguageCollection.init(this);
		final int languageId = intent.getIntExtra(PARAMETER_LANGUAGE, -1);
		language = LanguageCollection.getLanguage(languageId);
		if (language == null) {
			Logger.e(getClass().getSimpleName(), "Cannot edit word. Invalid language: " + languageId);
		}

		word = intent.getStringExtra(PARAMETER_WORD);
		if (word == null) {
			Logger.e(getClass().getSimpleName(), "Cannot edit a null word.");
			word = null;
		}

		isWordTooShort = word == null || word.length() < SettingsStore.ADD_WORD_MIN_LENGTH;
		position = 0;
	}


	//	public EditWordDialog(@NonNull TraditionalT9 tt9, @NonNull Language language, @Nullable String word) {
//		super(tt9, language, word);
//		originalWord = word;
//		position = 0;
//	}
//
//
//	@Override
//	protected void setStrings() {
//		cancelLabel = tt9.getString(android.R.string.cancel);
//		OKLabel = tt9.getString(R.string.add_word_add);
//	}


	private void createView() {
		setContentView(R.layout.popup_edit_word);

		final View title = findViewById(R.id.edit_word_title);
		if (title instanceof TextView) {
			((TextView) title).setText("Editing \"" + word + "\"");
		}

		final View cancel = findViewById(R.id.edit_word_cancel_button);
		if (cancel != null) {
			cancel.setOnClickListener((v) -> finish());
		}

		final View ok = findViewById(R.id.edit_word_ok_button);
		if (ok != null) {
			ok.setOnClickListener(this::showAddDialog);
		}

		stringALabel = findViewById(R.id.edit_word_string_a);
		stringBLabel = findViewById(R.id.edit_word_string_b);
		currentLetterInput = findViewById(R.id.edit_word_current_letter);
	}


	//	@Override
//	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//		if (Key.isArrow(keyCode)) {
//			return super.onKey(dialog, keyCode, event);
//		}
//
//		if (event.getAction() == KeyEvent.ACTION_DOWN) {
//			return true;
//		}
//
//		if (Key.isBack(keyCode)) {
//			close();
//			return true;
//		}
//
//		if (Key.isBackspace(tt9.getSettings(), keyCode)) {
//			if (position > 0) {
//				position--;
//				edit(position, getWordLetter(position));
//			}
//			return true;
//		}
//
//		if (Key.isOK(keyCode)) {
//			if (word != null && currentLetterInput != null && position < word.length()) {
//				edit(position, getCurrentLetter());
//				edit(++position, getWordLetter(position));
//			}
//			return true;
//		}
//
//
//		if (Key.isNumber(keyCode)) {
//			if (currentLetterInput != null) {
//				currentLetterInput.setSelection(0, currentLetterInput.getText().length());
////				currentLetterInput.setText("" + Key.codeToNumber(tt9.getSettings(), keyCode));
//
//				tt9.onKeyDown(keyCode, event);
//				tt9.onKeyUp(keyCode, event);
//			}
//
//		}
//
//		return true;
//	}
//
//
	private void edit(int position, @NonNull String newLetter) {
		if (word == null || isWordTooShort) {
			Logger.d(LOG_TAG, "Word '" + word + "' is too short to edit.");
			return;
		}

		if (position < 0 || position >= word.length()) {
			Logger.d(LOG_TAG, "Position " + position + " is out of bounds for word '" + word + "'.");
			return;
		}

		if (newLetter.length() != 1) {
			Logger.d(LOG_TAG, "New letter '" + newLetter + "' is not a single character.");
			return;
		}

		String stringA = word.substring(0, position);
		String stringB = word.substring(position + 1);

		if (stringALabel != null) {
			stringALabel.setText(stringA.isEmpty() ? null : stringA + " + ");
		}

		if (currentLetterInput != null) {
			currentLetterInput.setText(newLetter);
			currentLetterInput.setSelection(currentLetterInput.getText().length());
		}

		if (stringBLabel != null) {
			stringBLabel.setText(stringB.isEmpty() ? null : " + " + stringB);
		}

		word = stringA + newLetter + stringB;
	}


	private String getCurrentLetter() {
		if (currentLetterInput == null) {
			return "";
		}
		return currentLetterInput.getText().toString();
	}


	private String getWordLetter(int position) {
		if (word == null || isWordTooShort) {
			return "";
		}

		if (position < 0 || position >= word.length()) {
			return "";
		}

		return String.valueOf(word.charAt(position));
	}


	private void showAddDialog(View v) {
		// @todo: set command to main to add word
		finish();
	}


//	public void show() {
//		if (isWordTooShort) {
//			UI.toastLong(context, R.string.add_word_no_selection);
//			close();
//			return;
//		}
//
//		createView();
//		edit(position, getWordLetter(position));
//		render(this::showAddDialog, this::close, null, body);
//	}



	public static Intent generateShowIntent(@NonNull TraditionalT9 tt9, @NonNull Language language, @Nullable String word) {
		Intent intent = new Intent(tt9.getApplicationContext(), EditWordDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(PARAMETER_LANGUAGE, language.getId());
		intent.putExtra(PARAMETER_WORD, word);

		return intent;
	}
}
