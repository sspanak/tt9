package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.util.TextTools;

public class SoftKeyNumber1 extends SoftKeyNumber {
	private static final String LARGE_LABEL_NUMERIC = "1";
	private static final String LARGE_LABEL_TEXT = ",:-)";
	private static final String HOLD_LABEL_NUMERIC_WHEN_LETTERS = "1 :-)";

	public SoftKeyNumber1(Context context) { super(context); }
	public SoftKeyNumber1(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber1(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected String getTitle() {
		if (isFnPanelOn()) {
			return super.getTitle();
		}

		if (tt9 == null || tt9.isInputModeNumeric()) {
			return LARGE_LABEL_NUMERIC;
		}

		if (hasLettersOnAllKeys() && tt9.getLanguage() != null) {
			return TextTools.removeNonLettersFromListAndJoin(tt9.getLanguage().getKeyCharacters(1));
		}

		return LARGE_LABEL_TEXT;
	}


	@Override
	protected float getTitleScale() {
		if (isFnPanelOn()) {
			return super.getTitleScale();
		} else {
			return super.getTitleScale() * (isBopomofo() ? TITLE_SCALE_BOPOMOFO : 1);
		}
	}


	@Override
	protected String getHoldText() {
		if (isFnPanelOn()) {
			return super.getHoldText();
		}

		if (tt9 == null || tt9.isNumericModeStrict()) {
			return null;
		}

		if (tt9.isInputModeNumeric()) {
			return LARGE_LABEL_TEXT;
		}

		if (hasLettersOnAllKeys() && !(tt9.isInputModeABC() && isBopomofo())) {
			return HOLD_LABEL_NUMERIC_WHEN_LETTERS;
		}

		return super.getLocalizedNumber(getNumber());
	}
}
