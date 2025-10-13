package io.github.sspanak.tt9.preferences.screens.keypad;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class KeyPadScreen extends BaseScreenFragment {
	final public static String NAME = "KeyPad";
	public KeyPadScreen() { init(); }
	public KeyPadScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_keypad; }
	@Override protected int getXml() { return R.xml.prefs_screen_keypad; }

	@Override
	protected void onCreate() {
		createPhysicalKeysSection();
		createVirtualKeysSection();
		resetFontSize(false);
	}

	private void createPhysicalKeysSection() {
		Preference debounceTime = findPreference(DropDownKeyPadDebounceTime.NAME);
		if (debounceTime instanceof DropDownKeyPadDebounceTime) {
			((DropDownKeyPadDebounceTime) debounceTime).populate(activity.getSettings()).preview();
		}
	}

	protected void createVirtualKeysSection() {
		(new ItemHapticFeedback(findPreference(ItemHapticFeedback.NAME), activity.getSettings())).populate().enableClickHandler();

		// hide the entire category when the settings shows no interest in it
		final boolean isVisible = activity.getSettings().isMainLayoutNumpad() || activity.getSettings().isMainLayoutSmall();
		final PreferenceCategory category = findPreference("category_virtual_keys");
		if (category != null) {
			category.setVisible(isVisible);
		}
	}
}
