package io.github.sspanak.tt9.languages;

import java.util.HashMap;

public class LanguageHelper {
	private static LanguageHelper self;

	private final HashMap<Integer, Language> languages = new HashMap<>();

	private LanguageHelper() {
		Language lang = new English();
		languages.put(lang.getId(), lang);

		lang = new Russian();
		languages.put(lang.getId(), lang);
	}

	public static LanguageHelper getInstance() {
		if (self == null) {
			self = new LanguageHelper();
		}

		return self;
	}

	public static Language getLanguage(int langId) {
		return getInstance().languages.get(langId);
	}
}
