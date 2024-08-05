package io.github.sspanak.tt9.preferences.screens.languageSelection;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.preference.SwitchPreferenceCompat;

import java.util.ArrayList;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.WordFile;
import io.github.sspanak.tt9.languages.NaturalLanguage;

public class ItemLanguage extends SwitchPreferenceCompat {
	public ItemLanguage(@NonNull Activity activity, @NonNull NaturalLanguage language, ArrayList<Integer> enabledLanguageIds) {
		super(activity);

		setKey("language_" + language.getId());
		setTitle(language.getName());
		setSummary(generateSummary(activity, language));
		setChecked(enabledLanguageIds.contains(language.getId()));
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
}
