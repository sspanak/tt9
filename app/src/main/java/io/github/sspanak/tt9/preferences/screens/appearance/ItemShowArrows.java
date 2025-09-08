package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.preferences.items.ItemSwitch;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemShowArrows extends ItemSwitch implements ItemLayoutChangeReactive {
	public final static String NAME = "pref_arrow_keys_visible";
	protected final SettingsStore settings;

	public ItemShowArrows(SwitchPreferenceCompat item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	public ItemSwitch populate() {
		onLayoutChange(settings.getMainViewLayout());
		return super.populate();
	}

	@Override
	protected boolean getDefaultValue() {
		return !settings.areArrowKeysHidden();
	}


	public void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setVisible(mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
			item.setIconSpaceReserved(false);
		}
	}
}
