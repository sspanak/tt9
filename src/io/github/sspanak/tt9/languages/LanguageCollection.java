package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.definitions.*;

public class LanguageCollection {
	private static LanguageCollection self;

	private final HashMap<Integer, Language> languages = new HashMap<>();

	private LanguageCollection() {
		List<Class<? extends Language>> languageList = Arrays.asList(
			// Add languages here, to enable them in the UI and
			// please, maintain the alphabetical order.
			Bulgarian.class,
			English.class,
			French.class,
			German.class,
			Italian.class,
			Russian.class,
			Ukrainian.class
		);

		// initialize the language objects from the class list above
		for (Class<? extends Language> languageClass : languageList) {
			try {
				Language lang = languageClass.newInstance();
				if (languages.containsKey(lang.getId())) {
					throw new Exception("Duplicate language ID: " + lang.getId() + " for language: " + lang.getName());
				}
				languages.put(lang.getId(), lang);
			} catch (Exception e) {
				Logger.e("tt9.LanguageCollection", "Skipping an invalid language. " + e.getMessage());
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
