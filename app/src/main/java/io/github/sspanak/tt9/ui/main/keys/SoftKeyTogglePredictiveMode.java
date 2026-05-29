package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdTogglePredictiveMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;

public class SoftKeyTogglePredictiveMode extends BaseSoftKeyCustomizable {
	private final CmdTogglePredictiveMode toggleMode = new CmdTogglePredictiveMode();

	public SoftKeyTogglePredictiveMode(Context context) { super(context); }
	public SoftKeyTogglePredictiveMode(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTogglePredictiveMode(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		return toggleMode.run(tt9);
	}

	@Override
	protected String getAccessibilityText() {
		return toggleMode.getName(tt9);
	}

	@Override
	protected String getTitle() {
		if (tt9 == null) {
			return "";
		}

		if (InputModeKind.isPredictive(tt9.getInputMode())) {
			return "T9";
		} else if (tt9.isInputModeNumeric()) {
			return "--";
		} else {
			return tt9.getInputModeName();
		}
	}

	@Override
	protected float getTitleScale() {
		return super.getTitleScale() * (isBopomofo() && tt9.isInputModeABC() ? 0.63f : 0.9f);
	}

	@Override
	public void render() {
		setEnabled(toggleMode.isAvailable(tt9));
		super.render();
	}
}
