package io.github.sspanak.tt9.commands;

import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.helpers.Key;

public class CmdShowEmojis implements Command {
	@Override public String getId() { return "cmd_show_emojis"; }
	@Override public int getIcon() { return R.drawable.ic_fn_show_emoijs; }
	@Override public int getName() { return R.string.function_show_emojis; }

	@Override public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& !tt9.isInputTypeNumeric()
			&& !tt9.areEmojiCategoriesVisible()
			&& !tt9.isTouchExplorationEnabled();
	}

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || !isAvailable(tt9)) {
			return false;
		}

		final int keyCode = Key.numberToCode(tt9.getSettings(), 1);

		if (!tt9.getInputMode().containsEmojis()) {
			firstPress1(tt9, keyCode);
		}
		secondPress1(tt9, keyCode);

		return true;
	}

	private void firstPress1(@NonNull TraditionalT9 tt9, int keyCode1) {
		final boolean useHold = tt9.getLanguage() != null && tt9.getLanguage().hasLettersOnAllKeys();

		if (useHold) {
			tt9.onKeyLongPress(keyCode1, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode1));
		} else {
			tt9.onKeyDown(keyCode1, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode1));
		}
		tt9.onKeyUp(keyCode1, new KeyEvent(KeyEvent.ACTION_UP, keyCode1));
	}

	private void secondPress1(@NonNull TraditionalT9 tt9, int keyCode1) {
		tt9.onKeyDown(keyCode1, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode1));
		tt9.onKeyUp(keyCode1, new KeyEvent(KeyEvent.ACTION_UP, keyCode1));
	}
}
