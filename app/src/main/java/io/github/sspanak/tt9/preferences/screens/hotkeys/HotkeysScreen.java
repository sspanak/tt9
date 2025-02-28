package io.github.sspanak.tt9.preferences.screens.hotkeys;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class HotkeysScreen extends BaseScreenFragment {
	public static final String NAME = "Hotkeys";
	@NonNull static HashMap<String, PreferenceHotkey> hotkeys = new HashMap<>();


	public HotkeysScreen() { init(); }
	public HotkeysScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_function_keys; }
	@Override protected int getXml() { return R.xml.prefs_screen_hotkeys; }

	@Override
	public void onCreate() {
		getHotkeys();
		(new ItemResetKeys(findPreference(ItemResetKeys.NAME), activity, hotkeys.values())).enableClickHandler();
		resetFontSize(false);
	}

	@Override
	public int getPreferenceCount() {
		return -1; // prevent scrolling and item selection using the number keys on this screen
	}

	private void getHotkeys() {
		for (String function : SettingsStore.FUNCTIONS) {
			PreferenceHotkey hotkey = findPreference(function);
			if (hotkey != null) {
				hotkeys.put(function, hotkey);
			}
		}
	}
}
