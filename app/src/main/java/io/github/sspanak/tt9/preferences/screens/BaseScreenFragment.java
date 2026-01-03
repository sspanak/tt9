package io.github.sspanak.tt9.preferences.screens;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.ScreenPreferencesList;
import io.github.sspanak.tt9.preferences.settings.SettingsUI;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

abstract public class BaseScreenFragment extends PreferenceFragmentCompat {
	@Nullable protected PreferencesActivity activity;
	private ScreenPreferencesList preferencesList;


	public BaseScreenFragment() { init(null); }
	public BaseScreenFragment(@Nullable PreferencesActivity activity) { init(activity); }


	protected void init(@Nullable PreferencesActivity activity) {
		this.activity = activity == null ? (PreferencesActivity) getActivity() : activity;
		setScreenTitle();
	}


	public void restart(@NonNull PreferencesActivity activity) {
		init(activity);
		onCreate();
	}


	private void setScreenTitle() {
		if (activity != null) {
			activity.setScreenTitle(getTitle());
		}
	}


	private void initPreferencesList() {
		if (preferencesList == null) {
			preferencesList = new ScreenPreferencesList(getPreferenceScreen());
		}
	}


	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		LanguageCollection.init(getContext());
		setPreferencesFromResource(getXml(), rootKey);

		if (activity == null) {
			activity = (PreferencesActivity) getActivity();
		}

		if (activity == null) {
			Logger.w(
				"MainSettingsScreen",
				"Starting up without an Activity. Preference Items will not be fully initialized."
			);
			return;
		}

		onCreate();
	}


	@Override
	public void onResume() {
		super.onResume();
		setScreenTitle();
		setActivityOptionCounter();
	}


	public int getPreferenceCount() {
		initPreferencesList();
		return preferencesList.size();
	}


	private void setActivityOptionCounter() {
		if (activity != null) {
			activity.setOptionsCount(this::getPreferenceCount);
		}
	}


	public void resetFontSize(boolean reloadList) {
		if (activity == null) {
			return;
		}

		initPreferencesList();
		preferencesList.getAll(reloadList, true);
		if (DeviceInfo.AT_LEAST_ANDROID_12) {
			preferencesList.setFontSize(SettingsUI.FONT_SIZE_DEFAULT);
		} else {
			preferencesList.setFontSize(activity.getSettings().getSettingsFontSize());
		}
	}


	abstract public String getName();
	abstract protected int getTitle();
	abstract protected int getXml();
	abstract protected void onCreate();
}
