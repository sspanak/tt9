package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdFilterSuggestions implements Command {
	public static final String ID = "key_filter_suggestions";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_filter; }
	public int getIconExact() { return R.drawable.ic_fn_filter_exact; }
	public int getIconFuzzy() { return R.drawable.ic_fn_filter_fuzzy; }
	public int getName() { return R.string.function_filter_suggestions; }

	public boolean run(@Nullable TraditionalT9 tt9, boolean repeat) {
		return tt9 != null && tt9.onKeyFilterSuggestions(false, repeat);
	}
}
