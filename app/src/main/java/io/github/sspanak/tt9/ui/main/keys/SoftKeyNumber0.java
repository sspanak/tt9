package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyNumber0 extends SoftKeyNumber {
	private static final String CHARS_NUMERIC_MODE = "+%$";

	public SoftKeyNumber0(Context context) { super(context); }
	public SoftKeyNumber0(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber0(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	public static boolean isMe(int keyId) {
		return keyId == R.id.soft_key_0;
	}


	private boolean shouldBeTransparent() {
		return tt9 != null && tt9.isTextEditingActive() && hasLettersOnAllKeys();
	}


	protected boolean shouldHide() {
		if (tt9 == null || tt9.isInputModeNumeric()) {
			return false;
		}

		final boolean isKeypadLong = tt9.getSettings().isNumpadShapeLongSpace();
		final boolean isKeyLong = this instanceof SoftKeyNumber0Long;

		if (hasLettersOnAllKeys() || isFnPanelOn()) {
			return isKeyLong;
		}

		return isKeypadLong != isKeyLong;
	}


	@Override
	protected int getNumber(int keyId) {
		return 0;
	}


	@Override
	public boolean isHoldEnabled() {
		return tt9 != null && tt9.getSettings().getHoldToType() && !shouldHide();
	}


	@Override
	protected String getHoldText() {
		if (isFnPanelOn()) {
			return super.getHoldText();
		}

		if (tt9 == null || !isHoldEnabled()) {
			return null;
		}

		if (tt9.isTextEditingActive() || tt9.isNumericModeStrict()) {
			return "";
		} if (tt9.isNumericModeSigned()) {
			return "+/-";
		} else if (tt9.isInputModePhone()) {
			return "+";
		} else if (tt9.isInputModeNumeric() || hasLettersOnAllKeys()) {
			return CHARS_NUMERIC_MODE;
		}

		return super.getLocalizedNumber(getNumber());
	}


	@Override
	protected String getTitle() {
		if (isFnPanelOn()) {
			return super.getTitle();
		}

		if (tt9 == null || tt9.isInputModeNumeric()) {
			return "0";
		}

		if (hasLettersOnAllKeys() && tt9.getLanguage() != null) {
			return TextTools.removeNonLettersFromListAndJoin(tt9.getLanguage().getKeyCharacters(0));
		}

		return Characters.SPACE;
	}


	@Override
	protected float getTitleScale() {
		if (isBopomofo()) {
			return super.getTitleScale() * TITLE_SCALE_BOPOMOFO;
		}

		// scale up the space character, because it is too small
		if (Characters.SPACE.equals(getTitle())) {
			return 1.3f * Math.min(1, getTT9Height()) * getScreenScaleY();
		}

		return super.getTitleScale();
	}


	private void setVisibility() {
		getOverlayWrapper();
		if (shouldHide()) {
			overlay.setVisibility(GONE);
		} else if (shouldBeTransparent()) {
			overlay.setVisibility(INVISIBLE);
		} else {
			overlay.setVisibility(VISIBLE);
		}
	}


	private void setEnabled() {
		setEnabled(tt9 != null && !(tt9.isTextEditingActive() && hasLettersOnAllKeys()));
	}

	@Override
	public void render() {
		setVisibility();
		setEnabled();
		super.render();
	}
}
