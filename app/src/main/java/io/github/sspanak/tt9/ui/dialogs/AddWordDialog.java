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
import io.github.sspanak.tt9.util.ConsumerCompat;

public class AddWordDialog extends PopupDialog {
	public static final String TYPE = "tt9.popup_dialog.add_word";
	public static final String PARAMETER_LANGUAGE = "lang";
	public static final String PARAMETER_WORD = "word";

	private Language language;
	private String word;


	AddWordDialog(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		super(context, activityFinisher);

		title = context.getResources().getString(R.string.add_word_title);
		OKLabel = context.getResources().getString(R.string.add_word_add);
		parseIntent(context, intent);
	}


	private void parseIntent(@NonNull Context context, @NonNull Intent intent) {
		word = intent.getStringExtra(PARAMETER_WORD);

		int languageId = intent.getIntExtra(PARAMETER_LANGUAGE, -1);
		language = LanguageCollection.getLanguage(context, languageId);

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
