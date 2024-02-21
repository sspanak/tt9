package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class ConfirmDictionaryUpdateDialog extends PopupDialog {
	private Language language;
	public ConfirmDictionaryUpdateDialog(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		super(context, intent, activityFinisher);

		title = context.getString(R.string.app_name);
		OKLabel = context.getString(R.string.dictionary_update_update);
		String langName = language != null ? language.getName() : "";
		message = context.getResources().getString(R.string.dictionary_update_message, langName);
	}

	protected void parseIntent(Context context, Intent intent) {
		language = LanguageCollection.getLanguage(context, intent.getIntExtra("lang", -1));
	}

	@Override
	public void render() {
		super.render(this::loadDictionary);
	}

	private void loadDictionary() {
		DictionaryLoader.load(context, language);
		activityFinisher.accept("");
	}
}
