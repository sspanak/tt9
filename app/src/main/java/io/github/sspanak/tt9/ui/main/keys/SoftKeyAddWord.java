package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyAddWord extends BaseSoftKeyWithIcons {
	public SoftKeyAddWord(Context context) { super(context); }
	public SoftKeyAddWord(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyAddWord(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected String getTitle() { return hasLettersOnAllKeys() ? Characters.SPACE : ""; }
	@Override protected int getCentralIcon() { return hasLettersOnAllKeys() ? 0 : R.drawable.ic_fn_add_word; }
	@Override protected float getTitleScale() { return hasLettersOnAllKeys() ? 1.3f * Math.min(1, getTT9Height()) * getScreenScaleY() : super.getTitleScale(); }

	@Override
	protected boolean handleRelease() {
		if (!validateTT9Handler()) {
			return false;
		}

		if (hasLettersOnAllKeys()) {
			return tt9.onKeySpaceKorean(false);
		} else {
			tt9.addWord();
			return true;
		}
	}

	@Override
	public void render() {
		if (tt9 != null) {
			setEnabled(
				(
					(tt9.isAddingWordsSupported() && !tt9.isFnPanelVisible())
					|| hasLettersOnAllKeys()
				)
				&& !tt9.isVoiceInputActive()
			);
		}
		super.render();
	}
}
