package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdBack;
import io.github.sspanak.tt9.commands.CmdEditText;
import io.github.sspanak.tt9.commands.CmdTxtCut;
import io.github.sspanak.tt9.commands.CmdVoiceInput;

public class SoftKeyRF3 extends BaseSoftKeyWithIcons {
	private final CmdBack back = new CmdBack();
	private final CmdEditText editText = new CmdEditText();
	private final CmdVoiceInput voiceInput = new CmdVoiceInput();

	public SoftKeyRF3(Context context) { super(context); }
	public SoftKeyRF3(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyRF3(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private boolean isKeySmall() {
		return getTT9Height() < 0.8f && getTT9Width() < 0.7f;
	}


	@Override
	protected void handleHold() {
		preventRepeat();

		if (editText.isActive(tt9) || voiceInput.isMissing(tt9)) {
			return;
		}

		voiceInput.run(tt9);
	}


	@Override
	protected boolean handleRelease() {
		if (editText.isMissing(tt9) && voiceInput.isMissing(tt9)) {
			return false;
		}

		if (voiceInput.isActive(tt9) || editText.isMissing(tt9)) {
			voiceInput.run(tt9);
		} else {
			editText.run(tt9);
		}

		return true;
	}


	@Override
	protected int getCentralIcon() {
		if (editText.isActive(tt9)) {
			return back.getIcon();
		}

		if (voiceInput.isActive(tt9)) {
			return voiceInput.getIconOff();
		}

		if (editText.isMissing(tt9) && voiceInput.isAvailable(tt9)) {
			return voiceInput.getIcon();
		}

		return new CmdTxtCut().getIcon();
	}


	@Override
	protected float getCentralIconScale() {
		float scale = 1;
		if (!voiceInput.isActive(tt9) && !editText.isActive(tt9) && editText.isAvailable(tt9)) {
			scale = isKeySmall() ? 0.7f : 0.8f;
		}

		return super.getCentralIconScale() * scale;
	}


	@Override
	protected int getCornerIcon(int position) {
		if (position != ICON_POSITION_TOP_RIGHT || voiceInput.isActive(tt9) || editText.isActive(tt9) || editText.isMissing(tt9) || voiceInput.isMissing(tt9)) {
			return -1;
		}

		return voiceInput.getIcon();
	}


	@Override
	public void render() {
		resetIconCache();
		setEnabled(voiceInput.isAvailable(tt9) || editText.isAvailable(tt9));
		super.render();
	}
}
