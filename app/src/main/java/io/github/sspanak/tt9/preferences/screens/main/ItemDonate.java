package io.github.sspanak.tt9.preferences.screens.main;

import android.content.Intent;
import android.net.Uri;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.util.Logger;

class ItemDonate extends ItemClickable {
	static final String NAME = "donate_link";
	private final PreferencesActivity activity;
	private final boolean isVisible;

	ItemDonate(Preference preference, PreferencesActivity activity, boolean isTT9On) {
		super(preference);
		this.activity = activity;
		this.isVisible = isTT9On && !activity.getSettings().getDemoMode();
	}

	public ItemDonate populate() {
		if (item == null) {
			return this;
		}

		if (isVisible) {
			String appName = activity.getString(R.string.app_name_short);
			String url = activity.getString(R.string.donate_url_short);
			item.setSummary(activity.getString(R.string.donate_summary, appName, url));
		}
		item.setVisible(isVisible);

		return this;
	}

	@Override
	protected boolean onClick(Preference p) {
		try {
			activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.donate_url))));
			return true;
		} catch (Exception e) {
			Logger.w(getClass().getSimpleName(), "Cannot navigate to the donation page. " + e.getMessage() + " (do you have a browser?)");
			return false;
		}
	}
}
