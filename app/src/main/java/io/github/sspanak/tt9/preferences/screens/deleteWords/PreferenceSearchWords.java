package io.github.sspanak.tt9.preferences.screens.deleteWords;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.preferences.items.SearchPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceSearchWords extends SearchPreference {
	public static final String NAME = "dictionary_delete_words_search";
	private static final String LOG_TAG = PreferenceSearchWords.class.getSimpleName();
	static final int PAGE_SIZE = SettingsStore.CUSTOM_WORDS_SEARCH_RESULTS_MAX;

	@Nullable private Activity activity;
	@Nullable private Consumer<CustomWordsPage> onPage;
	private int lastRequestId = 0;

	@NonNull private String lastSearchTerm = "";


	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceSearchWords(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceSearchWords(@NonNull Context context) { super(context); }


	@Override
	protected void onTextChange() {
		search(text);
	}

	@NonNull
	public String getLastSearchTerm() {
		return lastSearchTerm;
	}

	void search(String word) {
		lastSearchTerm = word == null || word.trim().isEmpty() ? "" : word.trim();
		loadPage(0);
	}

	/**
	 * Loads the words matching the last search term, starting from the given position. Must be
	 * called on the UI thread.
	 */
	void loadPage(int offset) {
		if (onPage == null || activity == null) {
			Logger.w(LOG_TAG, "No handler set for the word change event.");
			return;
		}

		final int requestId = ++lastRequestId;
		final String searchTerm = lastSearchTerm;
		final int start = Math.max(0, offset);

		DataStore.countCustomWords(
			(total) -> DataStore.getCustomWords(
				(words) -> deliver(requestId, new CustomWordsPage(searchTerm, start, total, words == null ? new ArrayList<>() : words)),
				searchTerm,
				PAGE_SIZE,
				start
			),
			searchTerm
		);
	}

	private void deliver(int requestId, @NonNull CustomWordsPage page) {
		final Activity act = activity;
		final Consumer<CustomWordsPage> handler = onPage;
		if (act == null || handler == null) {
			return;
		}

		act.runOnUiThread(() -> {
			// ignore stale results, when the user has typed or changed the page in the meantime
			if (requestId == lastRequestId) {
				handler.accept(page);
			}
		});
	}

	void setOnPageHandler(@NonNull Activity activity, @NonNull Consumer<CustomWordsPage> handler) {
		this.activity = activity;
		this.onPage = handler;
	}
}
