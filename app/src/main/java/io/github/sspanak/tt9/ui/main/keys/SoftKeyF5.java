package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyF5 extends SoftKeyFn {
	public SoftKeyF5(Context context) {
		super(context);
	}

	public SoftKeyF5(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyF5(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected String getSubTitle() {
		return Characters.noEmojiSupported() ? getTextSubTitle(R.string.virtual_key_text_editing) : "✂";
	}

	@Override
	public void render() {
		setVisibility(tt9 != null && tt9.isInputLimited() ? GONE : VISIBLE);
		super.render();
	}
}
