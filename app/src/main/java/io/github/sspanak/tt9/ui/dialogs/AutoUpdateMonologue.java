package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class AutoUpdateMonologue extends PopupDialog {
	public static final String TYPE = "tt9.popup_dialog.confirm_words_update";
	public static final String PARAMETER_LANGUAGE = "lang";

	private Language language;


	AutoUpdateMonologue(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		super(context, activityFinisher);
		parseIntent(intent);
	}


	private void parseIntent(@NonNull Intent intent) {
		int languageId = intent.getIntExtra(PARAMETER_LANGUAGE, -1);
		language = LanguageCollection.getLanguage(languageId);

		if (language == null) {
			Logger.e(getClass().getSimpleName(), "Auto-updating is not possible. Intent parameter '" + PARAMETER_LANGUAGE + "' is invalid: " + languageId);
		}
	}


	@Override
	void render() {
		if (language != null) {
			DictionaryLoader.load(context, language);
		}
		close();
	}


	public static Intent generateShowIntent(Context context, int language) {
		Intent intent = new Intent(context, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(PARAMETER_DIALOG_TYPE, TYPE);
		intent.putExtra(PARAMETER_LANGUAGE, language);

		return intent;
	}
}
