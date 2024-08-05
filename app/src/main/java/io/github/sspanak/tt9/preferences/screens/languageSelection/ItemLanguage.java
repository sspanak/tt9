package io.github.sspanak.tt9.preferences.screens.languageSelection;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.ArrayList;
import java.util.Set;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.WordFile;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemLanguage extends SwitchPreferenceCompat {
	public static final String KEY_PREFIX = "language_";

	private final SettingsStore settings;

	public ItemLanguage(@NonNull PreferencesActivity activity, @NonNull NaturalLanguage language, ArrayList<Integer> enabledLanguageIds) {
		super(activity);

		settings = activity.getSettings();

		setKey(KEY_PREFIX + language.getId());
		setTitle(language.getName());
		setSummary(generateSummary(activity, language));
		setChecked(enabledLanguageIds.contains(language.getId()));
		setOnPreferenceChangeListener(ItemLanguage::handleChange);
	}


	private String generateSummary(Activity activity, NaturalLanguage language) {
		String summary = language.getLocale().getDisplayLanguage();

		WordFile wordFile = new WordFile(activity, language.getDictionaryFile(), activity.getAssets());
		if (wordFile.getTotalLines() > 1000000) {
			summary += String.format(", %1.2fM words", wordFile.getTotalLines() / 1000000.0);
		} else {
			summary += ", " + wordFile.getTotalLines() / 1000 + "k words";
		}

		if (BuildConfig.LITE) {
			summary += String.format(", %1.2f Mb", wordFile.getSize() / 1048576.0);
		}

		return summary;
	}


	public void setLoaded(boolean loaded) {
		Context context = getContext();

		String summary = getSummary() != null ? getSummary().toString() : null;
		if (summary == null) {
			return;
		}

		summary = summary.replace(context.getString(R.string.language_selection_language_loaded), "");

		if (loaded) {
			summary += " " + context.getString(R.string.language_selection_language_loaded);
		}

		setSummary(summary);
	}


	private String getLanguageId() {
		return getKey().substring(KEY_PREFIX.length());
	}


	private static boolean handleChange(Preference p, Object newValue) {
		SettingsStore settings = ((PreferencesActivity) p.getContext()).getSettings();
		String languageSettingsId = ((ItemLanguage) p).getLanguageId();

		Set<String> enabledLanguages = settings.getEnabledLanguagesIdsAsStrings();
		if ((boolean) newValue) {
			enabledLanguages.add(languageSettingsId);
		} else {
			enabledLanguages.remove(languageSettingsId);
		}

		settings.saveEnabledLanguageIds(enabledLanguages);

		return true;
	}
}
