package io.github.sspanak.tt9.preferences.screens.punctuation;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

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
	private PreferenceChars0 charList0;
	private PreferenceChars1 charList1;

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
		charList0 = findPreference(PreferenceChars0.NAME);
		charList1 = findPreference(PreferenceChars1.NAME);

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
		if (charList0 == null || !charList0.validateCurrentChars() || charList1 == null || !charList1.validateCurrentChars()) {
			UI.toastShortSingle(activity, R.string.punctuation_order_save_error);
		} else {
			charList0.saveCurrentChars();
			charList1.saveCurrentChars();
		}
	}


	private void onLanguageChanged(@Nullable String newLanguageId) {
		Language language = LanguageCollection.getLanguage(newLanguageId);

		restoreDefaults.setLanguage(language);

		if (charList0 != null) {
			charList0.onLanguageChange(language);
		}

		if (charList1 != null) {
			charList1.onLanguageChange(language);
		}
	}


	private void loadCharLists() {
		loadCharList(findPreference(PreferenceChars0.NAME));
		loadCharList(findPreference(PreferenceChars1.NAME));
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
