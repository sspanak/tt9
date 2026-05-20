package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ui.StatusIcon;

public class CmdTogglePredictiveMode implements Command {
	public static final String ID = "key_toggle_predictive_mode";
	@Override public String getId() { return ID; }
	@Override public int getIcon() { return StatusIcon.getCachedResourceId(); }
	@Override public int getName() { return R.string.function_toggle_predictive_mode; }

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
			&& isAvailableStd(tt9)
			&& !tt9.isVoiceInputActive()
			&& (InputModeKind.isABC(tt9.getInputMode()) || InputModeKind.isPredictive(tt9.getInputMode()))
			&& !tt9.isFnPanelVisible()
			&& tt9.getLanguage() != null
			&& !tt9.getLanguage().isTranscribed();
	}

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || !isAvailable(tt9)) {
			return false;
		}

		ArrayList<Integer> allowedInputModes = tt9.getAllowedInputModes();
		if (InputModeKind.isPredictive(tt9.getInputMode()) && allowedInputModes.contains(InputMode.MODE_ABC)) {
			tt9.setInputMode(InputMode.MODE_ABC);
		} else if (InputModeKind.isABC(tt9.getInputMode()) && allowedInputModes.contains(InputMode.MODE_PREDICTIVE)) {
			tt9.setInputMode(InputMode.MODE_PREDICTIVE);
		}

		return true;
	}
}
