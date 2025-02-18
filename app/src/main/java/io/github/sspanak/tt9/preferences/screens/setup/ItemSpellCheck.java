package io.github.sspanak.tt9.preferences.screens.setup;

import android.content.Context;
import android.content.Intent;
import android.service.textservice.SpellCheckerService;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.TextServicesManager;

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
		} else if (isSpellCheckEnabled(activity) || isTextSpellCheckerEnabled(activity)) {
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


	private boolean isTextSpellCheckerEnabled(PreferencesActivity activity) {
		if (activity == null) {
			return false;
		}

		TextServicesManager tsm = (TextServicesManager) activity.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
		SpellCheckerSession session = tsm.newSpellCheckerSession(null, null, new DummySpellCheckerListener(), true);
		if (session == null) {
			return false;
		}

		session.close();
		return true;
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
