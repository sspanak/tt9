package io.github.sspanak.tt9.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.UI;

public class Clipboard {
	public static void copy(Context context, @NonNull CharSequence label, @NonNull CharSequence text) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText(label, text));

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
			UI.toast(context, "Text copied.");
		}
	}

	public static void copy(Context context, @NonNull CharSequence text) {
		String label = context.getString(R.string.app_name_short) + " / text";
		copy(context, label, text);
	}

	@Nullable public static String paste(Context context) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = clipboard.getPrimaryClip();
		return clip != null ? clip.getItemAt(0).getText().toString() : null;
	}
}