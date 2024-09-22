package io.github.sspanak.tt9.preferences.screens.setup;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.service.textservice.SpellCheckerService;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.ui.UI;

public class ItemSpellCheck extends ItemClickable {
	public ItemSpellCheck(PreferencesActivity activity, Preference item) {
		super(item);

		if (DeviceInfo.noTouchScreen(activity)) {
			disable();
			item.setVisible(false);
		} else if (isSpellCheckEnabled(activity)) {
			enable();
		} else {
			disable();
		}
	}

	private boolean isSpellCheckEnabled(PreferencesActivity activity) {
		if (activity == null) {
			return false;
		}

		Intent spellCheckIntent = new Intent(SpellCheckerService.SERVICE_INTERFACE);
		return activity.getPackageManager().resolveService(spellCheckIntent, 0) != null;
	}

	@Override
	public void enable() {
		if (item != null) {
			super.enable();
			enableClickHandler();
			item.setSummary(R.string.setup_spell_checker_on);
		}
	}

	@Override
	public void disable() {
		if (item != null) {
			super.disable();
			item.setSummary(R.string.setup_spell_checker_off);
		}
	}

	@Override
	public void enableClickHandler() {
		if (item != null && item.isEnabled()) {
			super.enableClickHandler();
		}
	}

	@Override
	protected boolean onClick(Preference p) {
		return UI.showSystemSpellCheckerSettings(p.getContext());
	}
}
