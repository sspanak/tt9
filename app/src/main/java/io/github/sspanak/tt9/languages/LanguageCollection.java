package io.github.sspanak.tt9.languages;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.SystemSettings;

public class LanguageCollection {
	private static LanguageCollection self;

	private final HashMap<Integer, NaturalLanguage> languages = new HashMap<>();

	private LanguageCollection(Context context) {
		for (String file : LanguageDefinition.getAllFiles(context.getAssets())) {
			try {
				NaturalLanguage lang = NaturalLanguage.fromDefinition(LanguageDefinition.fromFile(context.getAssets(), file));
				languages.put(lang.getId(), lang);
			} catch (Exception e) {
				Logger.e("tt9.LanguageCollection", "Skipping invalid language: '" + file + "'. " + e.getMessage());
			}
		}
	}


	public static LanguageCollection getInstance(Context context) {
		if (self == null) {
			self = new LanguageCollection(context);
		}

		return self;
	}

	@Nullable
	public static NaturalLanguage getLanguage(Context context, int langId) {
		if (getInstance(context).languages.containsKey(langId)) {
			return getInstance(context).languages.get(langId);
		}

		return null;
	}

	@NonNull public static Language getDefault(Context context) {
		Language language = getByLocale(context, SystemSettings.getLocale());
		language = language == null ? getByLocale(context, "en") : language;
		return language == null ? new NullLanguage(context) : language;
	}

	@Nullable
	public static NaturalLanguage getByLocale(Context context, String locale) {
		for (NaturalLanguage lang : getInstance(context).languages.values()) {
			if (lang.getLocale().toString().equals(locale)) {
				return lang;
			}
		}

		return null;
	}

	public static ArrayList<Language> getAll(Context context, ArrayList<Integer> languageIds, boolean sort) {
		if (languageIds == null) {
			return new ArrayList<>();
		}

		ArrayList<NaturalLanguage> langList = new ArrayList<>();
		for (int languageId : languageIds) {
			NaturalLanguage lang = getLanguage(context, languageId);
			if (lang != null) {
				langList.add(lang);
			}
		}

		if (sort) {
			Collections.sort(langList);
		}

		return new ArrayList<>(langList);
	}

	public static ArrayList<Language> getAll(Context context, ArrayList<Integer> languageIds) {
		return getAll(context, languageIds, false);
	}

	public static ArrayList<Language> getAll(Context context, boolean sort) {
		ArrayList<NaturalLanguage> langList = new ArrayList<>(getInstance(context).languages.values());

		if (sort) {
			Collections.sort(langList);
		}

		return new ArrayList<>(langList);
	}

	public static ArrayList<Language> getAll(Context context) {
		return getAll(context,false);
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
