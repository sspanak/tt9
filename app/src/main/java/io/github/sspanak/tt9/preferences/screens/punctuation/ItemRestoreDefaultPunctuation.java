package io.github.sspanak.tt9.preferences.screens.punctuation;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.items.ItemClickable;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

class ItemRestoreDefaultPunctuation extends ItemClickable {
	public static final String NAME = "punctuation_order_reset_defaults";

	private final Language language;
	private final ConsumerCompat<String> onClick;
	private final SettingsStore settings;

	ItemRestoreDefaultPunctuation(@NonNull SettingsStore settings, Preference item, Language language, ConsumerCompat<String> onClick) {
		super(item);
		this.language = language;
		this.onClick = onClick;
		this.settings = settings;
	}

	@Override
	protected boolean onClick(Preference p) {
		if (language == null) {
			return false;
		}

		settings.saveSpecialChars(
			language,
			String.join("", language.getKeyCharacters(0))
		);

		settings.savePunctuation(
			language,
			String.join("", language.getKeyCharacters(1))
		);

		if (onClick != null) {
			onClick.accept(String.valueOf(language.getId()));
		}

		return true;
	}
}
