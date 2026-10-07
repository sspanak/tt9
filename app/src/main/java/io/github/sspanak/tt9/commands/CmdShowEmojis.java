package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputModeKind;

public class CmdShowEmojis implements Command {
	public static final String ID = "key_show_emojis";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_fn_show_emojis; }
	@Override public int getName() { return R.string.function_show_emojis; }


	@Override public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& isAvailableStd(tt9)
			&& tt9.getInputType().isText()
			&& !tt9.areEmojiCategoriesVisible()
			&& !tt9.isTouchExplorationEnabled();
	}


	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || !isAvailable(tt9)) {
			return false;
		}

		if (!tt9.getInputMode().containsEmojis()) {
			tt9.getInputMode().loadEmojis();
			tt9.getSuggestions(0, null, null);
		}

		return true;
	}
}
