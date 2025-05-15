package io.github.sspanak.tt9.util.sys;

import android.content.Context;
import android.os.LocaleList;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;


public class SystemSettings {
	private static InputMethodManager inputManager;
	private static String packageName;

	public static boolean isTT9Enabled(Context context) {
		inputManager = inputManager == null ? (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE) : inputManager;
		packageName = packageName == null ? context.getPackageName() : packageName;

		for (final InputMethodInfo imeInfo : inputManager.getEnabledInputMethodList()) {
			if (packageName.equals(imeInfo.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTT9Selected(Context context) {
		String defaultIME = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		inputManager = inputManager == null ? (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE) : inputManager;
		packageName = packageName == null ? context.getPackageName() : packageName;

		for (final InputMethodInfo imeInfo : inputManager.getEnabledInputMethodList()) {
			if (packageName.equals(imeInfo.getPackageName()) && imeInfo.getId().equals(defaultIME)) {
				return true;
			}
		}
		return false;
	}

	@NonNull
	public static String getLocale() {
		Locale locale = DeviceInfo.AT_LEAST_ANDROID_7 ? LocaleList.getDefault().get(0) : Locale.getDefault();
		String country = locale.getCountry();
		String language = locale.getLanguage();

		if (language.equals(Locale.ENGLISH.getLanguage())) {
			country = "";
		}

		return country.isEmpty() ? language : language + "_" + country;
	}

	@Nullable
	public static String getPreviousIME(Context context) {
		inputManager = inputManager == null ? (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE) : inputManager;
		packageName = packageName == null ? context.getPackageName() : packageName;

		for (final InputMethodInfo imeInfo : inputManager.getEnabledInputMethodList()) {
			if (!packageName.equals(imeInfo.getPackageName())) {
				return imeInfo.getId();
			}
		}

		return null;
	}

	/**
	 * Even though the background changes automatically on Android 15, thanks to edge-to-edge,
	 * the text/icon color remains the device default. This function allows us to change it.
	 * {@code @see:} <a href="https://stackoverflow.com/a/77240330">the only working solution</a>.
	 */
	public static void setNavigationBarDarkTheme(@Nullable Window window, boolean dark) {
		if (!DeviceInfo.AT_LEAST_ANDROID_11) {
			return;
		}

		WindowInsetsController insetsController = window != null ? window.getInsetsController() : null;
		if (insetsController == null) {
			return;
		}

		insetsController.setSystemBarsAppearance(
			dark ? 0 : WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
			WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
		);
	}
}
