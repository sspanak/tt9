package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.entities.AddWordResult;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.ThemedContextBuilder;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class AddWordDialog extends PopupDialog {
	public static final String TYPE = "tt9.popup_dialog.add_word";
	public static final String PARAMETER_LANGUAGE = "lang";
	public static final String PARAMETER_WORD = "word";

	private Language language;
	private String word;


	AddWordDialog(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		super(
			new ThemedContextBuilder()
				.setConfiguration(context.getApplicationContext().getResources().getConfiguration())
				.setContext(context)
				.setSettings(new SettingsStore(context))
				// The main theme does not work on Android <= 11 and the _AddWord theme does not work on 12+.
				// Not sure why since they inherit from the same parent, but it is what it is.
				.setTheme(DeviceInfo.AT_LEAST_ANDROID_12 ? R.style.TTheme : R.style.TTheme_AddWord)
				.build(),
			activityFinisher
		);

		title = context.getResources().getString(R.string.add_word_title);
		OKLabel = context.getResources().getString(R.string.add_word_add);
		parseIntent(context, intent);
	}


	private void parseIntent(@NonNull Context context, @NonNull Intent intent) {
		word = intent.getStringExtra(PARAMETER_WORD);

		int languageId = intent.getIntExtra(PARAMETER_LANGUAGE, -1);
		language = LanguageCollection.getLanguage(languageId);

		if (language == null) {
			message = context.getString(R.string.add_word_invalid_language_x, languageId);
		} else {
			message = context.getString(R.string.add_word_confirm, word, language.getName());
		}
	}


	private void onOK() {
		if (language != null) {
			DataStore.put(this::onAddingFinished, language, word);
		}
	}


	private void onAddingFinished(AddWordResult addingResult) {
		activityFinisher.accept(addingResult.toHumanFriendlyString(context));
	}


	@Override
	void render() {
		if (message == null || word == null || word.isEmpty()) {
			close();
			return;
		}

		super.render(this::onOK);
	}


	public static void show(InputMethodService ims, int language, String currentWord) {
		Intent intent = new Intent(ims, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra(PARAMETER_DIALOG_TYPE, TYPE);
		intent.putExtra(PARAMETER_LANGUAGE, language);
		intent.putExtra(PARAMETER_WORD, currentWord);
		ims.startActivity(intent);
	}
}
