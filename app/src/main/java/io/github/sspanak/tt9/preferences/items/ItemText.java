package io.github.sspanak.tt9.preferences.items;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Clipboard;

public class ItemText extends ItemClickable {
	private final PreferencesActivity activity;
	public ItemText(PreferencesActivity activity, Preference preference) {
		super(preference);

		this.activity = activity;
	}

	@Override
	protected boolean onClick(Preference p) {
		if (activity == null || p.getSummary() == null) {
			return false;
		}

		Clipboard.copy(
			activity,
			activity.getString(R.string.app_name_short) + " / " + item.getTitle(),
			p.getSummary()
		);

		if (!DeviceInfo.AT_LEAST_ANDROID_13) {
			UI.toast(activity, "\"" + Clipboard.getPreview(activity) + "\" copied.");
		}

		return true;
	}

	public ItemText populate(String text) {
		if (item != null) {
			item.setSummary(text);
		}

		return this;
	}
}
