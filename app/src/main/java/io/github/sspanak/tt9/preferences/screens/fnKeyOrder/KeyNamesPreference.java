package io.github.sspanak.tt9.preferences.screens.fnKeyOrder;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.PreferencePlainText;

public class KeyNamesPreference extends PreferencePlainText {
	public KeyNamesPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); populate(); }
	public KeyNamesPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); populate(); }
	public KeyNamesPreference(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); populate(); }
	public KeyNamesPreference(@NonNull Context context) { super(context); populate(); }

	private void populate() {
		StringBuilder content = new StringBuilder(__(R.string.fn_key_order_legend))
			.append("\n1 = ").append(__(R.string.function_show_settings)).append("\t5 = ").append(__(R.string.function_backspace))
			.append("\n2 = ").append(__(R.string.function_add_word)).append("\t\t\t\t6 = ").append(__(R.string.function_filter_suggestions)).append(" / ").append(__(R.string.virtual_key_space_korean))
			.append("\n3 = ").append(__(R.string.virtual_key_shift)).append("\t\t\t\t\t\t\t7 = ").append(__(R.string.function_edit_text)).append(" / ").append(__(R.string.function_voice_input))
			.append("\n4 = ").append(__(R.string.function_next_mode)).append("\t\t\t8 = OK\n\n")
			.append(__(R.string.fn_key_order_preview_tip));

		setSummary(content);
	}

	private String __(int resourceId) {
		return getContext().getString(resourceId);
	}
}
