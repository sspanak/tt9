package io.github.sspanak.tt9.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.InsertBlankWordException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.T9Preferences;

public class AddWordAct extends Activity {

	View main;
	int lang;
	String word;

	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		Intent i = getIntent();
		word = i.getStringExtra("io.github.sspanak.tt9.word");
		lang = i.getIntExtra("io.github.sspanak.tt9.lang", -1);

		View v = getLayoutInflater().inflate(R.layout.addwordview, null);

		EditText et = (EditText) v.findViewById(R.id.add_word_text);
		et.setText(word);
		et.setSelection(word.length());
		setContentView(v);
		main = v;
	}


	private final Handler onAddedWord = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					Logger.d("onAddedWord", "Added word: '" + word + "'...");
					T9Preferences.getInstance(main.getContext()).saveLastWord(word);
					break;

				case 1:
					UI.toastLong(
						main.getContext(),
						getResources().getString(R.string.add_word_exist, word)
					);
					break;

				default:
					UI.toastLong(main.getContext(), R.string.error_unexpected);
					break;
			}

			finish();
		}
	};

	public void addWord(View v) {
		try {
			// re-fetch the word, in case the user has changed it after the initialization
			word = ((EditText) main.findViewById(R.id.add_word_text)).getText().toString();
			Logger.d("addWord", "Attempting to add word: '" + word + "'...");

			DictionaryDb.insertWord(this, onAddedWord, LanguageCollection.getLanguage(lang), word);
		} catch (InsertBlankWordException e) {
			Logger.e("AddWordAct.addWord", e.getMessage());
			UI.toastLong(this, R.string.add_word_blank);
		} catch (InvalidLanguageException e) {
			Logger.e("AddWordAct.addWord", e.getMessage() + ". Language ID: " + lang);
			UI.toastLong(this, R.string.add_word_invalid_language);
		} catch (Exception e) {
			Logger.e("AddWordAct.addWord", e.getMessage());
			UI.toastLong(this, e.getMessage());
		}
	}


	public void cancelAddingWord(View v) {
		this.finish();
	}
}
