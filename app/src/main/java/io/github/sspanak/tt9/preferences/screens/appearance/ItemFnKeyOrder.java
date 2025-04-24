package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.Preference;

import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemFnKeyOrder extends ItemClickable implements ItemLayoutChangeReactive {
	public static final String NAME = "screen_fn_key_order";
	public ItemFnKeyOrder(SettingsStore settings, Preference item) {
		super(item);
		onLayoutChange(settings.getMainViewLayout());
	}

	@Override protected boolean onClick(Preference p) { return true; }

	public void onLayoutChange(int mainViewLayout) {
		if (item != null) {
			item.setVisible(mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
			item.setIconSpaceReserved(false);
		}
	}
}
