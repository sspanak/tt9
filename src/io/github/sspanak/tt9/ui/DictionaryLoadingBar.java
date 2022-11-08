package io.github.sspanak.tt9.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import java.io.FileNotFoundException;
import java.io.IOException;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryImportException;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;


public class DictionaryLoadingBar {
	private static DictionaryLoadingBar self;

	private static final int NOTIFICATION_ID = 1;
	private static final String NOTIFICATION_CHANNEL_ID = "loading-notifications";

	private final NotificationManager manager;
	private final NotificationCompat.Builder notificationBuilder;
	private final Resources resources;

	private int maxProgress = 0;
	private int progress = 0;
	private boolean hasFailed = false;


	public static DictionaryLoadingBar getInstance(Context context) {
		if (self == null) {
			self = new DictionaryLoadingBar(context);
		}

		return self;
	}


	public DictionaryLoadingBar(Context context) {
		resources = context.getResources();

		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			manager.createNotificationChannel(new NotificationChannel(
				NOTIFICATION_CHANNEL_ID,
				"Dictionary Loading Channel",
				NotificationManager.IMPORTANCE_LOW
			));
			notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
		} else {
			//noinspection deprecation
			notificationBuilder = new NotificationCompat.Builder(context);
		}

		notificationBuilder
			.setSmallIcon(android.R.drawable.stat_notify_sync)
			.setCategory(NotificationCompat.CATEGORY_PROGRESS)
			.setOnlyAlertOnce(true);
	}


	public void setFileCount(int count) {
		maxProgress = count * 100;
	}


	public boolean isCompleted() {
		return progress >= maxProgress;
	}


	public boolean isFailed() {
		return hasFailed;
	}


	public void show(Bundle data) {
		String error = data.getString("error", null);

		if (error != null) {
			hasFailed = true;
			showError(
				error,
				data.getInt("languageId", -1),
				data.getLong("fileLine", -1),
				data.getString("word", "")
			);
		} else {
			hasFailed = false;
			showProgress(
				data.getInt("currentFile", 0),
				data.getInt("progress", 0),
				data.getInt("languageId", -1)
			);
		}
	}


	private String generateTitle(int languageId) {
		Language lang = LanguageCollection.getLanguage(languageId);

		if (lang != null) {
			return resources.getString(R.string.dictionary_loading, lang.getName());
		}

		return resources.getString(R.string.dictionary_load_title);
	}


	private void showProgress(int currentFile, int currentFileProgress, int languageId) {
		progress = 100 * currentFile + currentFileProgress;

		if (currentFileProgress < 0) {
			hide();
		} else if (progress >= maxProgress) {
			renderProgress(
				generateTitle(-1),
				resources.getString(R.string.completed),
				0,
				0
			);
		} else {
			renderProgress(
				generateTitle(languageId),
				currentFileProgress + "%",
				progress,
				maxProgress
			);
		}
	}


	private void showError(String errorType, int langId, long line, String word) {
		Language lang = LanguageCollection.getLanguage(langId);
		String message;

		if (lang == null || errorType.equals(InvalidLanguageException.class.getSimpleName())) {
			message = resources.getString(R.string.add_word_invalid_language);
		} else if (errorType.equals(DictionaryImportException.class.getSimpleName()) || errorType.equals(InvalidLanguageCharactersException.class.getSimpleName())) {
			String languageName = lang.getName();
			message = resources.getString(R.string.dictionary_load_bad_char, word, line, languageName);
		} else if (errorType.equals(IOException.class.getSimpleName()) || errorType.equals(FileNotFoundException.class.getSimpleName())) {
			String languageName = lang.getName();
			message = resources.getString(R.string.dictionary_not_found, languageName);
		} else {
			String languageName = lang.getName();
			message = resources.getString(R.string.dictionary_load_error, languageName, errorType);
		}

		renderError(generateTitle(-1), message);
	}


	private void hide() {
		manager.cancel(NOTIFICATION_ID);
	}


	private void renderError(String title, String message) {
		NotificationCompat.BigTextStyle bigMessage = new NotificationCompat.BigTextStyle();
			bigMessage.setBigContentTitle(title);
			bigMessage.bigText(message);

		notificationBuilder
			.setSmallIcon(android.R.drawable.stat_notify_error)
			.setStyle(bigMessage)
			.setOngoing(false)
			.setProgress(0, 0, false);

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}


	private void renderProgress(String title, String message, int progress, int maxProgress) {
		notificationBuilder
			.setSmallIcon(progress < maxProgress ? android.R.drawable.stat_notify_sync : R.drawable.ic_done)
			.setOngoing(progress < maxProgress)
			.setProgress(maxProgress, progress, false)
			.setContentTitle(title)
			.setContentText(message);

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
}
