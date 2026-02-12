package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdAddWord;
import io.github.sspanak.tt9.commands.CmdEditWord;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyAddWord extends BaseSoftKeyWithIcons {
	private final CmdAddWord addWord = new CmdAddWord();
	private final CmdEditWord editWord = new CmdEditWord();

	public SoftKeyAddWord(Context context) { super(context); }
	public SoftKeyAddWord(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyAddWord(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected int getCentralIcon() { return addWord.getIcon(); }

	@Override
	protected int getCornerIcon(int position) {
		return position == ICON_POSITION_TOP_RIGHT ? editWord.getIcon() : super.getCornerIcon(position);
	}

	@Override
	protected boolean handleRelease() {
		return addWord.run(tt9);
	}

	@Override
	protected void handleHold() {
		preventRepeat();
		if (editWord.run(tt9)) {
			vibrate(Vibration.getHoldVibration());
		}
		ignoreLastPressedKey();
	}

	@Override
	public void render() {
		if (tt9 != null) {
			setEnabled(addWord.isAvailable(tt9) && !tt9.isFnPanelVisible() && !tt9.isVoiceInputActive());
		}
		super.render();
	}
}
