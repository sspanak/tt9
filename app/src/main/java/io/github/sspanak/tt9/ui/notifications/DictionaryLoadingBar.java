package io.github.sspanak.tt9.ui.notifications;

import android.content.Context;
import android.os.Bundle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageException;


public class DictionaryLoadingBar extends DictionaryProgressNotification {
	private static DictionaryLoadingBar self;


	private boolean isStopped = false;
	private boolean hasFailed = false;


	public static DictionaryLoadingBar getInstance(Context context) {
		if (self == null) {
			self = new DictionaryLoadingBar(context);
		}

		return self;
	}


	private DictionaryLoadingBar(Context context) {
		super(context);
	}


	public String getMessage() {
		return message;
	}


	public String getTitle() {
		return title;
	}


	public void setFileCount(int count) {
		maxProgress = count * 100;
	}


	public boolean isCancelled() {
		return isStopped;
	}


	public boolean isFailed() {
		return hasFailed;
	}


	public void show(Context context, Bundle data) {
		String error = data.getString("error", null);
		int fileCount = data.getInt("fileCount", -1);
		int progress = data.getInt("progress", -1);

		if (error != null) {
			hasFailed = true;
			showError(
				context,
				error,
				data.getInt("languageId", -1),
				data.getLong("fileLine", -1),
				data.getString("word", "")
			);
		} else if (progress >= 0) {
			hasFailed = false;
			if (fileCount >= 0) {
				setFileCount(fileCount);
			}

			showProgress(
				context,
				data.getLong("time", 0),
				data.getInt("currentFile", 0),
				data.getInt("progress", 0),
				data.getInt("languageId", -1)
			);
		}
	}


	private String generateTitle(Context context, int languageId) {
		Language lang = LanguageCollection.getLanguage(context, languageId);

		if (lang != null) {
			return resources.getString(R.string.dictionary_loading, lang.getName());
		}

		return resources.getString(R.string.dictionary_loading_indeterminate);
	}


	private void showProgress(Context context, long time, int currentFile, int currentFileProgress, int languageId) {
		if (currentFileProgress <= 0) {
			hide();
			isStopped = true;
			title = "";
			message = resources.getString(R.string.dictionary_load_cancelled);
			return;
		}

		isStopped = false;
		progress = 100 * currentFile + currentFileProgress;

		if (progress >= maxProgress) {
			progress = maxProgress = 0;
			title = generateTitle(context, -1);

			String timeFormat = time > 60000 ? " (%1.0fs)" : " (%1.1fs)";
			message = resources.getString(R.string.completed) + String.format(Locale.ENGLISH, timeFormat, time / 1000.0);
		} else {
			title = generateTitle(context, languageId);
			message = currentFileProgress + "%";
		}

		renderMessage();
	}


	private void showError(Context context, String errorType, int langId, long line, String word) {
		Language lang = LanguageCollection.getLanguage(context, langId);

		if (lang == null || errorType.equals(InvalidLanguageException.class.getSimpleName())) {
			message = resources.getString(R.string.add_word_invalid_language);
		} else if (errorType.equals(DictionaryImportException.class.getSimpleName()) || errorType.equals(InvalidLanguageCharactersException.class.getSimpleName())) {
			message = resources.getString(R.string.dictionary_load_bad_char, word, line, lang.getName());
		} else if (errorType.equals(IOException.class.getSimpleName()) || errorType.equals(FileNotFoundException.class.getSimpleName())) {
			message = resources.getString(R.string.dictionary_not_found, lang.getName());
		} else {
			message = resources.getString(R.string.dictionary_load_error, lang.getName(), errorType);
		}

		title = generateTitle(context, -1);
		progress = maxProgress = 0;

		renderError();
	}
}
