package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.util.sys.Clipboard;

public class CmdEditText implements Command {
	public static final String ID = "key_edit_text";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return R.drawable.ic_fn_edit_text; }
	@Override public int getName() { return R.string.function_edit_text; }
	@Override public int getHardKey() { return 5; }
	@Override public int getPaletteKey() { return R.id.soft_key_5; }


	public boolean isActive(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.isTextEditingActive();
	}


	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& !tt9.shouldBeOff()
			&& !isMissing(tt9);
	}


	public boolean isMissing(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.isInputLimited();
	}


	private void showTextEditingPalette(@NonNull TraditionalT9 tt9) {
		if (!isAvailable(tt9) || tt9.getMainView() == null || tt9.getMainView().isTextEditingPaletteShown()) {
			return;
		}

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.getSuggestionOps().acceptIncomplete();
		tt9.getInputMode().reset();
		tt9.stopVoiceInput();

		tt9.getMainView().showTextEditingPalette();
		tt9.resetStatus();
	}


	public boolean hideTextEditingPalette(@NonNull TraditionalT9 tt9) {
		if (tt9.getMainView() == null || !tt9.isTextEditingActive()) {
			return false;
		}

		// paste any selected clipboard item and change its priority
		String word = tt9.getSuggestionOps().acceptCurrent();
		if (Clipboard.contains(word)) {
			Clipboard.copy(tt9, word);
		}

		tt9.getMainView().showKeyboard();
		tt9.resetStatus();
		return true;
	}


	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		} else if (tt9.isTextEditingActive()) {
			return hideTextEditingPalette(tt9);
		} else {
			showTextEditingPalette(tt9);
			return true;
		}
	}
}
