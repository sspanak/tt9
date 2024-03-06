package io.github.sspanak.tt9.preferences.screens.hotkeys;

import android.content.Context;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.UI;


class ItemResetKeys extends ItemClickable {
	public static final String NAME = "reset_keys";

	private final Context context;
	private final SectionKeymap dropdowns;
	private final SettingsStore settings;


	ItemResetKeys(Preference item, Context context, SettingsStore settings, SectionKeymap dropdowns) {
		super(item);
		this.context = context;
		this.dropdowns = dropdowns;
		this.settings = settings;
	}

	@Override
	protected boolean onClick(Preference p) {
		Hotkeys.setDefault(settings);
		dropdowns.reloadSettings();
		UI.toast(context, R.string.function_reset_keys_done);
		return true;
	}
}
