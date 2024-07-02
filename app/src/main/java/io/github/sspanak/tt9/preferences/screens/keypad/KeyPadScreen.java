package io.github.sspanak.tt9.preferences.screens.keypad;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
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
		(new ItemHapticFeedback(findPreference(ItemHapticFeedback.NAME), activity.getSettings())).populate().enableClickHandler();

		ItemDropDown[] items = {
			new ItemSelectZeroKeyCharacter(findPreference(ItemSelectZeroKeyCharacter.NAME), activity),
			new ItemSelectABCAutoAccceptTime(findPreference(ItemSelectABCAutoAccceptTime.NAME), activity),
			new ItemKeyPadDebounceTime(findPreference(ItemKeyPadDebounceTime.NAME), activity)
		};

		for (ItemDropDown item : items) {
			item.populate().enableClickHandler().preview();
		}

		resetFontSize(false);
	}
}
