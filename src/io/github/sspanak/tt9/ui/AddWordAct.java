package io.github.sspanak.tt9.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.InsertBlankWordException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class AddWordAct extends AppCompatActivity {

	private View main;
	private int lang;
	private String word;

	@Override
	protected void onCreate(Bundle savedData) {
		AppCompatDelegate.setDefaultNightMode(
			SettingsStore.getInstance().getDarkTheme() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
		);

		super.onCreate(savedData);

		Intent i = getIntent();
		word = i.getStringExtra("io.github.sspanak.tt9.word");
		lang = i.getIntExtra("io.github.sspanak.tt9.lang", -1);

		View v = getLayoutInflater().inflate(R.layout.addwordview, null);

		EditText et = v.findViewById(R.id.add_word_text);
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
					SettingsStore.getInstance().saveLastWord(word);
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

			DictionaryDb.insertWord(onAddedWord, LanguageCollection.getLanguage(lang), word);
		} catch (InsertBlankWordException e) {
			Logger.e("AddWordAct.addWord", e.getMessage());
			UI.toastLong(this, R.string.add_word_blank);
		} catch (InvalidLanguageException e) {
			Logger.e("AddWordAct.addWord", "Cannot insert a word for language with ID: '" + lang + "'. " + e.getMessage());
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
