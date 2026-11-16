package io.github.sspanak.tt9.preferences.screens.backup;


import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class BackupScreen extends BaseScreenFragment {
	final public static String NAME = "Backup";

	public BackupScreen() { super(); }
	public BackupScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_backup;}
	@Override protected int getXml() { return R.xml.prefs_screen_backup; }

	@Override
	public void onCreate() {
		initButtons();
		resetFontSize(false);
	}

	@Override
	public void onResume() {
		super.onResume();
		onCreate();
	}

	private void initButtons() {
		if (activity == null) {
			return;
		}


		PreferenceExportSettings export = findPreference(PreferenceExportSettings.NAME);
		if (export != null) {
			export.populate(activity);
		}

		PreferenceImportSettings importPref = findPreference(PreferenceImportSettings.NAME);
		if (importPref != null) {
			importPref.populate(activity);
		}
	}
}
