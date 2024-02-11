package io.github.sspanak.tt9.preferences.screens;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.helpers.SystemSettings;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemSelectGlobalKeyboard;
import io.github.sspanak.tt9.preferences.items.ItemSetDefaultGlobalKeyboard;

public class SetupScreen extends BaseScreenFragment {
	public SetupScreen() { init(); }
	public SetupScreen(PreferencesActivity activity) { init(activity); }

	@Override protected int getTitle() { return R.string.pref_category_setup;}
	@Override protected int getXml() { return R.xml.prefs_screen_setup; }

	@Override
	public void onCreate() {
		createKeyboardSection();
	}

	@Override
	public void onResume() {
		super.onResume();
		createKeyboardSection();
	}

	private void createKeyboardSection() {
		boolean isTT9On = SystemSettings.isTT9Enabled(activity);

		Preference statusItem = findPreference("global_tt9_status");
		if (statusItem != null) {
			statusItem.setSummary(
				isTT9On ? R.string.setup_tt9_on : R.string.setup_tt9_off
			);

			new ItemSelectGlobalKeyboard(statusItem, activity).enableClickHandler();
		}

		Preference defaultKeyboardItem = findPreference("global_default_keyboard");
		if (defaultKeyboardItem != null) {
			new ItemSetDefaultGlobalKeyboard(defaultKeyboardItem, activity).enableClickHandler();
		}
	}
}
