package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.UI;

public class CmdShift implements Command {
	public static final String ID = "key_shift";
	public String getId() { return ID; }
	public int getIcon() { return R.drawable.ic_fn_shift_low; }
	public int getIconCaps() { return R.drawable.ic_fn_shift_caps; }
	public int getIconUp() { return R.drawable.ic_fn_shift_up; }
	public int getName() { return R.string.virtual_key_shift; }

	@Override
	public boolean isAvailable(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& !tt9.shouldBeOff()
			&& !tt9.getVoiceInputOps().isListening()
			&& !tt9.isInputModeNumeric();
	}

	public boolean run(io.github.sspanak.tt9.ime.TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		tt9.getSuggestionOps().scheduleDelayedAccept(tt9.getInputMode().getAutoAcceptTimeout()); // restart the timer
		if (!tt9.nextTextCase()) {
			return false;
		}

		tt9.getDisplayTextCase(tt9.getLanguage(), tt9.getInputMode().getTextCase());
		tt9.setStatusIcon(tt9.getInputMode(), tt9.getLanguage());

		if (tt9.getStatusBar() != null) {
			tt9.getStatusBar().setText(tt9.getInputMode());
			tt9.getStatusBar().setAccessibilityTextCase(tt9.getInputMode());
		}

		if (tt9.getMainView() != null) {
			tt9.getMainView().render();
		}

		if (tt9.getSettings().isMainLayoutStealth() && !tt9.getSettings().isStatusIconEnabled()) {
			UI.toastShortSingle(tt9, tt9.getInputMode().getClass().getSimpleName(), tt9.getInputMode().toString());
		}

		return true;
	}
}
