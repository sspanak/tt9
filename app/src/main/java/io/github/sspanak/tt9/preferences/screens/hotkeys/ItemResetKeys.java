package io.github.sspanak.tt9.preferences.screens.hotkeys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.UI;


class ItemResetKeys extends ItemClickable {
	public static final String NAME = "reset_keys";

	private final PreferencesActivity activity;
	private final Iterable<PreferenceHotkey> hotkeys;


	ItemResetKeys(@Nullable Preference item, @NonNull PreferencesActivity activity, @NonNull Iterable<PreferenceHotkey> hotkeys) {
		super(item);
		this.activity = activity;
		this.hotkeys = hotkeys;
	}

	@Override
	protected boolean onClick(Preference p) {
		activity.getSettings().setDefaultKeys();
		for (PreferenceHotkey hotkey : hotkeys) {
			hotkey.populate();
		}
		UI.toast(activity, R.string.function_reset_keys_done);
		return true;
	}
}
