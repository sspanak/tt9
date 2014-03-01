package org.nyanya.android.traditionalt9;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class AddWordAct extends Activity {

	View main;
	int lang;
	SharedPreferences pref;
	String origword;

	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		View v = getLayoutInflater().inflate(R.layout.addwordview, null);
		EditText et = (EditText) v.findViewById(R.id.add_word_text);
		Intent i = getIntent();
		origword = i.getStringExtra("org.nyanya.android.traditionalt9.word");

		lang = i.getIntExtra("org.nyanya.android.traditionalt9.lang", -1);
		if (lang == -1) {
			Log.e("AddWordAct.onCreate", "lang is invalid. How?");
		}
		// Log.d("AddWord", "data.get: " + word);
		et.setText(origword);
		et.setSelection(origword.length());
		setContentView(v);
		main = v;
		pref = PreferenceManager.getDefaultSharedPreferences(this);
	}

	public void addWordButton(View v) {
		EditText et = (EditText) main.findViewById(R.id.add_word_text);
		// Log.d("AddWordAct", "adding word: " + et.getText());
		doAddWord(et.getText().toString());
		this.finish();
	}

	public void doAddWord(String text) {
		T9DB db = T9DB.getInstance(this);
		try {
			db.addWord(text, lang);
		} catch (DBException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			String msg = e.getMessage();
			//Log.e("AddWord.doAddWord", msg);
			builder.setMessage(msg).setTitle(R.string.add_word)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		SharedPreferences.Editor prefedit = pref.edit();
		prefedit.putString("last_word", text);
		prefedit.commit();
	}


	public void cancelButton(View v) {
		// Log.d("AddWordAct", "Cancelled...");
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_word, menu);
		return true;
	}

}
