package io.github.sspanak.tt9.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.languages.LanguagesScreen;

public abstract class DictionaryNotification {
	private static DictionaryNotification self;
	private static final int NOTIFICATION_ID = 1;
	private static final String NOTIFICATION_CHANNEL_ID = "dictionary-notifications";

	private final NotificationManager manager;
	private final NotificationCompat.Builder notificationBuilder;
	protected final Resources resources;

	protected int maxProgress = 0;
	protected int progress = 0;
	protected boolean indeterminate = false;
	protected String title = "";
	protected String message = "";
	protected String messageLong = "";


	protected DictionaryNotification(Context context) {
		resources = context.getResources();

		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = getNotificationBuilderCompat(context);

		notificationBuilder
			.setContentIntent(createNavigationIntent(context))
			.setSmallIcon(android.R.drawable.stat_notify_sync)
			.setCategory(NotificationCompat.CATEGORY_PROGRESS)
			.setOnlyAlertOnce(true);
	}


	public static DictionaryNotification getInstance(Context context) {
		if (self == null) {
			self = new DictionaryNotification(context) {
			};
		}
		return self;
	}


	private PendingIntent createNavigationIntent(Context context) {
		Intent intent = new Intent(context, PreferencesActivity.class);
		intent.putExtra("screen", LanguagesScreen.NAME);
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


	public void showMessage(@NonNull String title, @NonNull String message, @NonNull String messageLong) {
		progress = maxProgress = 0;
		indeterminate = false;
		this.title = title;
		this.message = message;
		this.messageLong = messageLong;
		renderMessage();
	}


	public void showLoadingMessage(@NonNull String title, @NonNull String message) {
		this.title = title;
		this.message = message;
		messageLong = "";
		indeterminate = true;
		progress = 1;
		maxProgress = 2;
		renderMessage();
	}


	public void showError(@NonNull String title, @NonNull String message) {
		progress = maxProgress = 0;
		indeterminate = false;
		this.title = title;
		this.message = message;
		renderError();
	}


	protected void hide() {
		progress = maxProgress = 0;
		manager.cancel(NOTIFICATION_ID);
	}


	public boolean inProgress() {
		return progress < maxProgress;
	}


	protected void renderError() {
		NotificationCompat.BigTextStyle bigMessage = new NotificationCompat.BigTextStyle();
		bigMessage.setBigContentTitle(title);
		bigMessage.bigText(message);

		notificationBuilder
			.setSmallIcon(android.R.drawable.stat_notify_error)
			.setContentTitle(title)
			.setContentText(message)
			.setOngoing(false)
			.setStyle(bigMessage)
			.setProgress(maxProgress, progress, false);

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}


	protected void renderMessage() {
		NotificationCompat.BigTextStyle bigMessage = new NotificationCompat.BigTextStyle();
			bigMessage.setBigContentTitle(title);
			bigMessage.bigText(messageLong.isEmpty() ? message : messageLong);

		notificationBuilder
			.setSmallIcon(inProgress() ? android.R.drawable.stat_notify_sync : R.drawable.ic_done)
			.setOngoing(inProgress())
			.setProgress(maxProgress, progress, indeterminate)
			.setStyle(bigMessage)
			.setContentTitle(title)
			.setContentText(message);

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
}
