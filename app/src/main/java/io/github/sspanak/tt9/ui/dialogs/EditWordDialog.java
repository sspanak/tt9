package io.github.sspanak.tt9.ui.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.EdgeToEdgeActivity;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;

public class EditWordDialog extends EdgeToEdgeActivity {
	private static final String LOG_TAG = EditWordDialog.class.getSimpleName();

	public static final String PARAMETER_LANGUAGE = "language";
	public static final String PARAMETER_WORD = "word";

	private TextView stringALabel;
	private TextView stringBLabel;
	private EditWordDialogLetterEditor currentLetterInput;

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


	private void createView() {
		// @todo: styles
		setContentView(R.layout.popup_edit_word);

		final View title = findViewById(R.id.edit_word_title);
		if (title instanceof TextView) {
			((TextView) title).setText("Editing \"" + word + "\""); // @todo: localize + add instructions
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

		if (currentLetterInput != null) {
			currentLetterInput
				.setOnArrowLeftListener(this::onLeft)
				.setOnArrowRightListener(this::onRightArrow)
				.setOnBackspaceListener(this::onLeft)
				.setOnOKListener(this::onOK)
				.setLanguage(language);
		}
	}


	private void onLeft() {
		if (word != null && currentLetterInput != null && position > 0) {
			edit(--position, getWordLetter(position));
		}
	}


	private void onRight(boolean fromOK) {
		if (word == null || currentLetterInput == null) {
			return;
		}

		if (position < word.length() - 1) {
			edit(position, getCurrentLetter());
			edit(++position, getWordLetter(position));
		} else if (fromOK && position == word.length() - 1) {
			edit(position, getCurrentLetter());
			showAddDialog(currentLetterInput);
		}
	}


	private void onRightArrow() {
		onRight(false);
	}


	private void onOK() {
		onRight(true);
	}


	private void edit(int editPosition, @NonNull String newLetter) {
		if (word == null || isWordTooShort) {
			Logger.d(LOG_TAG, "Word '" + word + "' is too short to edit.");
			return;
		}

		if (editPosition < 0 || editPosition >= word.length()) {
			Logger.d(LOG_TAG, "Position " + editPosition + " is out of bounds for word '" + word + "'.");
			return;
		}

		if (newLetter.length() != 1) {
			Logger.d(LOG_TAG, "New letter '" + newLetter + "' is not a single character.");
			return;
		}

		String stringA = word.substring(0, editPosition);
		String stringB = word.substring(editPosition + 1);

		if (stringALabel != null) {
			stringALabel.setText(stringA.isEmpty() ? null : stringA + " + ");
		}

		if (currentLetterInput != null) {
			currentLetterInput.setText(newLetter);
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

		final CharSequence text = currentLetterInput.getText();
		return text == null ? "" : text.toString();
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
		Intent intent = new Intent(this, TraditionalT9.class);
		intent.putExtra(UI.COMMAND, UI.COMMAND_ADD_WORD);
		intent.putExtra(PARAMETER_LANGUAGE, language != null ? language.getId() : -1);
		intent.putExtra(PARAMETER_WORD, word);
		startService(intent);

		finish();
	}


	public static Intent generateShowIntent(@NonNull TraditionalT9 tt9, @NonNull Language language, @Nullable String word) {
		Intent intent = new Intent(tt9.getApplicationContext(), EditWordDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(PARAMETER_LANGUAGE, language.getId());
		intent.putExtra(PARAMETER_WORD, word);

		return intent;
	}
}
