package io.github.sspanak.tt9.preferences.screens.chinese;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.chinese.R;
import io.github.sspanak.tt9.chinese.ChineseCandidateSettings;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.extensions.ChinesePackInstaller;

public class ChineseSettingsScreen extends BaseScreenFragment {
	public static final String NAME = "ChineseSettings";

	public ChineseSettingsScreen() { super(); }
	public ChineseSettingsScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return io.github.sspanak.tt9.R.string.pref_chinese_settings; }
	@Override protected int getXml() { return R.xml.prefs_screen_chinese_settings; }

	@Override public void onResume() {
		super.onResume();
		Preference install = findPreference("install_chinese_pack");
		if (install != null) install.setVisible(activity != null && ChinesePackInstaller.needsInstall(activity));
		getPreferenceScreen().setEnabled(false);
		if (activity != null) {
			ChineseSettingsProfile.checkInstalled(activity, installed -> {
				if (!isAdded() || activity == null) return;
				if (installed) {
					getPreferenceScreen().setEnabled(true);
				} else {
					activity.getOnBackPressedDispatcher().onBackPressed();
				}
			});
		}
	}

	@Override protected void onCreate() {
		resetFontSize(true);
		Preference download = findPreference("download_chinese_pack");
		if (download != null) download.setOnPreferenceClickListener(preference -> {
			if (activity != null) ChinesePackInstaller.downloadPack(activity);
			return true;
		});
		Preference importPack = findPreference("import_chinese_pack");
		if (importPack != null) importPack.setOnPreferenceClickListener(preference -> {
			if (activity != null) ChinesePackInstaller.importPack(activity);
			return true;
		});
		Preference install = findPreference("install_chinese_pack");
		if (install != null) {
			install.setVisible(activity != null && ChinesePackInstaller.needsInstall(activity));
			install.setOnPreferenceClickListener(preference -> {
				if (activity != null) ChinesePackInstaller.launch(activity);
				return true;
			});
		}
		if (activity != null) {
			ListPreference scrollSpeed = findPreference(ChineseCandidateSettings.EXPANDED_SCROLL_SPEED);
			if (scrollSpeed != null) {
				scrollSpeed.setValue(ChineseCandidateSettings.getExpandedScrollSpeed(activity.getSettings()));
				scrollSpeed.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
				scrollSpeed.setOnPreferenceChangeListener((preference, value) -> {
					activity.getSettings().getPrefsEditor().putString(ChineseCandidateSettings.EXPANDED_SCROLL_SPEED, value.toString()).apply();
					return true;
				});
			}
			bindSwitch("pref_chinese_number_select", ChineseCandidateSettings.getNumberSelect(activity.getSettings()));
			bindSwitch("pref_chinese_prefix_completion", activity.getSettings().getChinesePrefixCompletion());
			bindSwitch("pref_chinese_commit_on_language_switch", activity.getSettings().getChineseCommitOnLanguageSwitch());
			bindSwitch("pref_chinese_common_only", activity.getSettings().getChineseCommonOnly());
			String[] fuzzy = {"nl", "z_zh", "c_ch", "s_sh", "en_eng", "in_ing"};
			int options = activity.getSettings().getChineseFuzzyOptions();
			for (int i = 0; i < fuzzy.length; i++) bindSwitch("pref_chinese_fuzzy_" + fuzzy[i], (options & (1 << i)) != 0);
		}
	}

	private void bindSwitch(String key, boolean checked) {
		SwitchPreferenceCompat preference = findPreference(key);
		if (preference == null || activity == null) return;
		preference.setChecked(checked);
		preference.setOnPreferenceChangeListener((item, value) -> {
			activity.getSettings().getPrefsEditor().putBoolean(key, (boolean) value).apply();
			return true;
		});
	}
}
