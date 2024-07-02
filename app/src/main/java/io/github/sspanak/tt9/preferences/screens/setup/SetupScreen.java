package io.github.sspanak.tt9.preferences.screens.setup;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.SystemSettings;

public class SetupScreen extends BaseScreenFragment {
	final public static String NAME = "Setup";
	public SetupScreen() { init(); }
	public SetupScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_setup;}
	@Override protected int getXml() { return R.xml.prefs_screen_setup; }

	@Override
	public void onCreate() {
		boolean isTT9On = SystemSettings.isTT9Enabled(activity);
		createKeyboardSection(isTT9On);
		createHacksSection(isTT9On | activity.getSettings().getDemoMode());
		resetFontSize(false);
	}

	@Override
	public void onResume() {
		super.onResume();
		onCreate();
	}

	private void createKeyboardSection(boolean isTT9On) {
		Preference statusItem = findPreference("global_tt9_status");
		if (statusItem != null) {
			String appName = getString(R.string.app_name);
			statusItem.setSummary(
				getString(isTT9On ? R.string.setup_tt9_on : R.string.setup_tt9_off, appName)
			);

			new ItemSelectGlobalKeyboard(statusItem, activity).enableClickHandler();
		}

		Preference defaultKeyboardItem = findPreference("global_default_keyboard");
		if (defaultKeyboardItem != null) {
			new ItemSetDefaultGlobalKeyboard(defaultKeyboardItem, activity).enableClickHandler();
		}
	}

	private void createHacksSection(boolean isEnabled) {
		Preference hackGoogleChat = findPreference("pref_hack_google_chat");
		if (hackGoogleChat != null) {
			hackGoogleChat.setEnabled(isEnabled);
		}
	}
}
