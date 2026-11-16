package io.github.sspanak.tt9.preferences.screens.backup;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.customWords.SettingsExporter;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceExportSettings extends ScreenPreference {
	public static final String NAME = "export_settings";

	private PreferencesActivity activity;
	private SettingsExporter exporter;

	public PreferenceExportSettings(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(context); }
	public PreferenceExportSettings(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }
	public PreferenceExportSettings(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public PreferenceExportSettings(@NonNull Context context) { super(context); init(context); }


	private void init(@NonNull Context context) {
		exporter = new SettingsExporter();
		setKey(NAME);
		setTitle(R.string.backup_export);
		setSummary(context.getString(R.string.backup_export_summary, exporter.getOutputDir()));
	}


	@Override
	public int getLargeLayout() {
		return R.layout.pref_default_large;
	}


	@Override
	protected void onClick() {
		if (activity == null) {
			Logger.d("LOG", "=>>>> pros si" );
			return;
		}

		super.onClick();

		String json = activity.getSettings().exportAll();
		Logger.d("LOG", json );
	}


	public PreferenceExportSettings populate(@NonNull PreferencesActivity activity) {
		this.activity = activity;
		setEnabled(!activity.getSettings().getDemoMode());
		return this;
	}
}
