package io.github.sspanak.tt9.util;

import android.app.LocaleManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

/** Explicit UI preference; selecting or downloading an input language never changes it. */
public final class InterfaceLanguage {
	private InterfaceLanguage() {}

	public static void apply(Context context, String language) {
		String tags = explicitLanguage(language);
		if (Build.VERSION.SDK_INT >= 33) {
			LocaleManager manager = context.getSystemService(LocaleManager.class);
			LocaleList locales = LocaleList.forLanguageTags(tags);
			if (manager != null && !manager.getApplicationLocales().equals(locales)) {
				manager.setApplicationLocales(locales);
			}
		} else {
			LocaleListCompat locales = LocaleListCompat.forLanguageTags(tags);
			if (!AppCompatDelegate.getApplicationLocales().equals(locales)) {
				AppCompatDelegate.setApplicationLocales(locales);
			}
		}
	}

	public static void configure(Context context, Configuration configuration) {
		String tags = explicitLanguage(new SettingsStore(context).getInterfaceLanguage());
		if (!tags.isEmpty()) {
			configuration.setLocales(LocaleList.forLanguageTags(tags));
		}
	}


	private static String explicitLanguage(String language) {
		return "en".equals(language) || "zh-CN".equals(language) ? language : "";
	}


	/** Localizes service and application contexts without changing their shared resources. */
	public static final class ResourcesCache {
		private String language = "";
		private Configuration configuration;
		private Resources resources;

		public synchronized Resources get(Context context, Resources base, String preference) {
			String tags = explicitLanguage(preference);
			if (tags.isEmpty()) {
				language = "";
				configuration = null;
				resources = null;
				return base;
			}

			Configuration current = base.getConfiguration();
			if (resources == null || !tags.equals(language) || !current.equals(configuration)) {
				Configuration localized = new Configuration(current);
				localized.setLocales(LocaleList.forLanguageTags(tags));
				resources = context.createConfigurationContext(localized).getResources();
				configuration = new Configuration(current);
				language = tags;
			}
			return resources;
		}
	}
}
