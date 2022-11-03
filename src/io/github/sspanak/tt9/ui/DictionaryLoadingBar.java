package io.github.sspanak.tt9.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class DictionaryLoadingBar {
	private static final int NOTIFICATION_ID = 1;
	private static final String NOTIFICATION_CHANNEL_ID = "loading-notifications";

	private final NotificationManager manager;
	private final NotificationCompat.Builder notificationBuilder;
	private final Resources resources;

	private int maxProgress = 0;


	DictionaryLoadingBar(Context context) {
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
			notificationBuilder = new NotificationCompat.Builder(context);
		}

		notificationBuilder
			.setSmallIcon(R.drawable.ic_notification)
			.setCategory(NotificationCompat.CATEGORY_PROGRESS)
			.setOnlyAlertOnce(true);
	}


	private String generateTitle(int languageId) {
		Language lang = LanguageCollection.getLanguage(languageId);

		if (lang != null) {
			return resources.getString(R.string.dictionary_loading, lang.getName());
		}

		return resources.getString(R.string.dictionary_load_title);
	}


	public void show(int currentFile, int currentFileProgress, int languageId) {
		int totalProgress = 100 * currentFile + currentFileProgress;

		if (currentFileProgress < 0) {
			hide();
			return;
		} else if (totalProgress >= maxProgress) {
			notificationBuilder
				.setContentTitle(generateTitle(-1))
				.setContentText(resources.getString(R.string.dictionary_loaded))
				.setOngoing(false)
				.setProgress(0, 0, false);
		} else {
			notificationBuilder
				.setContentTitle(generateTitle(languageId))
				.setContentText(currentFileProgress + "%")
				.setOngoing(true)
				.setProgress(maxProgress, totalProgress, false);
		}

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}


	public void hide() {
		manager.cancel(NOTIFICATION_ID);
	}


	public void setFileCount(int count) {
		maxProgress = count * 100;
	}
}
