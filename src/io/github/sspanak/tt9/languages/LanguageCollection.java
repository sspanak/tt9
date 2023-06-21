package io.github.sspanak.tt9.languages;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.definitions.English;
import io.github.sspanak.tt9.ui.UI;

public class LanguageCollection {
	private static LanguageCollection self;

	private final Language defaultLanguage = new English();
	private final HashMap<Integer, Language> languages = new HashMap<>();

	private LanguageCollection(Context context) {
		ArrayList<LanguageDefinition> definitions = new ArrayList<>();

		try {
			definitions = LanguageDefinition.getAll(context.getAssets());
		} catch (Exception e) {
			UI.toastLong(context, context.getString(R.string.failed_loading_language_definitions));
			Logger.e("tt9.LanguageCollection", e.getMessage());
		}

		for (LanguageDefinition definition : definitions) {
			try {
				Language lang = Language.fromDefinition(definition);
				if (languages.containsKey(lang.getId())) {
					throw new Exception("Duplicate language ID: " + lang.getId() + " for language: " + lang.getName());
				}
				languages.put(lang.getId(), lang);
			} catch (Exception e) {
				Logger.e("tt9.LanguageCollection", "Skipping an invalid language. " + e.getMessage());
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
		return getInstance(context).defaultLanguage;
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
