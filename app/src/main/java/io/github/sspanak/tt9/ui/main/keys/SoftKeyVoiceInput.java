package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdVoiceInput;

public class SoftKeyVoiceInput extends BaseSoftKeyWithIcons {
	private final CmdVoiceInput voiceInput = new CmdVoiceInput();

	public SoftKeyVoiceInput(Context context) { super(context); }
	public SoftKeyVoiceInput(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyVoiceInput(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected boolean handleRelease() {
		return voiceInput.isAvailable(tt9) && voiceInput.run(tt9);
	}


	@Override
	protected String getAccessibilityText() {
		return voiceInput.getName(tt9);
	}


	@Override
	protected int getCentralIcon() {
		return voiceInput.isActive(tt9) ? voiceInput.getIconOff() : voiceInput.getIcon();
	}


	@Override
	public void render() {
		resetIconCache();
		setEnabled(voiceInput.isAvailable(tt9));
		super.render();
	}
}
