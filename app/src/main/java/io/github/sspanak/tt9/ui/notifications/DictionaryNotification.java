package io.github.sspanak.tt9.ui.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import io.github.sspanak.tt9.languages.Language;

public abstract class DictionaryNotification {
	private static final int NOTIFICATION_ID = 1;
	private static final String NOTIFICATION_CHANNEL_ID = "dictionary-notifications";

	private final NotificationManager manager;
	protected final NotificationCompat.Builder notificationBuilder;
	protected final Resources resources;

	protected boolean indeterminate = false;
	protected String title = "";
	protected String message = "";
	protected String messageLong = "";


	protected DictionaryNotification(@NonNull Context context, @Nullable Language language) {
		resources = context.getResources();

		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = getNotificationBuilderCompat(context);

		notificationBuilder
			.setContentIntent(createNavigationIntent(context, language))
			.setSmallIcon(android.R.drawable.stat_notify_sync)
			.setCategory(NotificationCompat.CATEGORY_PROGRESS)
			.setOnlyAlertOnce(true);
	}

	protected abstract PendingIntent createNavigationIntent(@NonNull Context context, @Nullable Language language);

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


	public void show() {
		indeterminate = false;
		renderMessage();
	}


	public void showMessage(String title, String message, String messageLong) {
		this.title = title;
		this.message = message;
		this.messageLong = messageLong;
		this.show();
	}


	public void showError(String title, String message) {
		indeterminate = false;
		this.title = title;
		this.message = message;
		renderError();
	}


	protected void hide() {
		manager.cancel(NOTIFICATION_ID);
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
			.setStyle(bigMessage);

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}


	protected void renderMessage() {
		NotificationCompat.BigTextStyle bigMessage = new NotificationCompat.BigTextStyle();
			bigMessage.setBigContentTitle(title);
			bigMessage.bigText(messageLong.isEmpty() ? message : messageLong);

		notificationBuilder
			.setStyle(bigMessage)
			.setContentTitle(title)
			.setContentText(message);

		manager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
}
