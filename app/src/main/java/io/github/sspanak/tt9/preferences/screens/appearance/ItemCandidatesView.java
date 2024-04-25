package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.items.ItemSwitch;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ItemCandidatesView extends ItemSwitch {
	public static final String NAME = "pref_candidates_view";
	private final SettingsStore settings;

	public ItemCandidatesView(SwitchPreferenceCompat item, SettingsStore settings) {
		super(item);
		this.settings = settings;
	}

	@Override
	protected boolean getDefaultValue() {
		return settings.getCandidatesView();
	}

	@Override
	public ItemSwitch populate() {
		if (item != null) {
			String appName = item.getContext().getString(R.string.app_name_short);
			item.setSummary(item.getContext().getString(R.string.pref_hack_candidate_view_summary, appName));
		}

		return super.populate();
	}
}
