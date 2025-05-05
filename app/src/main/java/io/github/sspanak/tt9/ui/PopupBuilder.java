package io.github.sspanak.tt9.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class PopupBuilder {
	private final Context context;
	private MaterialAlertDialogBuilder builder12;
	private AlertDialog.Builder builderLegacy;


	public PopupBuilder(Context context) {
		this.context = context;

		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12 = new MaterialAlertDialogBuilder(context);
		} else {
			builderLegacy = new AlertDialog.Builder(context);
		}
	}


	public PopupBuilder setCancelable(boolean cancelable) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12.setCancelable(cancelable);
		} else {
			builderLegacy.setCancelable(cancelable);
		}
		return this;
	}


	public PopupBuilder setMessage(String message) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12.setMessage(message);
		} else {
			builderLegacy.setMessage(message);
		}
		return this;
	}


	public PopupBuilder setNegativeButton(boolean yes, Runnable action) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12.setNegativeButton(
				yes ? context.getString(android.R.string.cancel) : null,
				(dialog, whichButton) -> { if (action != null) action.run(); }
			);
		} else {
			builderLegacy.setNegativeButton(
				yes ? context.getString(android.R.string.cancel) : null,
				(dialog, whichButton) -> { if (action != null) action.run(); }
			);
		}
		return this;
	}


	public PopupBuilder setOnKeyListener(DialogInterface.OnKeyListener listener) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12.setOnKeyListener(listener);
		} else {
			builderLegacy.setOnKeyListener(listener);
		}
		return this;
	}


	public PopupBuilder setPositiveButton(String text, Runnable action) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12.setPositiveButton(
				text,
				(dialog, which) -> action.run()
			);
		} else {
			builderLegacy.setPositiveButton(
				text,
				(dialog, which) -> action.run()
			);
		}
		return this;
	}


	public PopupBuilder setTitle(String title) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12.setTitle(title);
		} else {
			builderLegacy.setTitle(title);
		}
		return this;
	}


	public PopupBuilder setView(int viewId) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12.setView(viewId);
		} else {
			builderLegacy.setView(viewId);
		}
		return this;
	}


	public Dialog show() {
		return DeviceInfo.AT_LEAST_ANDROID_12 ? builder12.show() : builderLegacy.show();
	}
}
