package io.github.sspanak.tt9.preferences.screens.deleteWords;

import androidx.annotation.Nullable;

import java.util.ArrayList;

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

		DeletableWordsList searchResultsList = new DeletableWordsList(activity.getSettings(), findPreference(DeletableWordsList.NAME), PreferenceSearchWords.PAGE_SIZE);
		searchResultsList.setResult(new CustomWordsPage("", 0, 0, new ArrayList<>()));

		PreferenceSearchWords searchWords = findPreference(PreferenceSearchWords.NAME);
		if (searchWords != null) {
			searchWords.setOnPageHandler(activity, searchResultsList::setResult);
			searchResultsList.setOnPageChangeHandler(searchWords::loadPage);
			searchWords.search("");
		}

		resetFontSize(false);
	}
}
