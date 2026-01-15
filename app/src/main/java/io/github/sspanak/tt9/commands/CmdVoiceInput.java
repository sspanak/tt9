package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdVoiceInput implements Command {
	public static final String ID = "key_voice_input";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_fn_voice; }
	public int getIconOff() { return R.drawable.ic_fn_voice_off; }
	@Override public int getName() { return R.string.function_voice_input; }
	@Override public int getHardKey() { return 3; }
	@Override public int getPaletteKey() { return R.id.soft_key_3; }

	@Override
	public boolean run(TraditionalT9 tt9) {
		if (tt9 != null) {
			tt9.toggleVoiceInput();
			return true;
		}
		return false;
	}

	public boolean isActive(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.isVoiceInputActive();
	}

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return !isMissing(tt9);
	}

	public boolean isMissing(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.isVoiceInputMissing();
	}
}
