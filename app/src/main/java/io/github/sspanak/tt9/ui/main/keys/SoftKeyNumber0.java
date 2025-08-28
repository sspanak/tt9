package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.TextTools;

public class SoftKeyNumber0 extends SoftKeyFnNumpad {
	private static final String CHARS_NUMERIC_MODE = "+%$";

	public SoftKeyNumber0(Context context) { super(context); }
	public SoftKeyNumber0(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber0(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private boolean isKorean() {
		return tt9 != null && !tt9.isInputModeNumeric() && LanguageKind.isKorean(tt9.getLanguage());
	}


	private boolean shouldBeTransparent() {
		return tt9 != null && tt9.isFnPanelVisible() && hasLettersOnAllKeys();
	}


	protected boolean shouldHide() {
		return tt9 != null
			&& tt9.getSettings().isNumpadShapeLongSpace()
			&& !tt9.isInputModeNumeric()
			&& !hasLettersOnAllKeys();
	}


	@Override
	protected int getNumber(int keyId) {
		return 0;
	}


	@Override
	protected String getHoldText() {
		if (tt9 == null || shouldHide()) {
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

		return super.getLocalizedNumber(getNumber(getId()));
	}


	@Override
	protected String getTitle() {
		if (tt9 == null || tt9.isInputModeNumeric()) {
			return "0";
		}

		if (hasLettersOnAllKeys() && tt9.getLanguage() != null) {
			return TextTools.removeNonLettersFromListAndJoin(tt9.getLanguage().getKeyCharacters(0));
		}

		return "␣";
	}


	@Override
	protected float getTitleScale() {
		if (isKorean() || (tt9 != null && tt9.isInputModeNumeric())) {
			return super.getTitleScale();
		}

		if (isBopomofo()) {
			return super.getTitleScale() * TITLE_SCALE_BOPOMOFO;
		}

		// scale up the space character, because it is too small
		return 1.3f * Math.min(1, getTT9Height()) * getScreenScaleY();
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
