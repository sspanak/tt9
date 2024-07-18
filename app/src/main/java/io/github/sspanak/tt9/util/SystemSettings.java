package io.github.sspanak.tt9.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.LocaleList;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.Locale;


public class SystemSettings {
	private static InputMethodManager inputManager;
	private static String packageName;

	public static boolean isTT9Enabled(Activity context) {
		inputManager = inputManager == null ? (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE) : inputManager;
		packageName = packageName == null ? context.getPackageName() : packageName;

		for (final InputMethodInfo imeInfo : inputManager.getEnabledInputMethodList()) {
			if (packageName.equals(imeInfo.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	public static String getLocale() {
		Locale locale = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? LocaleList.getDefault().get(0) : Locale.getDefault();
		String country = locale.getCountry();
		String language = locale.getLanguage();

		if (language.equals(Locale.ENGLISH.getLanguage())) {
			country = "";
		}

		return country.isEmpty() ? language : language + "_" + country;
	}
}
