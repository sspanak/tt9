package io.github.sspanak.tt9.preferences.screens;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemSelectZeroKeyCharacter;

public class KeyPadScreen extends BaseScreenFragment {
	final public static String NAME = "KeyPad";
	public KeyPadScreen() { init(); }
	public KeyPadScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_keypad; }
	@Override protected int getXml() { return R.xml.prefs_screen_keypad; }

	@Override
	protected void onCreate() {
		(new ItemSelectZeroKeyCharacter(findPreference(ItemSelectZeroKeyCharacter.NAME), activity)).populate().activate();
	}
}
