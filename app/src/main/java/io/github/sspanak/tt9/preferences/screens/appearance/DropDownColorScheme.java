package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.colors.AbstractColorScheme;
import io.github.sspanak.tt9.colors.CollectionColorScheme;
import io.github.sspanak.tt9.colors.ColorSchemeSystem;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownColorScheme extends EnhancedDropDownPreference {
	public static final String NAME = "pref_theme";
	public static final String DEFAULT = String.valueOf(ColorSchemeSystem.ID);

	@Nullable protected SettingsStore settings;

	public DropDownColorScheme(@NonNull Context context) { super(context); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }


	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		this.settings = settings;

		if (!values.isEmpty()) {
			values.clear();
		}

		addOptions(getContext());
		commitOptions();
		setValue(validateSchemeId(loadValue(settings)));
		preview();

		return this;
	}


	@Override
	protected String getName() {
		return NAME;
	}


	@Override
	protected int getDisplayTitle() {
		return R.string.pref_color_scheme;
	}


	protected void addOptions(@NonNull Context context) {
		for (AbstractColorScheme scheme : CollectionColorScheme.getAll(context)) {
			add(scheme.getId(), scheme.getDisplayName());
		}
	}


	protected String loadValue(@NonNull SettingsStore settings) {
		return settings.getColorSchemeId();
	}


	@Override
	protected boolean onChange(Preference p, Object newKey) {
		if (settings == null) {
			return false;
		}

		AbstractColorScheme scheme = CollectionColorScheme.get(getContext(), newKey.toString());
		settings.setColorScheme(scheme);

		return true;
	}


	private String validateSchemeId(@Nullable String id) {
		if (id == null) {
			return DEFAULT;
		}

		for (String key : values.keySet()) {
			if (id.equals(key)) {
				return id;
			}
		}
		return DEFAULT;
	}
}
