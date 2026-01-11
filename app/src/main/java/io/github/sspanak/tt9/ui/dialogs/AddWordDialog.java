package io.github.sspanak.tt9.ui.dialogs;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;

public class AddWordDialog extends PopupDialog {
	protected final boolean isWordTooShort;
	@NonNull protected final Language language;
	@NonNull protected final SettingsStore settings;
	@NonNull protected final TraditionalT9 tt9;
	@Nullable protected String word;


	public AddWordDialog(@NonNull TraditionalT9 tt9, @Nullable Language language, @Nullable String word) {
		super(tt9, R.style.TTheme_AddWord);

		this.isWordTooShort = word == null || word.length() < SettingsStore.ADD_WORD_MIN_LENGTH;
		this.language = language == null ? new NullLanguage() : language;
		this.settings = tt9.getSettings();
		this.tt9 = tt9;
		this.word = word;

		setStrings();
	}


	public AddWordDialog(@NonNull TraditionalT9 tt9, @NonNull Intent addWordIntent) {
		this(
			tt9,
			LanguageCollection.getLanguage(addWordIntent.getIntExtra(EditWordDialog.PARAMETER_LANGUAGE, -1)),
			addWordIntent.getStringExtra(EditWordDialog.PARAMETER_WORD)
		);
	}


	protected void setStrings() {
		title = tt9.getString(R.string.add_word_title);
		cancelLabel = tt9.getString(R.string.edit_word_edit);
		neutralLabel = tt9.getString(android.R.string.cancel);
		OKLabel = tt9.getString(R.string.add_word_add);
		message = tt9.getString(R.string.add_word_confirm, word, language.getName());
	}


	private void addWord() {
		close();
		DataStore.put(
			(result) -> UI.toastLongFromAsync(context, result.toHumanFriendlyString(context)),
			language,
			word
		);
	}


	private void showEditDialog() {
		close();
		tt9.startActivity(EditWordDialog.generateShowIntent(tt9, language, word));
	}


	public void show() {
		if (language instanceof NullLanguage) {
			UI.toastShortSingle(context, R.string.add_word_invalid_language);
			close();
			return;
		}


		if (isWordTooShort) {
			UI.toastLong(context, R.string.add_word_no_selection);
			close();
			return;
		}

		if (settings.getAddWordsNoConfirmation()) {
			addWord();
			return;
		}

		if (!render(this::addWord, this::showEditDialog, this::close, null)) {
			addWord();
		}
	}
}
