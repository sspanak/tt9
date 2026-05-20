package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import java.util.LinkedList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.sys.Clipboard;

public class CmdTxtPaste implements Command {
	public static final String ID = "key_txt_paste";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_txt_paste; }
	@Override public int getName() { return R.string.function_txt_paste; }
	@Override public int getHardKey() { return 9; }
	@Override public int getPaletteKey() { return R.id.soft_key_9; }

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		if (!tt9.getSuggestionOps().isEmpty()) {
			tt9.getSuggestionOps().clear();
			return true;
		}

		LinkedList<CharSequence> clips = Clipboard.getAll(tt9);
		if (clips.isEmpty()) {
			UI.toast(tt9, R.string.commands_clipboard_is_empty);
			return true;
		}

		tt9.getInputMode().reset();
		tt9.getSuggestionOps().setClipboardItems(clips);
		tt9.getAppHacks().setComposingTextWithHighlightedStem(tt9.getSuggestionOps().getCurrent(), null, false);

		return true;
	}
}
