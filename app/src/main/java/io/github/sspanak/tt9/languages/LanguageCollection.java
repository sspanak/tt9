package io.github.sspanak.tt9.languages;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.SystemSettings;

public class LanguageCollection {
	private static LanguageCollection self;

	private final HashMap<Integer, NaturalLanguage> languages;

	private LanguageCollection(Context context) {
		languages = new HashMap<>();

		for (LanguageDefinition definition : LanguageDefinition.getAll(context.getAssets())) {
			try {
				NaturalLanguage lang = NaturalLanguage.fromDefinition(definition);
				languages.put(lang.getId(), lang);
			} catch (Exception e) {
				Logger.e("tt9.LanguageCollection", "Skipping invalid language: '" + definition.name + "'. " + e.getMessage());
			}
		}
	}


	public static void init(Context context) {
		if (self == null) {
			self = new LanguageCollection(context);
		}
	}


	@Nullable
	public static NaturalLanguage getLanguage(String langId) {
		try {
			return getLanguage(Integer.parseInt(langId));
		} catch (NumberFormatException e) {
			return null;
		}
	}


	@Nullable
	public static NaturalLanguage getLanguage(int langId) {
		if (self.languages.containsKey(langId)) {
			return self.languages.get(langId);
		}

		return null;
	}

	@NonNull public static Language getDefault() {
		Language language = getByLocale(SystemSettings.getLocale());
		language = language == null ? getByLocale("en") : language;
		return language == null ? new NullLanguage() : language;
	}

	@Nullable
	public static NaturalLanguage getByLanguageCode(String languageCode) {
		for (NaturalLanguage lang : self.languages.values()) {
			if (lang.getLocale().getLanguage().equals(new Locale(languageCode).getLanguage())) {
				return lang;
			}
		}

		return null;
	}

	@Nullable
	public static NaturalLanguage getByLocale(String locale) {
		for (NaturalLanguage lang : self.languages.values()) {
			if (lang.getLocale().toString().equals(locale)) {
				return lang;
			}
		}

		return null;
	}

	public static ArrayList<Language> getAll(ArrayList<Integer> languageIds, boolean sort) {
		if (languageIds == null) {
			return new ArrayList<>();
		}

		ArrayList<NaturalLanguage> langList = new ArrayList<>();
		for (int languageId : languageIds) {
			NaturalLanguage lang = getLanguage(languageId);
			if (lang != null) {
				langList.add(lang);
			}
		}

		if (sort) {
			Collections.sort(langList);
		}

		return new ArrayList<>(langList);
	}

	public static ArrayList<Language> getAll(ArrayList<Integer> languageIds) {
		return getAll(languageIds, false);
	}

	public static ArrayList<Language> getAll(boolean sort) {
		ArrayList<NaturalLanguage> langList = new ArrayList<>(self.languages.values());

		if (sort) {
			Collections.sort(langList);
		}

		return new ArrayList<>(langList);
	}

	public static ArrayList<Language> getAll() {
		return getAll(false);
	}

	public static String toString(ArrayList<Language> list) {
		StringBuilder stringList = new StringBuilder();
		int listSize = list != null ? list.size() : 0;

		for (int i = 0; i < listSize; i++) {
			stringList.append(list.get(i));
			if (i < listSize - 1) {
				stringList.append(", ");
			}
		}

		return stringList.toString();
	}
}
