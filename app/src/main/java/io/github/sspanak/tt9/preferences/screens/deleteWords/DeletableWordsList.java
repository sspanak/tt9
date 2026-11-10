package io.github.sspanak.tt9.preferences.screens.deleteWords;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import java.util.ArrayList;
import java.util.function.IntConsumer;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.entities.CustomWord;
import io.github.sspanak.tt9.preferences.custom.PreferencePlainText;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

class DeletableWordsList {
	static final String NAME = "delete_words_list";

	private final PreferenceCategory item;
	private final boolean largeFont;
	private final int pageSize;
	@Nullable private IntConsumer onPageChange;
	private final ArrayList<Preference> previousLinks = new ArrayList<>();
	private final ArrayList<Preference> nextLinks = new ArrayList<>();

	private int offset = 0;
	private int currentWords = 0;
	private long totalMatching = 0;

	DeletableWordsList(SettingsStore settings, Preference preference, int pageSize) {
		item = preference instanceof PreferenceCategory ? (PreferenceCategory) preference : null;
		largeFont = settings.getSettingsFontSize() == SettingsStore.FONT_SIZE_LARGE;
		this.pageSize = pageSize;
	}

	void setOnPageChangeHandler(@Nullable IntConsumer handler) {
		onPageChange = handler;
	}

	private void clear() {
		if (item != null) {
			item.removeAll();
		}
		previousLinks.clear();
		nextLinks.clear();
	}

	void delete(PreferenceDeletableWord wordItem) {
		if (item == null) {
			return;
		}

		item.removePreference(wordItem);
		currentWords = Math.max(0, currentWords - 1);
		totalMatching = Math.max(0, totalMatching - 1);

		if (currentWords == 0 && totalMatching > 0) {
			// the page is now empty, show the words that were after it, or the previous page
			changePage(offset < totalMatching ? offset : offset - pageSize);
		} else {
			setResultCount();
			updateNavigation();
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

	private void addWords(ArrayList<CustomWord> words) {
		for (CustomWord word : words) {
			addWord(word);
		}
	}

	private void addNoResult(boolean noSearchTerm) {
		if (item != null) {
			PreferencePlainText pref = new PreferencePlainText(item.getContext());
			pref.setSummary(noSearchTerm ? "--" : item.getContext().getString(R.string.search_results_void));
			pref.setLayoutResource(largeFont ? pref.getLargeLayout() : pref.getDefaultLayout());
			item.addPreference(pref);
		}
	}

	/**
	 * Adds "previous" and "next" page links. The links are added at both the top and the bottom of
	 * the list, so that they are quickly reachable with a keypad, too.
	 */
	private void addNavigation() {
		if (item == null) {
			return;
		}

		addNavigationLink(R.string.delete_words_page_previous, true);
		addNavigationLink(R.string.delete_words_page_next, false);
	}

	private void addNavigationLink(int title, boolean previous) {
		PreferencePlainText pref = new PreferencePlainText(item.getContext());
		pref.setSummary(item.getContext().getString(title));
		pref.setLayoutResource(largeFont ? pref.getLargeLayout() : pref.getDefaultLayout());
		pref.setPersistent(false);
		pref.setOnPreferenceClickListener((p) -> {
			changePage(previous ? offset - pageSize : offset + currentWords);
			return true;
		});
		item.addPreference(pref);
		(previous ? previousLinks : nextLinks).add(pref);
	}

	private void updateNavigation() {
		boolean hasPrevious = offset > 0;
		boolean hasNext = offset + currentWords < totalMatching;

		for (Preference pref : previousLinks) {
			pref.setVisible(hasPrevious);
		}
		for (Preference pref : nextLinks) {
			pref.setVisible(hasNext);
		}
	}

	private void changePage(int newOffset) {
		if (onPageChange != null) {
			onPageChange.accept(Math.max(0, newOffset));
		}
	}

	void setResult(@NonNull CustomWordsPage page) {
		if (page.words().isEmpty() && page.offset() > 0 && page.totalMatching() > 0) {
			// we went past the end (e.g. some words were deleted elsewhere), so go to the last page
			int lastPageOffset = (int) ((page.totalMatching() - 1) / pageSize * pageSize);
			if (lastPageOffset < page.offset()) {
				changePage(lastPageOffset);
				return;
			}
		}

		clear();

		offset = page.offset();
		currentWords = page.words().size();
		totalMatching = page.totalMatching();

		if (page.words().isEmpty()) {
			addNoResult(page.searchTerm().isEmpty());
		} else {
			addNavigation();
			addWords(page.words());
			addNavigation();
		}

		setResultCount();
		updateNavigation();
	}

	private void setResultCount() {
		if (item == null) {
			return;
		}

		String results = currentWords == 0
			? " (0/" + totalMatching + ")"
			: " (" + (offset + 1) + "–" + (offset + currentWords) + "/" + totalMatching + ")";

		item.setTitle(item.getContext().getString(R.string.search_results) + results);
	}
}
