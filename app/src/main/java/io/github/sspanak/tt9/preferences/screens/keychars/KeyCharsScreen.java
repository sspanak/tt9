package io.github.sspanak.tt9.preferences.screens.keychars;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.ui.UI;

public class KeyCharsScreen extends BaseScreenFragment {
	public static final String NAME = "KeyChars";
	@Nullable private DropDownPunctuationOrderLanguage languageList;
	private ItemRestoreDefaultPunctuation restoreDefaults;
	private ItemPunctuationOrderSave saveOrder;
	private final ArrayList<AbstractPreferenceCharList> charLists = new ArrayList<>();

	public KeyCharsScreen() { super(); }
	public KeyCharsScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected int getTitle() {
		return R.string.pref_category_key_chars;
	}

	@Override
	protected int getXml() {
		return R.xml.prefs_screen_key_chars;
	}


	@Override
	public void onStop() {
		onSaveOrdering();
		super.onStop();
	}


	@Override
	protected void onCreate() {
		charLists.add(findPreference(PreferenceChars0.NAME));
		charLists.add(findPreference(PreferenceChars1.NAME));
		for (int i = 0; i < PreferenceCharsExtra.NAMES.length; i++) {
			charLists.add(findPreference(PreferenceCharsExtra.NAMES[i]));
		}
		for (int i = 0; i < PreferenceChars2to9.NAMES.length; i++) {
			charLists.add(findPreference(PreferenceChars2to9.NAMES[i]));
		}

		initLanguageList();
		Language initalLanguage = languageList != null ? LanguageCollection.getLanguage(languageList.getValue()) : null;
		init2to9Section(initalLanguage);
		initResetDefaults(initalLanguage);
		initSaveButton(initalLanguage);
		initIncludeSwitches(initalLanguage);
		loadCharLists(initalLanguage);
		resetFontSize(false);
	}


	private void init2to9Section(@Nullable Language language) {
		final Preference category = findPreference("category_extra_chars_2_to_9");
		if (category != null) {
			category.setVisible(language != null && !language.isTranscribed());
		}
	}


	private void initLanguageList() {
		languageList = findPreference(DropDownPunctuationOrderLanguage.NAME);
		if (languageList == null || activity == null) {
			return;
		}

		languageList.setOnChangeListener(this::onLanguageChanged);
		languageList
			.populate(activity.getSettings())
			.preview();
	}


	private void initIncludeSwitches(@Nullable Language language) {
		PreferenceIncludeTab includeTab = findPreference(PreferenceIncludeTab.NAME);
		if (includeTab != null && language != null && activity != null) {
			includeTab.setLanguage(activity.getSettings(), language);
			includeTab.setOnChange(this::onSaveOrdering);
		}

		PreferenceIncludeNewline includeNewline = findPreference(PreferenceIncludeNewline.NAME);
		if (includeNewline != null && language != null && activity != null) {
			includeNewline.setLanguage(activity.getSettings(), language);
			includeNewline.setOnChange(this::onSaveOrdering);
		}
	}


	private void initSaveButton(@Nullable Language initialLanguage) {
		Preference item = findPreference(ItemPunctuationOrderSave.NAME);
		if (item != null) {
			saveOrder = new ItemPunctuationOrderSave(item, this::onSaveOrdering).setLanguage(initialLanguage);
			saveOrder.enableClickHandler();
		}
	}


	private void initResetDefaults(@Nullable Language initialLanguage) {
		Preference item = findPreference(ItemRestoreDefaultPunctuation.NAME);
		if (item == null || activity == null) {
			return;
		}

		restoreDefaults = new ItemRestoreDefaultPunctuation(activity.getSettings(), item, this::onLanguageChanged);
		restoreDefaults
			.setLanguage(initialLanguage)
			.enableClickHandler();
	}


	private void onSaveOrdering() {
		if (activity == null) {
			return;
		}

		for (AbstractPreferenceCharList charList : charLists) {
			if (charList == null || !charList.validateCurrentChars()) {
				UI.toastShortSingle(activity, R.string.key_chars_save_error);
				return;
			}
		}

		for (AbstractPreferenceCharList charList : charLists) {
			charList.saveCurrentChars();
		}
	}


	private void onLanguageChanged(@Nullable String newLanguageId) {
		Language language = LanguageCollection.getLanguage(newLanguageId);

		init2to9Section(language);
		initIncludeSwitches(language);
		restoreDefaults.setLanguage(language);
		saveOrder.setLanguage(language);

		for (AbstractPreferenceCharList list : charLists) {
			if (list != null) {
				list.onLanguageChange(language);
			}
		}
	}


	private void loadCharLists(@Nullable Language initialLanguage) {
		if (initialLanguage == null) {
			 return;
		}

		if (activity != null) {
			for (Language lang : LanguageCollection.getAll(activity.getSettings().getEnabledLanguageIds())) {
				activity.getSettings().setDefaultChars(lang, false);
			}
		}
		onLanguageChanged(String.valueOf(initialLanguage.getId()));
	}
}
