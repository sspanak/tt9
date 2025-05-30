package io.github.sspanak.tt9.preferences.screens.punctuation;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.ui.UI;

public class PunctuationScreen extends BaseScreenFragment {
	public static final String NAME = "Punctuation";
	private ItemPunctuationOrderLanguage languageList;
	private ItemRestoreDefaultPunctuation restoreDefaults;
	private final ArrayList<AbstractPreferenceCharList> charLists = new ArrayList<>();

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
	public void onBackPressed() {
		onSaveOrdering();
	}


	@Override
	protected void onCreate() {
		charLists.add(findPreference(PreferenceChars0.NAME));
		charLists.add(findPreference(PreferenceChars1.NAME));
		for (int i = 0; i < PreferenceCharsExtra.NAMES.length; i++) {
			charLists.add(findPreference(PreferenceCharsExtra.NAMES[i]));
		}

		initLanguageList();
		initResetDefaults();
		initSaveButton();
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


	private void initSaveButton() {
		Preference item = findPreference(ItemPunctuationOrderSave.NAME);
		if (item != null) {
			new ItemPunctuationOrderSave(item, this::onSaveOrdering).enableClickHandler();
		}
	}


	private void initResetDefaults() {
		Preference item = findPreference(ItemRestoreDefaultPunctuation.NAME);
		if (item == null) {
			return;
		}

		restoreDefaults = new ItemRestoreDefaultPunctuation(activity.getSettings(), item, this::onLanguageChanged);
		restoreDefaults
			.setLanguage(LanguageCollection.getLanguage(languageList.getValue()))
			.enableClickHandler();
	}


	private void onSaveOrdering() {
		for (AbstractPreferenceCharList charList : charLists) {
			if (charList == null || !charList.validateCurrentChars()) {
				UI.toastShortSingle(activity, R.string.punctuation_order_save_error);
				return;
			}
		}

		for (AbstractPreferenceCharList charList : charLists) {
			charList.saveCurrentChars();
		}
	}


	private void onLanguageChanged(@Nullable String newLanguageId) {
		Language language = LanguageCollection.getLanguage(newLanguageId);

		restoreDefaults.setLanguage(language);

		for (AbstractPreferenceCharList list : charLists) {
			if (list != null) {
				list.onLanguageChange(language);
			}
		}
	}


	private void loadCharLists() {
		if (activity != null) {
			for (Language lang : LanguageCollection.getAll(activity.getSettings().getEnabledLanguageIds())) {
				activity.getSettings().setDefaultCharOrder(lang, false);
			}
		}
		onLanguageChanged(languageList.getValue());
	}
}
