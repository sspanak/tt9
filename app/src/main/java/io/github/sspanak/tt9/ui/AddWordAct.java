package io.github.sspanak.tt9.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class AddWordAct extends AppCompatActivity {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_BLANK_WORD = 1;
	public static final int CODE_INVALID_LANGUAGE = 2;
	public static final int CODE_WORD_EXISTS = 3;
	public static final int CODE_GENERAL_ERROR = 666;

	public static final String INTENT_FILTER = "tt9.add_word";

	private Language language;
	private String word;

	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		readInput();
		render(getMessage());
	}


	private void readInput() {
		Intent i = getIntent();
		word = i.getStringExtra("io.github.sspanak.tt9.word");
		language = LanguageCollection.getLanguage(this, i.getIntExtra("io.github.sspanak.tt9.lang", -1));
	}

	private String getMessage() {
		if (language == null) {
			Logger.e("WordManager.confirmAddWord", "Cannot insert a word for NULL language");
			UI.toastLong(getApplicationContext(), R.string.add_word_invalid_language);
			return null;
		}

		return getString(R.string.add_word_confirm, word, language.getName());
	}

	private void render(String message) {
		if (message == null || word == null || word.isEmpty()) {
			finish();
			return;
		}

		View main = View.inflate(this, R.layout.addwordview, null);
		((TextView) main.findViewById(R.id.add_word_dialog_text)).append(message);
		setContentView(main);
	}


	private void onAddedWord(int statusCode) {
		String message;
		switch (statusCode) {
			case CODE_SUCCESS:
				message = getString(R.string.add_word_success, word);
				break;

			case CODE_WORD_EXISTS:
				message = getResources().getString(R.string.add_word_exist, word);
				break;

			case CODE_BLANK_WORD:
				message = getString(R.string.add_word_blank);
				break;

			case CODE_INVALID_LANGUAGE:
				message = getResources().getString(R.string.add_word_invalid_language);
				break;

			default:
				message = getString(R.string.error_unexpected);
				break;
			}

		finish();
		sendMessageToMain(message);
	}


	public void addWord(View v) {
		WordStoreAsync.put(this::onAddedWord, language, word);
	}


	private void sendMessageToMain(String message) {
		Intent intent = new Intent(this, TraditionalT9.class);
		intent.putExtra(INTENT_FILTER, message);
		startService(intent);
	}


	public void cancelAddingWord(View v) {
		finish();
	}
}
