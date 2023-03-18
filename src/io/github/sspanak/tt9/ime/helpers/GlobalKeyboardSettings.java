package io.github.sspanak.tt9.ime.helpers;

import android.content.Context;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;


public class GlobalKeyboardSettings {
	private final InputMethodManager inputManager;
	private final String packageName;


	public GlobalKeyboardSettings(Context context, InputMethodManager inputManager) {
		this.inputManager = inputManager;
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
}
