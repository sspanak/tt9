package io.github.sspanak.tt9.preferences.screens.punctuation;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;

class DropDownPunctuationOrderLanguage extends EnhancedDropDownPreference {
	public static final String NAME = "punctuation_order_language";

	private ConsumerCompat<String> onChangeCallback;

	public DropDownPunctuationOrderLanguage(@NonNull Context context) { super(context); }
	public DropDownPunctuationOrderLanguage(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownPunctuationOrderLanguage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownPunctuationOrderLanguage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }


	DropDownPunctuationOrderLanguage setOnChangeHandler(ConsumerCompat<String> callback) {
		onChangeCallback = callback;
		return this;
	}


	@Override
	protected boolean onChange(Preference preference, Object newKey) {
		if (!super.onChange(preference, newKey)) {
			return false;
		}

		if (onChangeCallback != null) {
			onChangeCallback.accept(newKey.toString());
		}
		return true;
	}


	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		ArrayList<Language> languages = LanguageCollection.getAll(settings.getEnabledLanguageIds(), true);
		if (languages.isEmpty()) {
			return this;
		}

		for (Language lang : LanguageCollection.getAll(settings.getEnabledLanguageIds())) {
			add(String.valueOf(lang.getId()), lang.getName());
		}
		sort();
		commitOptions();
		setDefaultValue(String.valueOf(languages.get(0).getId()));

		return this;
	}


	@Override
	protected String getName() {
		return NAME;
	}
}
