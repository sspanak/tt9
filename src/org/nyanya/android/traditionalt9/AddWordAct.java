package org.nyanya.android.traditionalt9;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class AddWordAct extends Activity {

	View main;
	
	@Override
	protected void onCreate(Bundle savedData) {
		super.onCreate(savedData);
		View v = getLayoutInflater().inflate(R.layout.addwordview, null);
		EditText et = (EditText)v.findViewById(R.id.add_word_text);
		String word = getIntent().getStringExtra("org.nyanya.android.traditionalt9.word");
		//Log.d("AddWord", "data.get: " + word);
		et.setText(word);
		et.setSelection(word.length());
		setContentView(v);
		main = v;
	}
	
	public void addWordButton(View v) {
		EditText et = (EditText) main.findViewById(R.id.add_word_text);
		//Log.d("AddWordAct", "adding word: " + et.getText());
		TraditionalT9.ghettoaccess.doAddWord(et.getText().toString());
		this.finish();
	}
	
	public void cancelButton(View v) {
		//Log.d("AddWordAct", "Cancelled...");
		//TraditionalT9.ghettoaccess.addCancel();
		this.finish();
	}
	
	@Override public void onStop() {
		TraditionalT9.ghettoaccess.addCancel();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_word, menu);
		return true;
	}

}
