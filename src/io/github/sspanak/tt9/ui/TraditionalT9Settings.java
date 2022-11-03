package io.github.sspanak.tt9.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.DictionaryImportAlreadyRunningException;
import io.github.sspanak.tt9.db.DictionaryImportException;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.T9Preferences;
import io.github.sspanak.tt9.settings_legacy.CustomInflater;
import io.github.sspanak.tt9.settings_legacy.Setting;
import io.github.sspanak.tt9.settings_legacy.SettingAdapter;

public class TraditionalT9Settings extends ListActivity implements DialogInterface.OnCancelListener {

	private DictionaryLoader loader;
	DictionaryLoadingBar progressBar;

	Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		progressBar = new DictionaryLoadingBar(this);

		// get settings
		T9Preferences prefs = new T9Preferences(getApplicationContext());
		Object[] settings = {
			prefs.getInputMode()
		};
		ListAdapter settingitems;
		try {
			settingitems = new SettingAdapter(this, CustomInflater.inflate(this, R.xml.prefs, settings));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		setContentView(R.layout.preference_list_content);
		setListAdapter(settingitems);
		mContext = this;

	}

	@Override
	public void onCancel(DialogInterface dint) {
		if (loader != null) {
			loader.stop();
		}
	}

	@Override
	protected  void onListItemClick(ListView l, View v, int position, long id) {
		Setting s = (Setting)getListView().getItemAtPosition(position);
		switch (s.id) {
			case "help":
				openHelp();
				break;
			case "loaddict":
				loadDictionaries();
				break;
			case "truncatedict":
				truncateWords();
				break;
			default:
				s.clicked(mContext);
				break;
		}
	}


	private void openHelp() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(getString(R.string.help_url)));
		startActivity(i);
	}

	private void truncateWords() {
		if (loader != null && loader.isRunning()) {
			loader.stop();
		}

		Handler afterTruncate = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				UI.toast(mContext, R.string.dictionary_truncated);
			}
		};
		DictionaryDb.truncateWords(afterTruncate);
	}

	private void loadDictionaries() {
		ArrayList<Language> languages = LanguageCollection.getAll(T9Preferences.getInstance().getEnabledLanguages());
		progressBar.setFileCount(languages.size());

		Handler loadHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				String error = msg.getData().getString("error", null);

				if (error != null) {
					progressBar.hide();
					handleError(
						error,
						msg.getData().getInt("languageId", -1),
						msg.getData().getLong("fileLine", -1),
						msg.getData().getString("word", "")
					);
				} else {
					progressBar.show(
						msg.getData().getInt("currentFile", 0),
						msg.getData().getInt("progress", 0),
						msg.getData().getInt("languageId", -1)
					);
				}
			}
		};

		if (loader == null) {
			loader = new DictionaryLoader(this);
		}

		try {
			loader.load(loadHandler, languages);
		} catch (DictionaryImportAlreadyRunningException e) {
			loader.stop();
			UI.toast(this, getString(R.string.dictionary_import_cancelled));
		}
	}

	private void handleError(String errorType, int langId, long line, String word) {
		Language lang = LanguageCollection.getLanguage(langId);
		String message;

		if (lang == null || errorType.equals(InvalidLanguageException.class.getSimpleName())) {
			message = getString(R.string.add_word_invalid_language);
		} else if (errorType.equals(DictionaryImportException.class.getSimpleName()) || errorType.equals(InvalidLanguageCharactersException.class.getSimpleName())) {
			String languageName = lang.getName();
			message = getString(R.string.dictionary_import_bad_char, word, line, languageName);
		} else if (errorType.equals(IOException.class.getSimpleName()) || errorType.equals(FileNotFoundException.class.getSimpleName())) {
			String languageName = lang.getName();
			message = getString(R.string.dictionary_not_found, languageName);
		} else {
			String languageName = lang.getName();
			message = getString(R.string.dictionary_import_error, languageName, errorType);
		}

		showErrorDialog(getString(R.string.dictionary_load_title), message);
	}

	private void showErrorDialog(CharSequence title, CharSequence msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setMessage(msg)
			.setTitle(title)
			.setNeutralButton(android.R.string.ok, (dialog, id) -> dialog.dismiss());
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
