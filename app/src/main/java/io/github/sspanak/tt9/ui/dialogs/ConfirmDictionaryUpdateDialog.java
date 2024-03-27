package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class ConfirmDictionaryUpdateDialog extends PopupDialog {
	public static final String TYPE = "tt9.popup_dialog.confirm_words_update";
	public static final String PARAMETER_LANGUAGE = "lang";

	private Language language;
	public ConfirmDictionaryUpdateDialog(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		super(context, intent, activityFinisher);

		title = context.getString(R.string.app_name);
		OKLabel = context.getString(R.string.dictionary_update_update);
		parseIntent(context, intent);
	}

	protected void parseIntent(@NonNull Context context, @NonNull Intent intent) {
		int languageId = intent.getIntExtra(PARAMETER_LANGUAGE, -1);
		language = LanguageCollection.getLanguage(context, languageId);

		if (language == null) {
			Logger.e(getClass().getSimpleName(), "Failed auto-updating the dictionary for language: '" + languageId + "'");
		} else {
			message = context.getResources().getString(R.string.dictionary_update_message, language.getName());
		}
	}

	@Override
	public void render() {
		if (language == null) {
			close();
		} else {
			super.render(this::loadDictionary);
		}
	}

	private void loadDictionary() {
		DictionaryLoader.load(context, language);
		close();
	}
}
