package io.github.sspanak.tt9.preferences.items;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;

public class ItemText extends ItemClickable {
	private final PreferencesActivity activity;
	public ItemText(PreferencesActivity activity, Preference preference) {
		super(preference);

		this.activity = activity;
	}

	@Override
	protected boolean onClick(Preference p) {
		if (activity == null) {
			return false;
		}

		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		String label = activity.getString(R.string.app_name_short) + " / " + item.getTitle();
		clipboard.setPrimaryClip(ClipData.newPlainText(label , p.getSummary()));

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
			UI.toast(activity, "Text copied.");
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
