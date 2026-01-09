package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdAddWord implements Command {
	public static final String ID = "key_add_word";
	public String getId() { return ID; }
	public int getIcon() { return io.github.sspanak.tt9.R.drawable.ic_fn_add_word; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_add_word; }

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& tt9.getSettings().getPredictiveMode()
			&& tt9.getLanguage() != null
			&& !tt9.getLanguage().isTranscribed();
	}

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}
		tt9.addWord();
		return true;
	}
}
