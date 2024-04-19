package io.github.sspanak.tt9.preferences.screens.deleteWords;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;

class DeletableWordsList {
	static final String NAME = "delete_words_list";

	private final PreferenceCategory item;

	DeletableWordsList(Preference preference) {
		item = preference instanceof PreferenceCategory ? (PreferenceCategory) preference : null;
	}

	private void clear() {
		if (item != null) {
			item.removeAll();
		}
	}

	private void addWord(String word) {
		if (item != null) {
			PreferenceDeletableWord pref = new PreferenceDeletableWord(item.getContext());
			pref.setWord(word);
			pref.setLayoutResource(R.layout.pref_deletable_word);
			item.addPreference(pref);
		}
	}

	void addWords(ArrayList<String> words) {
		for (String word : words) {
			addWord(word);
		}
	}

	void addNoResult(boolean noSearchTerm) {
		if (item != null) {
			Preference pref = new Preference(item.getContext());
			pref.setSummary(noSearchTerm ? "--" : item.getContext().getString(R.string.delete_words_no_result));
			pref.setLayoutResource(R.layout.pref_text);
			item.addPreference(pref);
		}
	}

	void setResult(@NonNull String searchTerm, ArrayList<String> words) {
		clear();
		if (words == null || words.isEmpty()) {
			addNoResult(searchTerm.isEmpty());
		} else {
			addWords(words);
		}
	}
}
