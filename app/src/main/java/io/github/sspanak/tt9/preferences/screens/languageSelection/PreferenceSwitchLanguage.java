package io.github.sspanak.tt9.preferences.screens.languageSelection;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Set;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.WordFile;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class PreferenceSwitchLanguage extends SwitchPreferenceCompat {
	public static final String KEY_PREFIX = "language_";

	public PreferenceSwitchLanguage(@NonNull PreferencesActivity activity, @NonNull NaturalLanguage language) {
		super(activity);

		setKey(KEY_PREFIX + language.getId());
		setTitle(language.getName());
		setSummary(generateSummary(activity, language));
		setOnPreferenceChangeListener(PreferenceSwitchLanguage::handleChange);
	}


	private String generateSummary(Activity activity, NaturalLanguage language) {
		String summary = language.getLocale().getDisplayLanguage();

		String wordsString = activity.getString(R.string.language_selection_words);
		WordFile wordFile = new WordFile(activity, language.getDictionaryFile(), activity.getAssets());
		if (wordFile.getTotalLines() > 1000000) {
			summary += String.format(", %1.2fM %s", wordFile.getTotalLines() / 1000000.0, wordsString);
		} else {
			summary += ", " + wordFile.getTotalLines() / 1000 + "k " + wordsString;
		}

		if (BuildConfig.LITE) {
			summary += String.format(", %1.2f Mb", wordFile.getSize() / 1048576.0);
		}

		return summary;
	}


	private String getLanguageId() {
		return getKey().substring(KEY_PREFIX.length());
	}


	private static boolean handleChange(Preference p, Object newValue) {
		SettingsStore settings = ((PreferencesActivity) p.getContext()).getSettings();
		String languageSettingsId = ((PreferenceSwitchLanguage) p).getLanguageId();

		Set<String> enabledLanguages = settings.getEnabledLanguagesIdsAsStrings();
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
