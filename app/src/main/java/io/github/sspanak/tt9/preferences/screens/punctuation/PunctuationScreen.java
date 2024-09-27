package io.github.sspanak.tt9.preferences.screens.punctuation;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class PunctuationScreen extends BaseScreenFragment {
	public static final String NAME = "Punctuation";
	private ItemPunctuationOrderLanguage languageList;
	private ItemRestoreDefaultPunctuation restoreDefaults;

	public PunctuationScreen() { init(); }
	public PunctuationScreen(PreferencesActivity activity) { init(activity); }

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected int getTitle() {
		return R.string.pref_category_punctuation_order;
	}

	@Override
	protected int getXml() {
		return R.xml.prefs_screen_punctuation;
	}


	@Override
	protected void onCreate() {
		initLanguageList();
		initResetDefaults();
		loadCharLists();
		resetFontSize(false);
	}


	private void initLanguageList() {
		languageList = (new ItemPunctuationOrderLanguage(activity.getSettings(), findPreference(ItemPunctuationOrderLanguage.NAME)));
		languageList
			.onChange(this::onLanguageChanged)
			.enableClickHandler()
			.populate()
			.preview();
	}

	private void initResetDefaults() {
		Preference item = findPreference(ItemRestoreDefaultPunctuation.NAME);
		if (item == null) {
			return;
		}

		restoreDefaults = new ItemRestoreDefaultPunctuation(activity.getSettings(), item, this::onLanguageChanged);
		restoreDefaults
			.setLanguage(LanguageCollection.getLanguage(activity, languageList.getValue()))
			.enableClickHandler();
	}

	private void onLanguageChanged(@Nullable String newLanguageId) {
		Language language = LanguageCollection.getLanguage(activity, newLanguageId);

		restoreDefaults.setLanguage(language);

		PreferenceSpecialCharList key0 = findPreference(PreferenceSpecialCharList.NAME);
		if (key0 != null) {
			key0.onLanguageChange(language);
		}

		PreferenceSentencePunctuationList key1 = findPreference(PreferenceSentencePunctuationList.NAME);
		if (key1 != null) {
			key1.onLanguageChange(language);
		}
	}


	private void loadCharLists() {
		loadCharList(findPreference(PreferenceSpecialCharList.NAME));
		loadCharList(findPreference(PreferenceSentencePunctuationList.NAME));
	}


	private void loadCharList(AbstractPreferenceCharList list) {
		if (list == null) {
			return;
		}

		list.setOnRender(() -> {
			list.setOnRender(null);
			onLanguageChanged(languageList.getValue());
		});
	}
}
