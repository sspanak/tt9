package io.github.sspanak.tt9.util.sys;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class Clipboard {
	private static Runnable externalChangeListener;
	private static boolean ignoreNextChange = false;

	@NonNull private static CharSequence lastText = "";

	public static void copy(@NonNull Context context, @NonNull CharSequence label, @NonNull CharSequence text) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText(label, text));

		// Android clipboard works unreliably on all versions from 5 to 14, even when invoked from
		// the context menu. So, just in case, we keep a backup of the text.
		lastText = text;

		ignoreNextChange = true;
	}

	public static void copy(@NonNull Context context, @NonNull CharSequence text) {
		String label = context.getString(R.string.app_name_short) + " / text";
		copy(context, label, text);
	}

	@NonNull public static String paste(@NonNull Context context) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = clipboard.getPrimaryClip();

		// Try using the shared clipboard, but if Android has failed preserving it, use our backup.
		CharSequence text = clip != null && clip.getItemCount() > 0 ? clip.getItemAt(0).getText() : "";
		text = text == null || text.length() == 0 ? lastText : text;

		return text.toString();
	}


	@NonNull public static String getPreview(@NonNull Context context) {
		String text = paste(context);

		if (text.length() > SettingsStore.CLIPBOARD_PREVIEW_LENGTH) {
			return text.substring(0, SettingsStore.CLIPBOARD_PREVIEW_LENGTH) + "...";
		}

		return text;
	}

	public static void setOnChangeListener(Context context, Runnable newListener) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

		if (newListener != null) {
			clipboard.addPrimaryClipChangedListener(Clipboard::changeListener);
		} else if (externalChangeListener != null) {
			clipboard.removePrimaryClipChangedListener(Clipboard::changeListener);
		}

		externalChangeListener = newListener;
	}

	private static void changeListener() {
		if (ignoreNextChange) {
			ignoreNextChange = false;
		} else if (externalChangeListener != null) {
			externalChangeListener.run();
		}
	}
}
