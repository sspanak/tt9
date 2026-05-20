package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdCommandPalette implements Command {
	public static final String ID = "key_command_palette";
	public String getId() { return ID; }
	public int getIconText() { return R.string.virtual_key_command_palette; }
	public String getIconEmojiText() { return "☰"; }
	public int getIcon() { return R.drawable.ic_fn_command_palette; }
	public int getName() { return io.github.sspanak.tt9.R.string.function_show_command_palette; }


	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& tt9.getMainView() != null
			&& !tt9.shouldBeOff();
	}


	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.getMainView() == null) {
			return false;
		}

		if (tt9.getMainView().isCommandPaletteShown()) {
			hideCommandPalette(tt9);
		} else {
			showCommandPalette(tt9);
			tt9.forceShowWindow();
		}
		return true;
	}


	public void showCommandPalette(@NonNull TraditionalT9 tt9) {
		if (tt9.getMainView() == null || tt9.getMainView().isCommandPaletteShown()) {
			return;
		}

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.getInputMode().onAcceptSuggestion(tt9.getSuggestionOps().acceptIncomplete());
		tt9.getInputMode().reset();

		tt9.getMainView().showCommandPalette();
		tt9.resetStatus();
	}


	public boolean hideCommandPalette(@NonNull TraditionalT9 tt9) {
		if (tt9.getMainView() == null || !tt9.getMainView().isCommandPaletteShown()) {
			return false;
		}

		tt9.getMainView().showKeyboard();
		if (tt9.getVoiceInputOps().isListening()) {
			tt9.stopVoiceInput();
		} else {
			tt9.resetStatus();
		}

		return true;
	}
}
