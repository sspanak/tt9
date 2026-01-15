package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdEditText implements Command {
	public static final String ID = "key_edit_text";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_txt_cut; }
	@Override public int getName() { return io.github.sspanak.tt9.R.string.function_edit_text; }
	@Override public int getHardKey() { return 5; }
	@Override public int getPaletteKey() { return R.id.soft_key_5; }

	@Override
	public boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		} else if (tt9.isTextEditingActive()) {
			return tt9.hideTextEditingPalette();
		} else {
			tt9.showTextEditingPalette();
			return true;
		}
	}

	public boolean isActive(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.isTextEditingActive();
	}

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return !isMissing(tt9);
	}

	public boolean isMissing(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.isInputLimited();
	}
}
