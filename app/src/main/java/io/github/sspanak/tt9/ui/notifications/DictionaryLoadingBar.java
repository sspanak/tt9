package io.github.sspanak.tt9.ui.notifications;

import android.content.Context;
import android.os.Bundle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageException;


public class DictionaryLoadingBar extends DictionaryProgressNotification {
	private static DictionaryLoadingBar self;

	private boolean isStopped = false;
	private boolean hasFailed = false;
	private String shortMessage = "";
	private Runnable onStatusChange = null;
	private Runnable onStatusChange2 = null;


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


	public String getShortMessage() {
		return shortMessage;
	}


	public String getTitle() {
		return title;
	}


	public void setFileCount(int count) {
		maxProgress = count * 100;
	}


	public void setOnStatusChange(Runnable onStatusChange) {
		this.onStatusChange = onStatusChange;
	}


	public void setOnStatusChange2(Runnable onStatusChange2) {
		this.onStatusChange2 = onStatusChange2;
	}


	public boolean isCancelled() {
		return isStopped;
	}


	public boolean isFailed() {
		return hasFailed;
	}


	public void show(Bundle data) {
		String error = data.getString("error", null);
		int fileCount = data.getInt("fileCount", -1);
		int progress = data.getInt("progress", -1);

		if (error != null) {
			hasFailed = true;
			showError(
				error,
				data.getInt("languageId", -1),
				data.getLong("fileLine", -1)
			);
			if (onStatusChange != null) onStatusChange.run();
			if (onStatusChange2 != null) onStatusChange2.run();
		} else if (progress >= 0) {
			hasFailed = false;
			if (fileCount >= 0) {
				setFileCount(fileCount);
			}

			showProgress(
				data.getLong("time", 0),
				data.getInt("currentFile", 0),
				data.getInt("progress", 0),
				data.getInt("languageId", -1)
			);

			if (onStatusChange != null) onStatusChange.run();
			if (onStatusChange2 != null) onStatusChange2.run();
		}
	}


	private String generateTitle(int languageId) {
		Language lang = LanguageCollection.getLanguage(languageId);

		if (lang != null) {
			return resources.getString(R.string.dictionary_loading, lang.getName());
		}

		return resources.getString(R.string.dictionary_loading_indeterminate);
	}


	private String generateShortMessage(int languageId, int progress) {
		Language lang = LanguageCollection.getLanguage(languageId);
		lang = lang != null ? lang : new NullLanguage();
		return resources.getString(R.string.dictionary_loading_short, lang.getCode().toUpperCase(lang.getLocale()), progress) + "%";
	}


	private void showProgress(long time, int currentFile, int currentFileProgress, int languageId) {
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
			title = generateTitle(-1);

			String timeFormat = time > 60000 ? " (%1.0fs)" : " (%1.1fs)";
			message = resources.getString(R.string.completed) + String.format(Locale.ENGLISH, timeFormat, time / 1000.0);
			shortMessage = resources.getString(R.string.completed);
		} else {
			title = generateTitle(languageId);
			message = currentFileProgress + "%";
			shortMessage = generateShortMessage(languageId, currentFileProgress);
		}

		renderMessage();
	}


	private void showError(String errorType, int langId, long line) {
		Language lang = LanguageCollection.getLanguage(langId);

		if (lang == null || errorType.equals(InvalidLanguageException.class.getSimpleName())) {
			message = resources.getString(R.string.add_word_invalid_language);
		} else if (errorType.equals(DictionaryImportException.class.getSimpleName()) || errorType.equals(InvalidLanguageCharactersException.class.getSimpleName())) {
			message = resources.getString(R.string.dictionary_load_bad_char, line, lang.getName());
		} else if (errorType.equals(UnknownHostException.class.getSimpleName()) || errorType.equals(SocketException.class.getSimpleName())) {
			message = resources.getString(R.string.dictionary_load_no_internet, lang.getName());
		} else if (errorType.equals(IOException.class.getSimpleName()) || errorType.equals(FileNotFoundException.class.getSimpleName())) {
			message = resources.getString(R.string.dictionary_not_found, lang.getName());
		} else {
			message = resources.getString(R.string.dictionary_load_error, lang.getName(), errorType);
		}

		title = generateTitle(-1);
		progress = maxProgress = 0;

		renderError();
	}
}
