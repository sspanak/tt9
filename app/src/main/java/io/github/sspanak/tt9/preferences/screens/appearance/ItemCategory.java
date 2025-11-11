package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public record ItemCategory(@Nullable PreferenceCategory item) implements ItemLayoutChangeReactive {
	@Override
	public void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setVisible(mainViewLayout != SettingsStore.LAYOUT_STEALTH);
		}
	}
}
