package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageKind;

public class CmdTxtSelectNextWord implements Command {
	public static final String ID = "key_txt_select_next_word";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_txt_word_forward; }
	@Override public int getName() { return 0; }
	@Override public int getHardKey() { return 6; }
	@Override public int getPaletteKey() { return io.github.sspanak.tt9.R.id.soft_key_6; }

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.getTextSelection() == null) {
			return false;
		}

		tt9.getTextSelection().selectNextWord(LanguageKind.isRTL(tt9.getLanguage()));
		return true;
	}
}
