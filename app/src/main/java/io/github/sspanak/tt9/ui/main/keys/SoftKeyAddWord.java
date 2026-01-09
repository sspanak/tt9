package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdAddWord;

public class SoftKeyAddWord extends BaseSoftKeyWithIcons {
	private final CmdAddWord addWord = new CmdAddWord();

	public SoftKeyAddWord(Context context) { super(context); }
	public SoftKeyAddWord(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyAddWord(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected int getCentralIcon() { return addWord.getIcon(); }

	@Override
	protected boolean handleRelease() {
		return addWord.run(tt9);
	}

	@Override
	public void render() {
		if (tt9 != null) {
			setEnabled(addWord.isAvailable(tt9) && !tt9.isFnPanelVisible() && !tt9.isVoiceInputActive());
		}
		super.render();
	}
}
