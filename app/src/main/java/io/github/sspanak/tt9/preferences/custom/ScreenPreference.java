package io.github.sspanak.tt9.preferences.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

abstract public class ScreenPreference extends Preference {
	private int SMALL_LAYOUT = 0;

	public ScreenPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public ScreenPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ScreenPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ScreenPreference(@NonNull Context context) {
		super(context);
	}

	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);

		boolean largeFont = new SettingsStore(getContext()).getSettingsFontSize() == SettingsStore.FONT_SIZE_LARGE;
		int layout = largeFont ? getLargeLayout() : getDefaultLayout();
		setLayoutResource(layout);
	}

	public static int getLargeLayout(Preference pref) {
		if (pref instanceof PreferenceCategory) {
			return R.layout.pref_category;
		} else if (pref instanceof SwitchPreferenceCompat) {
			return R.layout.pref_switch;
		} else if (pref instanceof DropDownPreference) {
			return R.layout.pref_dropdown;
		} else {
			return R.layout.pref_text;
		}
	}

	public static int getDefaultLayout(Preference pref) {
		if (pref instanceof PreferenceCategory) {
			return new PreferenceCategory(pref.getContext()).getLayoutResource();
		} else if (pref instanceof SwitchPreferenceCompat) {
			return new SwitchPreferenceCompat(pref.getContext()).getLayoutResource();
		} else if (pref instanceof DropDownPreference) {
			return new DropDownPreference(pref.getContext()).getLayoutResource();
		} else {
			return new Preference(pref.getContext()).getLayoutResource();
		}
	}


	public static void setFontSize(Preference pref, int fontSize) {
		int layout;
		if (fontSize == SettingsStore.FONT_SIZE_LARGE) {
			layout = getLargeLayout(pref);
		} else if (fontSize == SettingsStore.FONT_SIZE_DEFAULT) {
			layout = getDefaultLayout(pref);
		} else {
			Logger.w(ScreenPreference.class.getSimpleName(), "Unknown font size: " + fontSize);
			return;
		}

		pref.setIconSpaceReserved(false);
		pref.setLayoutResource(layout);
		pref.setSingleLineTitle(pref instanceof PreferenceCategory);
	}


	protected int getDefaultLayout() {
		if (SMALL_LAYOUT == 0) {
			SMALL_LAYOUT = new Preference(getContext()).getLayoutResource();
		}

		return SMALL_LAYOUT;
	}

	abstract protected int getLargeLayout();
}
