package io.github.sspanak.tt9.ui.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.ui.dialogs.AutoUpdateMonolog;

public class DictionaryUpdateNotification extends DictionaryNotification {
	private final Language language;


	public DictionaryUpdateNotification(@NonNull Context context, @NonNull Language language) {
		super(context, language);
		this.language = language;
		notificationBuilder.addAction(getAction(context));
	}


	@Override
	protected PendingIntent createNavigationIntent(@NonNull Context context, @Nullable Language language) {
		Intent intent = AutoUpdateMonolog.generateShowIntent(context, language != null ? language.getId() : -1);
		return PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}


	private NotificationCompat.Action getAction(Context context) {
		return new NotificationCompat.Action(
			R.drawable.ic_dictionary_update,
			resources.getString(R.string.dictionary_update_update),
			createNavigationIntent(context, language)
		);
	}


	@Override
	public void show() {
		notificationBuilder.setSmallIcon(R.drawable.ic_dictionary_update);
		messageLong = message = resources.getString(R.string.dictionary_update_message, language.getName());
		super.show();
	}


	@Override
	public void showError(@NonNull String ignored, @NonNull String ignored2) {}
}
