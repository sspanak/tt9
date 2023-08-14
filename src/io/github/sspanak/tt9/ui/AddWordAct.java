package io.github.sspanak.tt9.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.exceptions.InsertBlankWordException;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class AddWordAct extends AppCompatActivity {
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
			case 0:
				message = getString(R.string.add_word_success, word);
				break;

			case 1:
				message = getResources().getString(R.string.add_word_exist, word);
				break;

			default:
				message = getString(R.string.error_unexpected);
				break;
			}

		finish();
		sendMessageToMain(message);
	}


	public void addWord(View v) {
		try {
			Logger.d("addWord", "Attempting to add word: '" + word + "'...");
			DictionaryDb.insertWord(this::onAddedWord, language, word);
		} catch (InsertBlankWordException e) {
			Logger.e("AddWordAct.addWord", e.getMessage());
			finish();
			sendMessageToMain(getString(R.string.add_word_blank));
		} catch (InvalidLanguageException e) {
			Logger.e("AddWordAct.addWord", "Cannot insert a word for language: '" + language.getName() + "'. " + e.getMessage());
			finish();
			sendMessageToMain(getString(R.string.add_word_invalid_language));
		} catch (Exception e) {
			Logger.e("AddWordAct.addWord", e.getMessage());
			finish();
			sendMessageToMain(e.getMessage());
		}
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
