package io.github.sspanak.tt9.preferences.screens.hotkeys;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.UI;


class ItemResetKeys extends ItemClickable {
	public static final String NAME = "reset_keys";

	private final PreferencesActivity activity;
	private final SectionKeymap dropdowns;


	ItemResetKeys(Preference item, PreferencesActivity activity, SectionKeymap dropdowns) {
		super(item);
		this.activity = activity;
		this.dropdowns = dropdowns;
	}

	@Override
	protected boolean onClick(Preference p) {
		Hotkeys.setDefault(activity.getSettings());
		dropdowns.reloadSettings();
		UI.toast(activity, R.string.function_reset_keys_done);
		return true;
	}
}
