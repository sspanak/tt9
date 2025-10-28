package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.colors.AbstractColorScheme;
import io.github.sspanak.tt9.colors.CollectionColorScheme;
import io.github.sspanak.tt9.colors.ColorSchemeSystem;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownColorScheme extends EnhancedDropDownPreference {
	public static final String NAME = "pref_theme";
	public static final String DEFAULT = String.valueOf(ColorSchemeSystem.ID);

	@Nullable private SettingsStore settings;
	@Nullable private Runnable onChangeListener = null;

	public DropDownColorScheme(@NonNull Context context) { super(context); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }


	@Override
	protected void init(@NonNull Context context) {
		super.init(context);
		populate(context, new SettingsStore(context));
	}


	@Override
	protected String getName() {
		return NAME;
	}


	@Override
	protected int getDisplayTitle() {
		return R.string.pref_color_scheme;
	}


	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		return this;
	}


	public void populate(@NonNull Context context, @NonNull SettingsStore settings) {
		this.settings = settings;

		addSortedOptions(CollectionColorScheme.getAll(context));
		commitOptions();
		setValue(validateSchemeId(settings.getColorSchemeId()));
		preview();
	}


	public void setOnChangeListener(@Nullable Runnable listener) {
		onChangeListener = listener;
	}


	protected boolean onChange(Preference p, Object newKey) {
		super.onChange(p, newKey);

		if (settings == null) {
			return false;
		}

		AbstractColorScheme scheme = CollectionColorScheme.get(getContext(), newKey.toString());
		settings.setColorScheme(scheme);

		if (onChangeListener != null) {
			onChangeListener.run();
		}

		return true;
	}


	private void addSortedOptions(@NonNull AbstractColorScheme[] schemes) {
		for (AbstractColorScheme scheme : schemes) {
			if (!scheme.isSystem()) {
				add(scheme.getId(), scheme.getName());
			}
		}

		sort();
		LinkedHashMap<String, String> nonSystemSchemes = new LinkedHashMap<>(values);
		values.clear();

		for (AbstractColorScheme scheme : schemes) {
			if (scheme.isSystem()) {
				add(scheme.getId(), scheme.getName());
			}
		}

		values.putAll(nonSystemSchemes);
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
