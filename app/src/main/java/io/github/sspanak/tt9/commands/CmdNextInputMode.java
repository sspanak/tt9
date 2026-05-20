package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ui.StatusIcon;

public class CmdNextInputMode implements Command {
	public static final String ID = "key_next_input_mode";
	public String getId() { return ID; }
	public int getIcon() { return StatusIcon.getCachedResourceId(); }
	public int getName() { return R.string.function_next_mode; }

	public void invalidateIcon(@Nullable TraditionalT9 tt9) {
		new StatusIcon(
			tt9 != null ? tt9.getInputMode() : null,
			tt9 != null ? tt9.getLanguage() : null,
			tt9 != null ? tt9.getDisplayTextCase() : 0
		);
	}

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& tt9.getAllowedInputModes().size() > 1
			&& !tt9.shouldBeOff()
			&& !tt9.isVoiceInputActive()
			&& !InputModeKind.isPassthrough(tt9.getInputMode());
	}


	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		InputMode inputMode = tt9.getInputMode();

		tt9.getSuggestionOps().scheduleDelayedAccept(inputMode.getAutoAcceptTimeout()); // restart the timer
		final int nextModeId = getNextInputMode(tt9);
		if (nextModeId != inputMode.getId()) {
			tt9.setInputMode(nextModeId);
		}

		tt9.forceShowWindow();
		return true;
	}


	private int getNextInputMode(@NonNull TraditionalT9 tt9) {
		ArrayList<Integer> allowedInputModes = tt9.getAllowedInputModes();
		InputMode currentMode = tt9.getInputMode();

		if (allowedInputModes.size() == 1 && allowedInputModes.contains(InputMode.MODE_123) && !InputModeKind.is123(currentMode)) {
			return InputMode.MODE_123;
		} else {
			final int nextModeIndex = (allowedInputModes.indexOf(currentMode.getId()) + 1) % allowedInputModes.size();
			return allowedInputModes.get(nextModeIndex);
		}
	}
}
