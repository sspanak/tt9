package io.github.sspanak.tt9.preferences.screens.chinese;

import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.chinese.R;
import io.github.sspanak.tt9.chinese.ChineseCandidateSettings;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

/** Chinese presentation options; dictionary and conversion settings live under Languages. */
public class ChineseAppearanceScreen extends BaseScreenFragment {
	public static final String NAME = "ChineseAppearance";

	public ChineseAppearanceScreen() { super(); }
	public ChineseAppearanceScreen(@Nullable PreferencesActivity activity) { super(activity); }
	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return io.github.sspanak.tt9.R.string.pref_chinese_appearance; }
	@Override protected int getXml() { return R.xml.prefs_screen_chinese_appearance; }

	@Override public void onResume() {
		super.onResume();
		getPreferenceScreen().setEnabled(false);
		if (activity != null) ChineseSettingsProfile.checkAppearanceAvailable(activity, available -> {
			if (!isAdded() || activity == null) return;
			if (available) getPreferenceScreen().setEnabled(true);
			else activity.getOnBackPressedDispatcher().onBackPressed();
		});
	}

	@Override protected void onCreate() {
		resetFontSize(true);
		if (activity == null) return;
		SwitchPreferenceCompat roomy = findPreference(ChineseCandidateSettings.CANDIDATE_ROOMY);
		if (roomy != null) {
			roomy.setChecked(ChineseCandidateSettings.getCandidateRoomy(activity.getSettings()));
			roomy.setOnPreferenceChangeListener((preference, value) -> {
				activity.getSettings().getPrefsEditor().putBoolean(ChineseCandidateSettings.CANDIDATE_ROOMY, (boolean) value).apply();
				return true;
			});
		}
	}
}
