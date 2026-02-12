package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.preferences.settings.SettingsUI;

public class DropDownLayoutType extends EnhancedDropDownPreference {
	public static final String NAME = "pref_layout_type";

	private final ArrayList<ItemLayoutChangeReactive> onChangeReactiveItems = new ArrayList<>();

	@Nullable private SettingsStore settings;

	public DropDownLayoutType(@NonNull Context context) { super(context); }
	public DropDownLayoutType(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownLayoutType(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownLayoutType(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		this.settings = settings;

		add(SettingsUI.LAYOUT_STEALTH, R.string.pref_layout_stealth);
		add(SettingsUI.LAYOUT_TRAY, R.string.pref_layout_tray);
		add(SettingsUI.LAYOUT_SMALL, R.string.pref_layout_small);
		add(SettingsUI.LAYOUT_CLASSIC, R.string.pref_layout_classic);
		add(SettingsUI.LAYOUT_NUMPAD, R.string.pref_layout_numpad);
		commitOptions();
		super.setValue(String.valueOf(settings.getMainViewLayout()));

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	public DropDownLayoutType addOnChangePreference(@Nullable Preference preference) {
		if (preference instanceof ItemLayoutChangeReactive) {
			onChangeReactiveItems.add((ItemLayoutChangeReactive) preference);
		}
		return this;
	}

	public DropDownLayoutType addOnChangeItem(@Nullable ItemLayoutChangeReactive reactiveItem) {
		if (reactiveItem != null) {
			onChangeReactiveItems.add(reactiveItem);
		}
		return this;
	}

	@Override
	protected boolean onChange(Preference preference, Object newKey) {
		int newLayout = Integer.parseInt(newKey.toString());
		for (ItemLayoutChangeReactive item : onChangeReactiveItems) {
			item.onLayoutChange(newLayout);
		}

		if (settings != null && (newLayout == SettingsUI.LAYOUT_CLASSIC || newLayout == SettingsUI.LAYOUT_NUMPAD)) {
			settings.setPreferredLargeLayout(newLayout);
		}

		return true;
	}
}
