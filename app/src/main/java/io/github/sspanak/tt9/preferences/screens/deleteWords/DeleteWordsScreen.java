package io.github.sspanak.tt9.preferences.screens.deleteWords;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class DeleteWordsScreen extends BaseScreenFragment {
	final public static String NAME = "DeleteWords";

	public DeleteWordsScreen() { super(); }
	public DeleteWordsScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_delete_words; }
	@Override protected int getXml() { return R.xml.prefs_screen_delete_words; }

	@Override protected void onCreate() {
		createPage();
	}

	private void createPage() {
		if (activity == null) {
			return;
		}

		DeletableWordsList searchResultsList = new DeletableWordsList(activity.getSettings(), findPreference(DeletableWordsList.NAME));
		searchResultsList.setTotalWords(0);
		searchResultsList.setResult("", null);

		PreferenceSearchWords searchWords = findPreference(PreferenceSearchWords.NAME);
		if (activity != null && searchWords != null) {
			searchWords.setOnWordsHandler(activity, (words) -> searchResultsList.setResult(searchWords.getLastSearchTerm(), words));
			searchWords.setOnTotalWordsHandler(activity, searchResultsList::setTotalWords);
			searchWords.search("");
		}

		resetFontSize(false);
	}
}
