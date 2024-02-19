package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class ConfirmDictionaryUpdateDialog extends PopupDialog {
	private static long lastDisplayTime = 0;
	private Language language;
	public ConfirmDictionaryUpdateDialog(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		super(context, intent, activityFinisher);

		title = context.getString(R.string.dictionary_update_title);
		OKLabel = context.getString(R.string.dictionary_update_update);
		String langName = language != null ? language.getName() : "";
		message = context.getResources().getString(R.string.dictionary_update_message, langName);
	}

	protected void parseIntent(Context context, Intent intent) {
		language = LanguageCollection.getLanguage(context, intent.getIntExtra("lang", -1));
	}

	@Override
	public void render() {
		if (System.currentTimeMillis() - lastDisplayTime < SettingsStore.DICTIONARY_CONFIRM_UPDATE_COOLDOWN_TIME) {
			activityFinisher.accept(null);
		} else {
			super.render(this::loadDictionary);
			lastDisplayTime = System.currentTimeMillis();
		}
	}

	private void loadDictionary() {
		DictionaryLoader.load(context, language);
		activityFinisher.accept(null);
	}
}
