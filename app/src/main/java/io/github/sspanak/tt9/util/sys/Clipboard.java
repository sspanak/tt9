package io.github.sspanak.tt9.util.sys;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class Clipboard {
	@NonNull private static final LinkedList<CharSequence> clips = new LinkedList<>();
	private static Runnable externalChangeListener;
	private static boolean ignoreNextChange = false;


	public static void copy(@NonNull Context context, @NonNull CharSequence label, @NonNull CharSequence text) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
		addClip(text);
		ignoreNextChange = true;
	}


	public static void copy(@NonNull Context context, @NonNull CharSequence text) {
		String label = context.getString(R.string.app_name_short) + " / text";
		copy(context, label, text);
	}


	@NonNull
	public static LinkedList<CharSequence> getAll(@NonNull Context context) {
		// Attempt to restore clips from the Android clipboard. It works unreliably on all versions from
		// 5 to 14, even when invoked from the context menu. However, we give it a try for user convenience.
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData androidClip = clipboard.getPrimaryClip();
		CharSequence androidText = androidClip != null && androidClip.getItemCount() > 0 ? androidClip.getItemAt(0).getText() : null;
		addClip(androidText);

		return clips;
	}


	@NonNull
	public static String get(int index) {
		return index >= 0 && index < clips.size() ? clips.get(index).toString() : "";
	}


	@NonNull
	public static String getLastPreview() {
		String lastPreview = getPreview(clips.size() - 1, "...");
		return lastPreview != null ? lastPreview : "";
	}


	@Nullable
	public static String getPreview(int index, @NonNull String suffix) {
		if (index < 0 || index >= clips.size()) {
			return null;
		}

		final String original = clips.get(index).toString();
		String formatted = original.replaceAll("[\\n\\r\\t]+", " ");
		if (formatted.length() > SettingsStore.CLIPBOARD_PREVIEW_LENGTH) {
			formatted = formatted.substring(0, SettingsStore.CLIPBOARD_PREVIEW_LENGTH);
		}

		if (!formatted.equals(original)) {
			formatted += suffix;
		}

		return formatted;
	}


	public static void setOnChangeListener(@NonNull Context context, @Nullable Runnable newListener) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

		if (newListener != null) {
			clipboard.addPrimaryClipChangedListener(Clipboard::changeListener);
		} else if (externalChangeListener != null) {
			clipboard.removePrimaryClipChangedListener(Clipboard::changeListener);
		}

		externalChangeListener = newListener;
	}


	public static void clearListener(@NonNull Context context) {
		setOnChangeListener(context, null);
	}


	private static void changeListener() {
		if (ignoreNextChange) {
			ignoreNextChange = false;
		} else if (externalChangeListener != null) {
			externalChangeListener.run();
		}
	}


	private static void addClip(@Nullable CharSequence text) {
		if (text == null || text.length() == 0) {
			return;
		}

		final int clipIndex = indexOf(text);
		if (clipIndex != -1) {
			clips.remove(clipIndex);
		} else if (clips.size() == SettingsStore.SUGGESTIONS_MAX) {
			clips.removeFirst();
		}

		clips.add(text);
	}


	private static int indexOf(@NonNull CharSequence text) {
		// indexOf on CharSequence compares references, so we have to search manually
		for (int i = clips.size() - 1; i >= 0 ; i--) {
			if (clips.get(i).toString().contentEquals(text)) {
				return i;
			}
		}
		return -1;
	}
}
