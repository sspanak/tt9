package io.github.sspanak.tt9.preferences;

import android.content.Context;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.UI;


public class ItemResetKeys extends ItemClickable {
	public static final String NAME = "reset_keys";

	private final Context context;
	private final SectionKeymap dropdowns;
	private final SettingsStore settings;


	ItemResetKeys(Preference item, Context context, SectionKeymap dropdowns, SettingsStore settings) {
		super(item);
		this.context = context;
		this.dropdowns = dropdowns;
		this.settings = settings;
	}

	@Override
	protected boolean onClick(Preference p) {
		settings.setDefaultKeys();
		dropdowns.reloadSettings();
		UI.toast(context, R.string.function_reset_keys_done);
		return false;
	}
}
