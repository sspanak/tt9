package io.github.sspanak.tt9.ui.dialogs;

import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.PopupBuilder;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.main.MainView;

public class AddWordDialog extends ThemedPopupDialog {
	@Nullable private final MainView mainView;
	@NonNull private final Language language;
	@NonNull private final SettingsStore settings;
	@Nullable private final String word;

	private Dialog popup;


	public AddWordDialog(@NonNull TraditionalT9 tt9, @NonNull Language language, @Nullable String word) {
		super(tt9, null, R.style.TTheme_AddWord);
		mainView = tt9.getMainView();

		title = tt9.getResources().getString(R.string.add_word_title);
		OKLabel = tt9.getResources().getString(R.string.add_word_add);
		message = tt9.getString(R.string.add_word_confirm, word, language.getName());
		settings = tt9.getSettings();

		this.language = language;
		this.word = word;
	}


	@Override
	protected void close() {
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}
	}


	private void onOK() {
		close();
		DataStore.put(
			(result) -> UI.toastLongFromAsync(context, result.toHumanFriendlyString(context)),
			language,
			word
		);
	}


	@Override
	public void show() {
		if (word == null || word.isEmpty()) {
			UI.toastLong(context, R.string.add_word_no_selection);
			close();
			return;
		}

		if (settings.getAddWordsNoConfirmation()) {
			onOK();
			return;
		}

		popup = new PopupBuilder(context)
			.setCancelable(true)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(OKLabel, this::onOK)
			.setNegativeButton(true, null)
			.setOnKeyListener(this)
			.showFromIme(mainView);
	}
}
