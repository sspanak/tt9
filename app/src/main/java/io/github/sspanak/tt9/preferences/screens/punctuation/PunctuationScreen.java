package io.github.sspanak.tt9.preferences.screens.punctuation;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class PunctuationScreen extends BaseScreenFragment {
	public static final String NAME = "Punctuation";

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
		resetFontSize(false);

		ItemPunctuationOrderLanguage languageList = (new ItemPunctuationOrderLanguage(activity.getSettings(), findPreference(ItemPunctuationOrderLanguage.NAME)));
		languageList
			.onChange(this::onLanguageChanged)
			.enableClickHandler()
			.populate()
			.preview();

		onLanguageChanged(languageList.getValue());
	}


	private void onLanguageChanged(@Nullable String newLanguageId) {
		PreferenceSpecialCharList key0 = findPreference(PreferenceSpecialCharList.NAME);
		if (key0 != null) {
			key0.onLanguageChanged(newLanguageId);
		}

		PreferenceSentencePunctuationList key1 = findPreference(PreferenceSentencePunctuationList.NAME);
		if (key1 != null) {
			key1.onLanguageChanged(newLanguageId);
		}
	}
}
