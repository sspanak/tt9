package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputModeKind;

public class CmdTogglePredictiveMode implements Command {
	@Override public String getId() { return "cmd_toggle_predictive_mode"; }
	@Override public int getIcon() { return 0; }
	@Override public int getName() { return R.string.function_toggle_predictive_mode; }

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& !tt9.isVoiceInputActive()
			&& (InputModeKind.isABC(tt9.getInputMode()) || InputModeKind.isPredictive(tt9.getInputMode()))
			&& !tt9.isFnPanelVisible()
			&& tt9.getLanguage() != null
			&& !tt9.getLanguage().isTranscribed();
	}

	@Override
	public boolean run(@Nullable TraditionalT9 tt9) {
		if (tt9 != null && isAvailable(tt9)) {
			tt9.togglePredictiveMode();
			return true;
		}

		return false;
	}
}
