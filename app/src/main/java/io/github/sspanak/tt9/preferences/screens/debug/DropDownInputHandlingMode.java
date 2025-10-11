package io.github.sspanak.tt9.preferences.screens.debug;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownInputHandlingMode extends EnhancedDropDownPreference {
	public static final int NORMAL = 0;
	public static final int RETURN_FALSE = 1;
	public static final int CALL_SUPER = 2;

	public static final String NAME = "pref_input_handling_mode";

	public DropDownInputHandlingMode(@NonNull Context context) { super(context); }
	public DropDownInputHandlingMode(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownInputHandlingMode(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownInputHandlingMode(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected void init(@NonNull Context c) {
		super.init(c);
		populate(new SettingsStore(c));
	}

	@Override
	public DropDownInputHandlingMode populate(@NonNull SettingsStore settings) {
		add(NORMAL, "Normal");
		add(RETURN_FALSE, "Return False");
		add(CALL_SUPER, "Call Super");
		commitOptions();
		setValue(String.valueOf(settings.getInputHandlingMode()));
		preview();

		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
