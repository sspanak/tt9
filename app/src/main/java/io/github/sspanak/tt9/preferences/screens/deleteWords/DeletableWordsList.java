package io.github.sspanak.tt9.preferences.screens.deleteWords;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.preferences.custom.PreferencePlainText;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

class DeletableWordsList {
	static final String NAME = "delete_words_list";

	private final PreferenceCategory item;
	private final boolean largeFont;
	private int currentWords = 0;
	private long totalWords = 0;

	DeletableWordsList(SettingsStore settings, Preference preference) {
		item = preference instanceof PreferenceCategory ? (PreferenceCategory) preference : null;
		largeFont = settings.getSettingsFontSize() == SettingsStore.FONT_SIZE_LARGE;
	}

	private void clear() {
		if (item != null) {
			item.removeAll();
		}
	}

	void delete(PreferenceDeletableWord wordItem) {
		if (item != null) {
			item.removePreference(wordItem);
			setTotalWords(totalWords - 1);
		}
	}

	private void addWord(CustomWord word) {
		if (item != null) {
			PreferenceDeletableWord pref = new PreferenceDeletableWord(item.getContext());
			pref.setParent(this);
			pref.setWord(word);
			pref.setLayoutResource(largeFont ? pref.getLargeLayout() : pref.getDefaultLayout());
			item.addPreference(pref);
		}
	}

	void addWords(ArrayList<CustomWord> words) {
		for (CustomWord word : words) {
			addWord(word);
		}
	}

	void addNoResult(boolean noSearchTerm) {
		if (item != null) {
			PreferencePlainText pref = new PreferencePlainText(item.getContext());
			pref.setSummary(noSearchTerm ? "--" : item.getContext().getString(R.string.search_results_void));
			pref.setLayoutResource(largeFont ? pref.getLargeLayout() : pref.getDefaultLayout());
			item.addPreference(pref);
		}
	}

	void setResult(@NonNull String searchTerm, ArrayList<CustomWord> words) {
		clear();

		if (words == null || words.isEmpty()) {
			addNoResult(searchTerm.isEmpty());
		} else {
			addWords(words);
		}

		currentWords = words == null ? 0 : words.size();
		setResultCount();
	}

	void setTotalWords(long total) {
		totalWords = total > 0 ? total : 0;
		setResultCount();
	}

	private void setResultCount() {
		if (item != null) {
			String results = " (" + currentWords + "/" + totalWords + ")";
			item.setTitle(item.getContext().getString(R.string.search_results) + results);
		}
	}
}
