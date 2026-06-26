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

	private boolean isAddWordAvailable = false;
	private boolean isEditWordAvailable = false;

	@Override protected int getCentralIcon() { return isAddWordAvailable ? addWord.getIcon() : editWord.getIcon(); }

	@Override
	protected int getCornerIcon(int position) {
		return position == ICON_POSITION_TOP_RIGHT && isAddWordAvailable && isEditWordAvailable ? editWord.getIcon() : super.getCornerIcon(position);
	}

	@Override
	protected boolean handleRelease() {
		return isAddWordAvailable ? addWord.run(tt9) : editWord.run(tt9);
	}

	@Override
	protected String getAccessibilityText() {
		return isAddWordAvailable ? addWord.getName(tt9) : editWord.getName(tt9);
	}

	@Override
	protected void handleHold() {
		preventRepeat();
		if (isAddWordAvailable && isEditWordAvailable && editWord.run(tt9)) {
			vibrate(Vibration.getHoldVibration());
		}
		ignoreLastPressedKey();
	}

	@Override
	public void render() {
		isAddWordAvailable = addWord.isAvailable(tt9);
		isEditWordAvailable = editWord.isAvailable(tt9);
		resetIconCache();

		if (tt9 != null) {
			setEnabled((isAddWordAvailable || isEditWordAvailable) && !tt9.isFnPanelVisible() && !tt9.isVoiceInputActive());
		}

		super.render();
	}
}
