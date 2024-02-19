package io.github.sspanak.tt9.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class UI {
	public static void showAddWordDialog(TraditionalT9 tt9, int language, String currentWord) {
		Intent intent = new Intent(tt9, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra("word", currentWord);
		intent.putExtra("lang", language);
		intent.putExtra("popup_type", PopupDialogActivity.DIALOG_ADD_WORD_INTENT);
		tt9.startActivity(intent);
	}


	public static void showConfirmDictionaryUpdateDialog(TraditionalT9 tt9, int language) {
		Intent intent = new Intent(tt9, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra("lang", language);
		intent.putExtra("popup_type", PopupDialogActivity.DIALOG_CONFIRM_WORDS_UPDATE_INTENT);
		tt9.startActivity(intent);
	}


	public static void showChangeKeyboardDialog(Context context) {
		((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
	}


	public static void showSettingsScreen(TraditionalT9 tt9) {
		Intent prefIntent = new Intent(tt9, PreferencesActivity.class);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		tt9.hideWindow();
		tt9.startActivity(prefIntent);
	}

	public static void alert(Context context, int titleResource, int messageResource) {
		new AlertDialog.Builder(context)
			.setTitle(titleResource)
			.setMessage(messageResource)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel())
			.show();
	}

	public static void confirm(Context context, String title, String message, String OKLabel, Runnable onOk, Runnable onCancel) {
		new AlertDialog.Builder(context)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(OKLabel, (dialog, whichButton) -> { if (onOk != null) onOk.run(); })
			.setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> { if (onCancel != null) onCancel.run(); })
			.setOnCancelListener(dialog -> { if (onCancel != null) onCancel.run(); })
			.show();
	}

	public static void toast(Context context, CharSequence msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void toastFromAsync(Context context, CharSequence msg) {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		toast(context, msg);
	}

	public static void toast(Context context, int resourceId) {
		Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show();
	}

	public static void toastFromAsync(Context context, int resourceId) {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		toast(context, resourceId);
	}

	public static void toastLong(Context context, int resourceId) {
		Toast.makeText(context, resourceId, Toast.LENGTH_LONG).show();
	}

	public static void toastLong(Context context, CharSequence msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
