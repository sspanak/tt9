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
	private ItemPunctuationOrderSave saveOrder;
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
		Language initalLanguage = LanguageCollection.getLanguage(languageList.getValue());
		initResetDefaults(initalLanguage);
		initSaveButton(initalLanguage);
		initIncludeSwitches(initalLanguage);
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


	private void initIncludeSwitches(Language language) {
		PreferenceIncludeTab includeTab = findPreference(PreferenceIncludeTab.NAME);
		if (includeTab != null && language != null) {
			includeTab.setLanguage(activity.getSettings(), language);
			includeTab.setOnChange(this::onSaveOrdering);
		}

		PreferenceIncludeNewline includeNewline = findPreference(PreferenceIncludeNewline.NAME);
		if (includeNewline != null && language != null) {
			includeNewline.setLanguage(activity.getSettings(), language);
			includeNewline.setOnChange(this::onSaveOrdering);
		}
	}


	private void initSaveButton(Language initialLanguage) {
		Preference item = findPreference(ItemPunctuationOrderSave.NAME);
		if (item != null) {
			saveOrder = new ItemPunctuationOrderSave(item, this::onSaveOrdering).setLanguage(initialLanguage);
			saveOrder.enableClickHandler();
		}
	}


	private void initResetDefaults(Language initialLanguage) {
		Preference item = findPreference(ItemRestoreDefaultPunctuation.NAME);
		if (item == null) {
			return;
		}

		restoreDefaults = new ItemRestoreDefaultPunctuation(activity.getSettings(), item, this::onLanguageChanged);
		restoreDefaults
			.setLanguage(initialLanguage)
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

		initIncludeSwitches(language);
		restoreDefaults.setLanguage(language);
		saveOrder.setLanguage(language);

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
