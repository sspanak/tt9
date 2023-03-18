package io.github.sspanak.tt9.languages;

import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.definitions.*;

public class LanguageCollection {
	private static LanguageCollection self;

	private final Language defaultLanguage = new English();
	private final HashMap<Integer, Language> languages = new HashMap<>();

	private LanguageCollection() {
		List<Class<? extends Language>> languageList = Arrays.asList(
			// Add languages here, to enable them in the UI and
			// please, maintain the alphabetical order.
			BrazilianPortuguese.class,
			Bulgarian.class,
			Dutch.class,
			English.class,
			French.class,
			German.class,
			Hebrew.class,
			Italian.class,
			Norwegian.class,
			Russian.class,
			Spanish.class,
			Swedish.class,
			Ukrainian.class
		);

		// initialize the language objects from the class list above.
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

	public static Language getDefault() {
		return getInstance().defaultLanguage;
	}

	public static ArrayList<Language> getAll(ArrayList<Integer> languageIds, boolean sort) {
		ArrayList<Language> langList = new ArrayList<>();

		for (int languageId : languageIds) {
			Language lang = getLanguage(languageId);
			if (lang != null) {
				langList.add(lang);
			}
		}

		if (sort && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			langList.sort(Comparator.comparing(l -> l.getLocale().toString()));
		}

		return langList;
	}

	public static ArrayList<Language> getAll(ArrayList<Integer> languageIds) {
		return getAll(languageIds, false);
	}

	public static ArrayList<Language> getAll(boolean sort) {
		ArrayList<Language> langList = new ArrayList<>(getInstance().languages.values());

		if (sort && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			langList.sort(Comparator.comparing(l -> l.getLocale().toString()));
		}

		return langList;
	}

	public static ArrayList<Language> getAll() {
		return getAll(false);
	}


	public static String toString(ArrayList<Language> list) {
		StringBuilder stringList = new StringBuilder();
		int listSize = list.size();

		for (int i = 0; i < listSize; i++) {
			stringList.append(list.get(i));
			stringList.append((i < listSize - 1) ? ", " : " ");
		}

		return stringList.toString();
	}
}
