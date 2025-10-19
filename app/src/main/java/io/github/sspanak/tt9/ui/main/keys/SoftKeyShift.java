package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdShift;
import io.github.sspanak.tt9.commands.CmdSpaceKorean;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyShift extends BaseSoftKeyWithIcons {
	public SoftKeyShift(Context context) {
		super(context);
	}

	public SoftKeyShift(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyShift(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private boolean isShiftEnabled() {
		return tt9 != null
			&& tt9.getLanguage() != null && tt9.getLanguage().hasUpperCase()
			&& !tt9.isVoiceInputActive()
			&& !tt9.isInputModeNumeric()
			&& !tt9.isFnPanelVisible();
	}

	@Override public boolean isDynamic() { return true; }
	@Override protected String getTitle() { return hasLettersOnAllKeys() ? Characters.SPACE : ""; }
	@Override protected float getTitleScale() { return hasLettersOnAllKeys() ? 1.3f * Math.min(1, getTT9Height()) * getScreenScaleY() : super.getTitleScale(); }

	@Override protected int getCentralIcon() {
		if (hasLettersOnAllKeys()) {
			return 0;
		}

		final int textCase = tt9 != null ? tt9.getDisplayTextCase() : InputMode.CASE_UNDEFINED;
		return switch (textCase) {
			case InputMode.CASE_CAPITALIZE -> new CmdShift().getIconCaps();
			case InputMode.CASE_UPPER -> new CmdShift().getIconUp();
			default -> new CmdShift().getIcon();
		};
	}

	@Override
	protected boolean handleRelease() {
		return hasLettersOnAllKeys() ? new CmdSpaceKorean().run(tt9) : new CmdShift().run(tt9);
	}

	@Override
	public void render() {
		resetIconCache();
		setEnabled(isShiftEnabled() || hasLettersOnAllKeys());
		super.render();
	}
}
