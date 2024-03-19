package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class AddWordDialog extends PopupDialog {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_BLANK_WORD = 1;
	public static final int CODE_INVALID_LANGUAGE = 2;
	public static final int CODE_WORD_EXISTS = 3;
	public static final int CODE_GENERAL_ERROR = 666;

	private Language language;
	private String word;


	public AddWordDialog(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		super(context, intent, activityFinisher);

		title = context.getResources().getString(R.string.add_word_title);
		OKLabel = context.getResources().getString(R.string.add_word_add);
		if (language == null) {
			message = context.getString(R.string.add_word_invalid_language);
		} else {
			message = context.getString(R.string.add_word_confirm, word, language.getName());
		}
	}

	protected void parseIntent(Context context, Intent intent) {
		word = intent.getStringExtra("word");
		language = LanguageCollection.getLanguage(context, intent.getIntExtra("lang", -1));
	}


	public void render() {
		if (message == null || word == null || word.isEmpty()) {
			if (activityFinisher != null) activityFinisher.accept("");
			return;
		}

		Runnable OKAction = language == null ? null : () -> WordStoreAsync.put(this::onAddedWord, language, word);
		super.render(OKAction);
	}

	private void onAddedWord(int statusCode) {
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
}
