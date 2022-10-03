package io.github.sspanak.tt9.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.T9Preferences;

public class AddWordAct extends Activity {

	View main;
	int lang;
	String origword;

	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		View v = getLayoutInflater().inflate(R.layout.addwordview, null);
		EditText et = (EditText) v.findViewById(R.id.add_word_text);
		Intent i = getIntent();
		origword = i.getStringExtra("io.github.sspanak.tt9.word");

		lang = i.getIntExtra("io.github.sspanak.tt9.lang", -1);
		if (lang == -1) {
			Logger.e("AddWordAct.onCreate", "lang is invalid. How?");
		}
		// Logger.d("AddWord", "data.get: " + word);
		et.setText(origword);
		et.setSelection(origword.length());
		setContentView(v);
		main = v;
	}

	public void addWordButton(View v) {
		EditText et = (EditText) main.findViewById(R.id.add_word_text);
		// Logger.d("AddWordAct", "adding word: " + et.getText());
		doAddWord(et.getText().toString());
		this.finish();
	}

	public void doAddWord(String text) {
		try {
			DictionaryDb.insertWord(this, text, LanguageCollection.getLanguage(lang).getId());
		} catch (Exception e) {
			UI.toast(this, e.getMessage());
			return;
		}
		T9Preferences.getInstance(this).saveLastWord(text);
	}


	public void cancelButton(View v) {
		// Logger.d("AddWordAct", "Cancelled...");
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_word, menu);
		return true;
	}

}
