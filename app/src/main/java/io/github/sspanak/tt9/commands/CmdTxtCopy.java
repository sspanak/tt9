package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.util.sys.Clipboard;

public class CmdTxtCopy implements Command {
	public static final String ID = "key_txt_copy";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_txt_copy; }
	@Override public int getName() { return R.string.function_txt_copy; }
	@Override public int getHardKey() { return 8; }
	@Override public int getPaletteKey() { return R.id.soft_key_8; }

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.getTextSelection() == null) {
			return false;
		}

		CharSequence selectedText = tt9.getTextSelection().getSelectedText();
		if (selectedText.length() == 0) {
			return false;
		}

		Clipboard.copy(tt9, selectedText);
		return true;
	}
}
