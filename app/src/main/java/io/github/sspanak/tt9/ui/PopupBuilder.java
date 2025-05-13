package io.github.sspanak.tt9.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class PopupBuilder {
	private static final String LOG_TAG = PopupBuilder.class.getSimpleName();

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


	public PopupBuilder setView(View view) {
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			builder12.setView(view);
		} else {
			builderLegacy.setView(view);
		}
		return this;
	}


	public Dialog show() {
		return DeviceInfo.AT_LEAST_ANDROID_12 ? builder12.show() : builderLegacy.show();
	}


	/**
	 * In IME context, it is not that easy to show a popup dialog. We need to make it "valid" using
	 * the hacks below. Made possible thanks to:
	 * <a href="https://stackoverflow.com/questions/51906586/display-dialog-from-input-method-service-in-android-9-android-pie">Philipp</a>
	 * <a href="https://stackoverflow.com/questions/3494476/android-ime-how-to-show-a-pop-up-dialog/3508462#3508462">Maher Abuthraa</a>
	 */
	public Dialog showFromIme(MainView main) {
		if (main == null || main.getView() == null) {
			Logger.e(LOG_TAG, "Cannot show a popup dialog. Main view is null.");
			return null;
		}

		if (main.getView().getWindowToken() == null) {
			Logger.d(LOG_TAG, "Not creating popup dialog, because the Main view has no token yet. Try again when it is shown to the user.");
			return null;
		}

		Dialog dialog = DeviceInfo.AT_LEAST_ANDROID_12 ? builder12.create() : builderLegacy.create();

		Window window = dialog.getWindow();
		if (window == null) {
			Logger.e(LOG_TAG, "Cannot show a popup dialog. AlertDialog generated a Dialog with NULL Window.");
			return null;
		}

		WindowManager.LayoutParams layout = window.getAttributes();
		layout.token = main.getView().getWindowToken();
		layout.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		window.setAttributes(layout);
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		dialog.show();

		return dialog;
	}
}
