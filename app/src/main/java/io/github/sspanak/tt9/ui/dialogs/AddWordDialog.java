package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.ConsumerCompat;

public class AddWordDialog extends PopupDialog {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_BLANK_WORD = 1;
	public static final int CODE_INVALID_LANGUAGE = 2;
	public static final int CODE_WORD_EXISTS = 3;
	public static final int CODE_GENERAL_ERROR = 666;

	public static final String TYPE = "tt9.popup_dialog.add_word";
	public static final String PARAMETER_LANGUAGE = "lang";
	public static final String PARAMETER_WORD = "word";

	private Language language;
	private String word;


	public AddWordDialog(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		super(context, intent, activityFinisher);

		title = context.getResources().getString(R.string.add_word_title);
		OKLabel = context.getResources().getString(R.string.add_word_add);
		parseIntent(context, intent);
	}


	protected void parseIntent(@NonNull Context context, @NonNull Intent intent) {
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
			WordStoreAsync.put(this::onAddingFinished, language, word);
		}
	}


	private void onAddingFinished(int statusCode) {
		String response;
		switch (statusCode) {
			case CODE_SUCCESS:
				response = context.getString(R.string.add_word_success, word);
				break;

			case CODE_WORD_EXISTS:
				response = context.getResources().getString(R.string.add_word_exist, word);
				break;

			case CODE_BLANK_WORD:
				response = context.getString(R.string.add_word_blank);
				break;

			case CODE_INVALID_LANGUAGE:
				response = context.getResources().getString(R.string.add_word_invalid_language);
				break;

			default:
				response = context.getString(R.string.error_unexpected);
				break;
			}

		activityFinisher.accept(response);
	}


	public void render() {
		if (message == null || word == null || word.isEmpty()) {
			close();
			return;
		}

		super.render(this::onOK);
	}
}
