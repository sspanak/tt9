package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class LongPositionsCache {
	private final HashMap<Integer, HashMap<String, Integer>> positions = new HashMap<>();

	public boolean contains(@NonNull Language language) {
		return positions.containsKey(language.getId());
	}

	public void put(@NonNull Language language, @NonNull String sequence, int wordCount) {
		if (wordCount < SettingsStore.SUGGESTIONS_POSITIONS_LIMIT && !contains(language)) {
			positions.put(language.getId(), null);
			return;
		}

		HashMap<String, Integer> words = positions.get(language.getId());
		if (words == null) {
			words = new HashMap<>();
			positions.put(language.getId(), words);
		}
		words.put(sequence, wordCount);
	}

	public int get(@NonNull Language language, @NonNull String sequence) {
		if (!contains(language)) {
			return SettingsStore.SUGGESTIONS_POSITIONS_LIMIT;
		}

		HashMap<String, Integer> words = positions.get(language.getId());
		if (words == null) {
			return SettingsStore.SUGGESTIONS_POSITIONS_LIMIT;
		}

		Integer wordCount = words.get(sequence);
		return wordCount == null ? SettingsStore.SUGGESTIONS_POSITIONS_LIMIT : wordCount;
	}
}
