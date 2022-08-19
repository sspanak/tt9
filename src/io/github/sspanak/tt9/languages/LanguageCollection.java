package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.HashMap;

public class LanguageCollection {
	private static LanguageCollection self;

	private final HashMap<Integer, Language> languages = new HashMap<>();

	private LanguageCollection() {
		Language lang = new English();
		languages.put(lang.getId(), lang);

		lang = new Russian();
		languages.put(lang.getId(), lang);

		lang = new Bulgarian();
		languages.put(lang.getId(), lang);
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
			langList.add(getLanguage(languageId));
		}

		return langList;
	}
}
