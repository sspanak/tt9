package io.github.sspanak.tt9.preferences.screens.deleteWords;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.Logger;

public class DeleteWordsScreen extends BaseScreenFragment {
	final public static String NAME = "DeleteWords";

	public DeleteWordsScreen() { init(); }
	public DeleteWordsScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_delete_words; }
	@Override protected int getXml() { return R.xml.prefs_screen_delete_words; }

	@Override protected void onCreate() {
		createPage();
	}

	private void createPage() {
		DeletableWordsList searchResultsList = new DeletableWordsList(activity.getSettings(), findPreference(DeletableWordsList.NAME));
		searchResultsList.setTotalWords(0);
		searchResultsList.setResult("", null);

		PreferenceSearchWords searchWords = findPreference(PreferenceSearchWords.NAME);
		if (searchWords != null) {
			searchWords.setOnWordsHandler((words) -> searchResultsList.setResult(searchWords.getLastSearchTerm(), words));
			searchWords.setOnTotalWordsHandler(searchResultsList::setTotalWords);
			searchWords.search("", Logger.isDebugLevel());
		}

		resetFontSize(false);
	}
}
