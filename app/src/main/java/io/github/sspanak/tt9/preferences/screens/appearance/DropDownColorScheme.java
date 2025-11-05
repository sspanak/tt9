package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.colors.AbstractColorScheme;
import io.github.sspanak.tt9.colors.CollectionColorScheme;
import io.github.sspanak.tt9.colors.ColorSchemeSystem;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class DropDownColorScheme extends EnhancedDropDownPreference {
	public static final String NAME = "pref_theme";
	public static final String DEFAULT = String.valueOf(ColorSchemeSystem.ID);

	@Nullable protected SettingsStore settings;

	public DropDownColorScheme(@NonNull Context context) { super(context); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownColorScheme(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }


	@Override
	protected void init(@NonNull Context context) {
		super.init(context);

		try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
			executor.submit(() -> populate(context, new SettingsStore(context)));
		} catch (Exception e) {
			Logger.e(DropDownColorScheme.NAME, "Failed to populate DropDownColorScheme preference. " + e.getMessage());
		}
	}


	public void populate(@NonNull Context context, @NonNull SettingsStore settings) {
		this.settings = settings;

		addOptions(context); // @todo: see why this happens twice
		commitOptions();
		setValue(validateSchemeId(loadValue(settings)));
		preview();
	}


	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
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
