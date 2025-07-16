package io.github.sspanak.tt9.preferences.screens.deleteWords;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.preferences.items.SearchPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceSearchWords extends SearchPreference {
	public static final String NAME = "dictionary_delete_words_search";
	private static final String LOG_TAG = PreferenceSearchWords.class.getSimpleName();

	private ConsumerCompat<ArrayList<String>> onWords;
	private ConsumerCompat<Long> onTotalWords;

	@NonNull private String lastSearchTerm = "";


	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceSearchWords(@NonNull Context context) { super(context); }


	@Override
	protected void onTextChange() {
		search(text, Logger.isDebugLevel());
	}

	@NonNull
	public String getLastSearchTerm() {
		return lastSearchTerm;
	}

	void search(String word, boolean withDebugInfo) {
		lastSearchTerm = word == null || word.trim().isEmpty() ? "" : word.trim();

		if (onWords == null) {
			Logger.w(LOG_TAG, "No handler set for the word change event.");
		} else if (lastSearchTerm.isEmpty()) {
			DataStore.countCustomWords(onTotalWords);
			DataStore.getCustomWords(onWords, lastSearchTerm, SettingsStore.CUSTOM_WORDS_SEARCH_RESULTS_MAX, withDebugInfo);
		} else {
			DataStore.countCustomWords(onTotalWords);
			DataStore.getCustomWords(onWords, lastSearchTerm, -1, withDebugInfo);
		}
	}

	void setOnWordsHandler(ConsumerCompat<ArrayList<String>> onWords) {
		this.onWords = onWords;
	}

	void setOnTotalWordsHandler(ConsumerCompat<Long> onTotalWords) {
		this.onTotalWords = onTotalWords;
	}
}
