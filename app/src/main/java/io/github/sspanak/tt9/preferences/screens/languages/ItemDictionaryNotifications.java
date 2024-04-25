package io.github.sspanak.tt9.preferences.screens.languages;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemSwitch;
import io.github.sspanak.tt9.util.Permissions;

public class ItemDictionaryNotifications extends ItemSwitch {
	public static final String NAME = "dictionary_notifications";
	private final SwitchPreferenceCompat item;
	private final Permissions permissions;

	public ItemDictionaryNotifications(SwitchPreferenceCompat preference, PreferencesActivity activity) {
		super(preference);
		this.item = preference;
		this.permissions = new Permissions(activity);
	}

	@Override
	public ItemDictionaryNotifications populate() {
		super.populate();
		if (item != null) {
			item.setVisible(!item.isChecked());
		}

		return this;
	}

	@Override
	protected boolean getDefaultValue() {
		return !permissions.noPostNotifications();
	}

	protected boolean onClick(Preference p, Object value) {
		if (value == Boolean.TRUE || permissions.noPostNotifications()) {
			permissions.requestPostNotifications();
		}

		// Switch off the component on user refusal. Android will not allow permission request again.
		item.setEnabled(false);
		return !permissions.noPostNotifications();
	}
}
