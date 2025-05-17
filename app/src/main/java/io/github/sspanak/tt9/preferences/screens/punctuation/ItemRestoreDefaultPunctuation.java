package io.github.sspanak.tt9.preferences.screens.punctuation;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;

class ItemRestoreDefaultPunctuation extends ItemClickable {
	public static final String NAME = "punctuation_order_reset_defaults";

	private Language language;
	private final ConsumerCompat<String> onClick;
	private final SettingsStore settings;

	ItemRestoreDefaultPunctuation(@NonNull SettingsStore settings, Preference item, ConsumerCompat<String> onClick) {
		super(item);
		this.onClick = onClick;
		this.settings = settings;
	}

	ItemRestoreDefaultPunctuation setLanguage(Language language) {
		this.language = language;
		return this;
	}

	@Override
	protected boolean onClick(Preference p) {
		if (language == null) {
			return false;
		}

		settings.setDefaultCharOrder(language, true);

		if (onClick != null) {
			onClick.accept(String.valueOf(language.getId()));
		}

		return true;
	}
}
