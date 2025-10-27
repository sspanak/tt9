package io.github.sspanak.tt9.ui.notifications;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import io.github.sspanak.tt9.preferences.settings.SettingsStore;


public class DictionaryLoadingBar extends DictionaryProgressNotification {
	private static DictionaryLoadingBar self;

	private boolean isStopped = false;
	private long lastProgressUpdate = 0;
	private boolean hasFailed = false;
	private String shortMessage = "";

	private Runnable onStatusChange = null;
	private Runnable onStatusChange2 = null;
	@Nullable private Handler statusHandler;


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


	public void showStart(int fileCount) {
		maxProgress = fileCount * 100;
		hasFailed = false;
		isStopped = false;
	}


	public void showCancelled() {
		if (hasFailed) {
			return;
		}

		hide();
		isStopped = true;
		generateCancelMsg();
		notifyChange(0);
	}


	public void showError(@NonNull String errorMessage, @Nullable Language language, long lineNumber) {
		hasFailed = true;
		generateErrorMsg(errorMessage, language, lineNumber);
		notifyChange(0);
	}


	public void showProgress(@Nullable Language language, long time, int currentFile, int currentFileProgress) {
		if (hasFailed || isStopped) {
			return;
		}

		progress = 100 * currentFile + currentFileProgress;

		if (progress < maxProgress && lastProgressUpdate + SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME > System.currentTimeMillis()) {
			return;
		}

		generateProgressMsg(time, currentFileProgress, language != null ? language.getId() : -1);
		notifyChange(progress >= maxProgress ? SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME : 0);

		lastProgressUpdate = System.currentTimeMillis();
	}


	private String generateTitle(int languageId) {
		Language lang = LanguageCollection.getLanguage(languageId);

		if (lang != null) {
			return resources.getString(R.string.dictionary_loading, lang.getName());
		}

		return resources.getString(R.string.dictionary_loading_indeterminate);
	}


	private String generateShortMsg(int languageId, int progress) {
		Language lang = LanguageCollection.getLanguage(languageId);
		lang = lang != null ? lang : new NullLanguage();
		return resources.getString(R.string.dictionary_loading_short, lang.getCode().toUpperCase(lang.getLocale()), progress) + "%";
	}


	private void generateCancelMsg() {
		title = "";
		message = resources.getString(R.string.dictionary_load_cancelled);
		progress = maxProgress = 0;

		renderMessage();
	}


	private void generateErrorMsg(@NonNull String errorType, @Nullable Language lang, long line) {
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


	private void generateProgressMsg(long time, int currentFileProgress, int languageId) {
		isStopped = false;

		if (progress >= maxProgress) {
			progress = maxProgress = 0;
			title = generateTitle(-1);

			String timeFormat = time > 60000 ? " (%1.0fs)" : " (%1.1fs)";
			message = resources.getString(R.string.completed) + String.format(Locale.ENGLISH, timeFormat, time / 1000.0);
			shortMessage = resources.getString(R.string.completed);
		} else {
			title = generateTitle(languageId);
			message = currentFileProgress + "%";
			shortMessage = generateShortMsg(languageId, currentFileProgress);
		}

		renderMessage();
	}


	private void notifyChange(int delay) {
		if (statusHandler == null) {
			Looper looper = Looper.getMainLooper();
			statusHandler = looper != null ? new Handler(looper) : null;
		}

		if (statusHandler == null) {
			return;
		}

		if (delay > 0) {
			if (onStatusChange != null) statusHandler.postDelayed(onStatusChange, delay);
			if (onStatusChange2 != null) statusHandler.postDelayed(onStatusChange2, delay);
		} else {
			if (onStatusChange != null) statusHandler.post(onStatusChange);
			if (onStatusChange2 != null) statusHandler.post(onStatusChange2);
		}
	}
}
