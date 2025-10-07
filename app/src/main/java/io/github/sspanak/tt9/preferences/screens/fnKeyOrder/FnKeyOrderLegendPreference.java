package io.github.sspanak.tt9.preferences.screens.fnKeyOrder;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdAddWord;
import io.github.sspanak.tt9.commands.CmdBackspace;
import io.github.sspanak.tt9.commands.CmdEditText;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.commands.CmdNextInputMode;
import io.github.sspanak.tt9.commands.CmdShift;
import io.github.sspanak.tt9.commands.CmdShowSettings;
import io.github.sspanak.tt9.commands.CmdVoiceInput;
import io.github.sspanak.tt9.preferences.custom.PreferencePlainText;

public class FnKeyOrderLegendPreference extends PreferencePlainText {
	public FnKeyOrderLegendPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); populate(); }
	public FnKeyOrderLegendPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); populate(); }
	public FnKeyOrderLegendPreference(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); populate(); }
	public FnKeyOrderLegendPreference(@NonNull Context context) { super(context); populate(); }

	private void populate() {
		StringBuilder content = new StringBuilder(__(R.string.fn_key_order_legend))
			.append("\n1 = ").append(__(new CmdShowSettings().getName()))
			.append("\n2 = ").append(__(new CmdAddWord().getName()))
			.append("\n3 = ").append(__(new CmdShift().getName()))
			.append("\n4 = ").append(__(new CmdNextInputMode().getName()))
			.append("\n5 = ").append(__(new CmdBackspace().getName()))
			.append("\n6 = ").append(__(new CmdFilterSuggestions().getName()))
			.append("\n7 = ").append(__(new CmdEditText().getName())).append(" / ").append(__(new CmdVoiceInput().getName()))
			.append("\n8 = OK\n\n")
			.append(__(R.string.fn_key_order_preview_tip));

		setSummary(content);
	}

	private String __(int resourceId) {
		return getContext().getString(resourceId);
	}
}
