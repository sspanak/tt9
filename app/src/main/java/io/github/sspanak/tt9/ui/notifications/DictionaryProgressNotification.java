package io.github.sspanak.tt9.ui.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.languages.LanguagesScreen;

public class DictionaryProgressNotification extends DictionaryNotification {
	private static DictionaryProgressNotification self;

	protected int maxProgress = 0;
	protected int progress = 0;


	protected DictionaryProgressNotification(Context context) {
		super(context, null);
	}


	public static DictionaryProgressNotification getInstance(Context context) {
		if (self == null) {
			self = new DictionaryProgressNotification(context) {
			};
		}
		return self;
	}


	@Override
	protected final PendingIntent createNavigationIntent(@NonNull Context context, @Nullable Language language) {
		Intent intent = new Intent(context, PreferencesActivity.class);
		intent.putExtra("screen", LanguagesScreen.NAME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}


	@Override
	public void showMessage(@NonNull String title, @NonNull String message, @NonNull String messageLong) {
		progress = maxProgress = 0;
		super.showMessage(title, message, messageLong);
	}


	public void showLoadingMessage(@NonNull String title, @NonNull String message) {
		showLoadingMessage(title, message, 0, 1);
	}


	public void showLoadingMessage(@NonNull String title, @NonNull String message, int progress, int maxProgress) {
		this.title = title;
		this.message = message;
		messageLong = "";
		this.progress = progress;
		this.maxProgress = maxProgress;
		indeterminate = (progress <= 0 && maxProgress <= 0);
		renderMessage();
	}


	@Override
	public void showError(@NonNull String title, @NonNull String message) {
		progress = maxProgress = 0;
		super.showError(title, message);
	}


	@Override
	public void hide() {
		progress = maxProgress = 0;
		super.hide();
	}


	public boolean inProgress() {
		return progress < maxProgress;
	}


	@Override
	protected void renderError() {
		notificationBuilder.setProgress(maxProgress, progress, false);
		super.renderError();
	}


	@Override
	protected void renderMessage() {
		notificationBuilder
			.setSmallIcon(inProgress() ? android.R.drawable.stat_notify_sync : R.drawable.ic_done)
			.setOngoing(inProgress())
			.setProgress(maxProgress, progress, indeterminate);

		super.renderMessage();
	}
}
