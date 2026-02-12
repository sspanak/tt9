package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageKind;

public class CmdTxtSelectPreviousChar implements Command {
	public static final String ID = "key_txt_select_previous_char";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_dpad_left; }
	@Override public int getName() { return 0; }
	@Override public int getHardKey() { return 1; }
	@Override public int getPaletteKey() { return R.id.soft_key_1; }

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.getTextSelection() == null) {
			return false;
		}

		tt9.getTextSelection().selectNextChar(!LanguageKind.isRTL(tt9.getLanguage()));
		return true;
	}
}
