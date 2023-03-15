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

	@Override protected int getTitle() { return R.string.app_settings;}
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
		Preference status = findPreference("global_tt9_status");
		if (status != null) {
			status.setSummary(
				activity.globalKeyboardSettings.isTT9Enabled() ? R.string.setup_tt9_on : R.string.setup_tt9_off
			);

			new ItemSelectGlobalKeyboard(status, activity).enableClickHandler();
		}

		Preference defaultKeyboard = findPreference("global_default_keyboard");
		if (defaultKeyboard != null) {
			String defaultKeyboardName = activity.globalKeyboardSettings.getDefault();
			defaultKeyboard.setSummary(!defaultKeyboardName.equals("") ? defaultKeyboardName : "--");

			new ItemSetDefaultGlobalKeyboard(defaultKeyboard, activity).enableClickHandler();
		}
	}


	private void createAboutSection() {
		Preference vi = findPreference("version_info");
		if (vi != null) {
			vi.setSummary(BuildConfig.VERSION_FULL);
		}
	}
}
