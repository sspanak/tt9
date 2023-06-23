package io.github.sspanak.tt9.languages;

import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import io.github.sspanak.tt9.Logger;

public class LanguageCollection {
	private static LanguageCollection self;

	private final HashMap<Integer, Language> languages = new HashMap<>();

	private LanguageCollection(Context context) {
		for (String file : LanguageDefinition.getAllFiles(context.getAssets())) {
			try {
				Language lang = Language.fromDefinition(LanguageDefinition.fromFile(context.getAssets(), file));
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

	public static Language getLanguage(Context context, int langId) {
		if (getInstance(context).languages.containsKey(langId)) {
			return getInstance(context).languages.get(langId);
		}

		return null;
	}

	public static Language getDefault(Context context) {
		Language language = getByLocale(context, "en");
		return language == null ? new NullLanguage(context) : language;
	}

	@Nullable
	public static Language getByLocale(Context context, String locale) {
		for (Language lang : getInstance(context).languages.values()) {
			if (lang.getLocale().toString().equals(locale)) {
				return lang;
			}
		}

		return null;
	}

	public static ArrayList<Language> getAll(Context context, ArrayList<Integer> languageIds, boolean sort) {
		ArrayList<Language> langList = new ArrayList<>();

		for (int languageId : languageIds) {
			Language lang = getLanguage(context, languageId);
			if (lang != null) {
				langList.add(lang);
			}
		}

		if (sort && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			langList.sort(Comparator.comparing(l -> l.getLocale().toString()));
		}

		return langList;
	}

	public static ArrayList<Language> getAll(Context context, ArrayList<Integer> languageIds) {
		return getAll(context, languageIds, false);
	}

	public static ArrayList<Language> getAll(Context context, boolean sort) {
		ArrayList<Language> langList = new ArrayList<>(getInstance(context).languages.values());

		if (sort && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			langList.sort(Comparator.comparing(l -> l.getLocale().toString()));
		}

		return langList;
	}

	public static ArrayList<Language> getAll(Context context) {
		return getAll(context,false);
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
