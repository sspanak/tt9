package io.github.sspanak.tt9.preferences.custom;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;

public class ScreenPreferencesList {
	@NonNull private final PreferenceScreen axScreen;
	@NonNull private final ArrayList<Preference> preferences;


	public ScreenPreferencesList(@NonNull PreferenceScreen screen) {
		this.axScreen = screen;
		preferences = new ArrayList<>();
	}


	public int size() {
		int count = 0;

		for (int i = axScreen.getPreferenceCount(); i > 0; i--) {
			Preference pref = axScreen.getPreference(i - 1);
			if (pref.isVisible()) {
				count += pref instanceof PreferenceCategory ? ((PreferenceCategory) pref).getPreferenceCount() : 1;
			}
		}

		return count;
	}


	public void setFontSize(int fontSize) {
		for (Preference pref : preferences) {
			ScreenPreference.setFontSize(pref, fontSize);
		}
	}


	public void getAll(boolean noCache, boolean includeCategories) {
		if (noCache) {
			preferences.clear();
		}

		if (preferences.isEmpty()) {
			addFromScreen(includeCategories);
		}
	}


	private void addFromScreen(boolean includeCategories) {
		for (int i = axScreen.getPreferenceCount(); i > 0; i--) {
			add(axScreen.getPreference(i - 1), includeCategories);
		}
	}

	private void add(@NonNull Preference pref, boolean includeCategories) {
		if (pref instanceof PreferenceCategory) {
			addCategory((PreferenceCategory) pref);
		}

		if (includeCategories || !(pref instanceof PreferenceCategory)) {
			preferences.add(pref);
		}
	}


	private void addCategory(@NonNull PreferenceCategory category) {
		for (int i = category.getPreferenceCount(); i > 0; i--) {
			add(category.getPreference(i - 1), false);
		}
	}
}
