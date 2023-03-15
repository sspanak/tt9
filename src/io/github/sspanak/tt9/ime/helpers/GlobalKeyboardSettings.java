package io.github.sspanak.tt9.ime.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;


public class GlobalKeyboardSettings {
	private final InputMethodManager inputManager;
	private final ContentResolver contentResolver;

	private final String packageName;


	public GlobalKeyboardSettings(Context context, InputMethodManager inputManager) {
		this.inputManager = inputManager;

		contentResolver = context.getContentResolver();
		packageName = context.getPackageName();
	}


	public boolean isTT9Enabled() {
		for (final InputMethodInfo imeInfo : inputManager.getEnabledInputMethodList()) {
			if (packageName.equals(imeInfo.getPackageName())) {
				return true;
			}
		}
		return false;
	}


	public String getDefault() {
		String defaultImeId = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD);
		String[] parts = defaultImeId.split("/", 2);

		return parts.length == 2 ? parts[1] : defaultImeId;
	}
}
