package io.github.sspanak.tt9.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class UI {
	private static Toast toastLang = null;

	public static void showAddWordDialog(InputMethodService ims, int language, String currentWord) {
		Intent intent = new Intent(ims, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra("word", currentWord);
		intent.putExtra("lang", language);
		intent.putExtra("popup_type", PopupDialogActivity.DIALOG_ADD_WORD_INTENT);
		ims.startActivity(intent);
	}


	public static void showConfirmDictionaryUpdateDialog(InputMethodService ims, int language) {
		Intent intent = new Intent(ims, PopupDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra("lang", language);
		intent.putExtra("popup_type", PopupDialogActivity.DIALOG_CONFIRM_WORDS_UPDATE_INTENT);
		ims.startActivity(intent);
	}


	public static void showChangeKeyboardDialog(Context context) {
		((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
	}


	public static void showSettingsScreen(InputMethodService ims) {
		Intent prefIntent = new Intent(ims, PreferencesActivity.class);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		ims.hideWindow();
		ims.startActivity(prefIntent);
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
			.setCancelable(false)
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

	public static void toastLanguage(@NonNull Context context, @NonNull Language language) {
		if (toastLang != null) {
			toastLang.cancel();
		}

		// we recreate the toast, because if set new text, when it is fading out,
		// the new text is discarded
		toastLang = Toast.makeText(context, language.getName(), Toast.LENGTH_SHORT);
		toastLang.show();
	}
}
