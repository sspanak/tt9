package io.github.sspanak.tt9.ui.dialogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;

public class AddWordDialog extends PopupDialog {
	@NonNull private final Language language;
	@NonNull private final SettingsStore settings;
	@Nullable private final String word;


	public AddWordDialog(@NonNull TraditionalT9 tt9, @NonNull Language language, @Nullable String word) {
		super(tt9, R.style.TTheme_AddWord);

		title = tt9.getResources().getString(R.string.add_word_title);
		cancelLabel = tt9.getString(R.string.add_word_edit);
		neutralLabel = tt9.getString(android.R.string.cancel);
		OKLabel = tt9.getResources().getString(R.string.add_word_add);
		message = tt9.getString(R.string.add_word_confirm, word, language.getName());
		settings = tt9.getSettings();

		this.language = language;
		this.word = word;
	}

	private void addWord() {
		close();
		DataStore.put(
			(result) -> UI.toastLongFromAsync(context, result.toHumanFriendlyString(context)),
			language,
			word
		);
	}


	private void editWord() {
		if (popup != null) {
			UI.sendCommandToMain(popup.getContext(), UI.COMMAND_EDIT_WORD);
		}
	}


	public void show() {
		if (word == null || word.isEmpty()) {
			UI.toastLong(context, R.string.add_word_no_selection);
			close();
			return;
		}

		if (settings.getAddWordsNoConfirmation()) {
			addWord();
			return;
		}

		if (!render(this::addWord, this::editWord, this::close, null)) {
			addWord();
		}
	}
}
