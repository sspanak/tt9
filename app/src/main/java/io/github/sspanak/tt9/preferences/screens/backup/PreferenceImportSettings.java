package io.github.sspanak.tt9.preferences.screens.backup;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.util.Logger;

public class PreferenceImportSettings extends ScreenPreference {
	public static final String NAME = "import_settings";

	private PreferencesActivity activity;

	public PreferenceImportSettings(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(context); }
	public PreferenceImportSettings(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }
	public PreferenceImportSettings(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public PreferenceImportSettings(@NonNull Context context) { super(context); init(context); }


	private void init(@NonNull Context context) {
		setKey(NAME);
		setTitle(R.string.backup_import);
		setSummary(context.getString(R.string.backup_import_summary));
	}


	@Override
	public int getLargeLayout() {
		return R.layout.pref_default_large;
	}


	@Override
	protected void onClick() {
		if (activity == null) {
			Logger.d("LOG", "=>>>> pros si i ti" );
			return;
		}

		super.onClick();
		Logger.d("LOG", "=====> launch file picker");
	}


	public PreferenceImportSettings populate(@NonNull PreferencesActivity activity) {
		this.activity = activity;
		setEnabled(!activity.getSettings().getDemoMode());
		return this;
	}
}
