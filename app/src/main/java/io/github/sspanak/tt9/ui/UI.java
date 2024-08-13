package io.github.sspanak.tt9.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.preferences.PreferencesActivity;

public class UI {
	private static final HashMap<String, Toast> singleToasts = new HashMap<>();


	public static void showChangeKeyboardDialog(Context context) {
		((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
	}


	public static void showSettingsScreen(InputMethodService ims) {
		Intent prefIntent = new Intent(ims, PreferencesActivity.class);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		prefIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		ims.startActivity(prefIntent);
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

	public static void toastLongFromAsync(Context context, CharSequence msg) {
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		toastLong(context, msg);
	}

	public static void toastShortSingle(@NonNull Context context, @NonNull String uniqueId, @NonNull String message) {
		Toast toast = singleToasts.get(uniqueId);

		if (toast != null) {
			toast.cancel();
		}

		// we recreate the toast, because if set new text, when it is fading out, it is ignored
		toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();

		singleToasts.put(uniqueId, toast);
	}


	public static void toastShortSingle(@NonNull Context context, int resourceId) {
		toastShortSingle(context, String.valueOf(resourceId), context.getString(resourceId));
	}
}
