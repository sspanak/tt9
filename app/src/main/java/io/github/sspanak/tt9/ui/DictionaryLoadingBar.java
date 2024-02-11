package io.github.sspanak.tt9.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.DictionariesScreen;


public class DictionaryLoadingBar {
	private static DictionaryLoadingBar self;

	private static final int NOTIFICATION_ID = 1;
	private static final String NOTIFICATION_CHANNEL_ID = "loading-notifications";

	private final NotificationManager manager;
	private final NotificationCompat.Builder notificationBuilder;
	private final Resources resources;

	private boolean isStopped = false;
	private boolean hasFailed = false;

	private int maxProgress = 0;
	private int progress = 0;
	private String title = "";
	private String message = "";


	public static DictionaryLoadingBar getInstance(Context context) {
		if (self == null) {
			self = new DictionaryLoadingBar(context);
		}

		return self;
	}


	public DictionaryLoadingBar(Context context) {
		resources = context.getResources();

		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = getNotificationBuilderCompat(context);

		notificationBuilder
			.setContentIntent(createNavigationIntent(context))
			.setSmallIcon(android.R.drawable.stat_notify_sync)
			.setCategory(NotificationCompat.CATEGORY_PROGRESS)
			.setOnlyAlertOnce(true);
	}


	private PendingIntent createNavigationIntent(Context context) {
		Intent intent = new Intent(context, PreferencesActivity.class);
		intent.putExtra("screen", DictionariesScreen.class.getSimpleName());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}


	private NotificationCompat.Builder getNotificationBuilderCompat(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			manager.createNotificationChannel(new NotificationChannel(
				NOTIFICATION_CHANNEL_ID,
				"Dictionary Status",
				NotificationManager.IMPORTANCE_LOW
			));
			return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
		} else {
			//noinspection deprecation
			return new NotificationCompat.Builder(context);
		}
	}


	public void setFileCount(int count) {
		maxProgress = count * 100;
	}


	public boolean isCancelled() {
		return isStopped;
	}


	public boolean isCompleted() {
		return progress >= maxProgress;
	}


	public boolean isFailed() {
		return hasFailed;
	}


	public String getTitle() {
		return title;
	}


	public String getMessage() {
		return message;
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

		renderProgress();
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


	private void hide() {
		progress = maxProgress = 0;
		manager.cancel(NOTIFICATION_ID);
	}


	private void renderError() {
		NotificationCompat.BigTextStyle bigMessage = new NotificationCompat.BigTextStyle();
			bigMessage.setBigContentTitle(title);
			bigMessage.bigText(message);

		notificationBuilder
			.setSmallIcon(android.R.drawable.stat_notify_error)
			.setStyle(bigMessage)
			.setOngoing(false)
			.setProgress(maxProgress, progress, false);

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}


	private void renderProgress() {
		notificationBuilder
			.setSmallIcon(isCompleted() ? R.drawable.ic_done : android.R.drawable.stat_notify_sync)
			.setOngoing(!isCompleted())
			.setProgress(maxProgress, progress, false)
			.setContentTitle(title)
			.setContentText(message);

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
}
