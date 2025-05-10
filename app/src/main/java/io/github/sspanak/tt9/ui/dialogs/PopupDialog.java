package io.github.sspanak.tt9.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.ConsumerCompat;

abstract public class PopupDialog implements DialogInterface.OnKeyListener {
	public static final String INTENT_CLOSE = "tt9.popup_dialog.close";
	public static final String PARAMETER_DIALOG_TYPE = "popup_type";


	protected final Context context;
	protected final ConsumerCompat<String> activityFinisher;
	protected String title;
	protected String message;
	protected String OKLabel;

	PopupDialog(@NonNull Context context, ConsumerCompat<String> activityFinisher) {
		this.activityFinisher = activityFinisher;
		this.context = context;
	}

	protected void close() {
		if (activityFinisher != null) {
			activityFinisher.accept("");
		}
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		return false;
	}

	abstract void show();
}
