package io.github.sspanak.tt9.preferences.screens.languageSelection;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.WordFile;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class PreferenceSwitchLanguage extends SwitchPreferenceCompat {
	public static final String KEY_PREFIX = "language_";

	private static final ArrayList<PreferenceSwitchLanguage> items = new ArrayList<>();

	public PreferenceSwitchLanguage(@NonNull PreferencesActivity activity, @NonNull NaturalLanguage language) {
		super(activity);

		setKey(KEY_PREFIX + language.getId());
		setTitle(language.getName());
		setSummary(generateSummary(activity, language));
		setOnPreferenceChangeListener(PreferenceSwitchLanguage::handleChange);
		items.add(this);
	}


	static void clearItems() {
		items.clear();
	}

	static ArrayList<PreferenceSwitchLanguage> getItems() {
		return new ArrayList<>(items);
	}


	private String generateSummary(Activity activity, NaturalLanguage language) {
		// name
		StringBuilder summary = new StringBuilder(
			LanguageKind.isHinglish(language) ? language.getName() : language.getLocale().getDisplayLanguage()
		);

		// word count
		WordFile wordFile = new WordFile(activity, language.getDictionaryFile(), activity.getAssets());
		summary
			.append(", ")
			.append(
			wordFile.getFormattedTotalLines(activity.getString(R.string.language_selection_words))
		);

		// download size
		if (BuildConfig.LITE) {
			summary.append(", ").append(wordFile.getFormattedSize());
		}

		return summary.toString();
	}


	private String getLanguageId() {
		return getKey().substring(KEY_PREFIX.length());
	}


	private static boolean handleChange(Preference p, Object newValue) {
		SettingsStore settings = ((PreferencesActivity) p.getContext()).getSettings();
		String languageSettingsId = ((PreferenceSwitchLanguage) p).getLanguageId();

		Set<String> enabledLanguages = new HashSet<>();

		for (PreferenceSwitchLanguage item : items) {
			if (item.isChecked()) {
				enabledLanguages.add(item.getLanguageId());
			}
		}

		if ((boolean) newValue) {
			enabledLanguages.add(languageSettingsId);
		} else {
			enabledLanguages.remove(languageSettingsId);
		}

		settings.saveEnabledLanguageIds(enabledLanguages);

		return true;
	}


	void setLoaded() {
		Context context = getContext();

		String summary = getSummary() != null ? getSummary().toString() : null;
		if (summary == null) {
			return;
		}

		summary += " " + context.getString(R.string.language_selection_language_loaded);

		setSummary(summary);
	}
}
