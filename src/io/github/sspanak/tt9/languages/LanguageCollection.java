package io.github.sspanak.tt9.languages;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LanguageCollection {
	private static LanguageCollection self;

	private final HashMap<Integer, Language> languages = new HashMap<>();

	private LanguageCollection() {
		// add any of the languages in the package here to make them available
		List<Class<? extends Language>> languageList = Arrays.asList(
			Bulgarian.class,
			English.class,
			Russian.class,
			Ukrainian.class
		);

		// initialize the language objects from the class list above
		for (Class<? extends Language> languageClass : languageList) {
			try {
				Language lang = languageClass.newInstance();
				languages.put(lang.getId(), lang);
			} catch (Exception e) {
				Log.e("LanguageCollection", "Skipping an invalid language. " + e.getMessage());
			}
		}
	}

	public static LanguageCollection getInstance() {
		if (self == null) {
			self = new LanguageCollection();
		}

		return self;
	}

	public static Language getLanguage(int langId) {
		if (getInstance().languages.containsKey(langId)) {
			return getInstance().languages.get(langId);
		}

		return null;
	}

	public static ArrayList<Language> getAll(ArrayList<Integer> languageIds) {
		ArrayList<Language> langList = new ArrayList<>();

		for (int languageId : languageIds) {
			Language lang = getLanguage(languageId);
			if (lang != null) {
				langList.add(lang);
			}
		}

		return langList;
	}
}
