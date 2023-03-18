package io.github.sspanak.tt9.preferences.screens;

import androidx.preference.Preference;

import io.github.sspanak.tt9.BuildConfig;
import io.github.sspanak.tt9.R;
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
		createAboutSection();
	}

	@Override
	public void onResume() {
		super.onResume();
		createKeyboardSection();
	}

	private void createKeyboardSection() {
		boolean isTT9On = activity.globalKeyboardSettings.isTT9Enabled();

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

		Preference goToMain = findPreference("goto_main_screen");
		if (goToMain != null) {
			goToMain.setEnabled(isTT9On);
		}
	}


	private void createAboutSection() {
		Preference vi = findPreference("version_info");
		if (vi != null) {
			vi.setSummary(BuildConfig.VERSION_FULL);
		}
	}
}
