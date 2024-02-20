package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.ui.UI;

abstract public class PopupDialog {
	protected final Context context;
	protected final ConsumerCompat<String> activityFinisher;
	protected String title;
	protected String message;
	protected String OKLabel;

	public PopupDialog(@NonNull Context context, @NonNull Intent intent, ConsumerCompat<String> activityFinisher) {
		this.activityFinisher = activityFinisher;
		this.context = context;
		parseIntent(context, intent);
	}

	abstract protected void parseIntent(Context context, Intent intent);
	abstract public void render();

	protected void render(Runnable OKAction) {
		UI.confirm(context, title, message, OKLabel, OKAction, () -> activityFinisher.accept(""));
	}
}
