package io.github.sspanak.tt9.preferences.screens.languageSelection;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceCategory;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class LanguageSelectionScreen  extends BaseScreenFragment {
	public static final String NAME = "LanguageSelection";

	private PreferenceCategory languagesCategory;
	private final ArrayList<PreferenceSwitchLanguage> languageItems = new ArrayList<>();

	public LanguageSelectionScreen() { init(); }
	public LanguageSelectionScreen(PreferencesActivity activity) { init(activity); }


	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.language_selection_title; }
	@Override protected int getXml() { return R.xml.prefs_screen_language_selection; }


	@Override
	protected void onCreate() {
		createLanguageList();
		enableFiltering();
		resetFontSize(false);
	}


	private void createLanguageList() {
		languagesCategory = findPreference("language_list");
		if (languagesCategory == null) {
			return;
		}

		ArrayList<Language> allLanguages = LanguageCollection.getAll(activity, true);
		if (allLanguages.isEmpty()) {
			return;
		}

		addLanguagesToCategory(languagesCategory, allLanguages);
		WordStoreAsync.exists(this::addLoadedStatus, allLanguages);
	}


	private void addLanguagesToCategory(@NonNull PreferenceCategory category, ArrayList<Language> allLanguages) {
		ArrayList<Integer> enabledLanguageIds = activity.getSettings().getEnabledLanguageIds();

		languageItems.clear();

		for (Language language : allLanguages) {
			if (language instanceof NaturalLanguage) {
				PreferenceSwitchLanguage item = new PreferenceSwitchLanguage(activity, (NaturalLanguage) language, enabledLanguageIds);
				languageItems.add(item);
				category.addPreference(item);
			}
		}
	}


	private void addLoadedStatus(ArrayList<Integer> enabledLanguageIds) {
		if (languagesCategory == null) {
			return;
		}

		activity.runOnUiThread(() -> {
			for (int languageId : enabledLanguageIds) {
				PreferenceSwitchLanguage item = languagesCategory.findPreference(PreferenceSwitchLanguage.KEY_PREFIX + languageId);
				if (item != null) {
					item.setLoaded();
				}
			}
		});
	}


	private void enableFiltering() {
		PreferenceSearchLanguage search = findPreference("language_search");
		if (search != null) {
			search
				.setLanguageItems(languageItems)
				.setNoResultItem(findPreference("language_search_no_result"));
		}
	}
}
