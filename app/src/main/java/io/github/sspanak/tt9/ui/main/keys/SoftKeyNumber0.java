package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.languages.LanguageKind;

public class SoftKeyNumber0 extends SoftKeyNumber {
	private static final String CHARS_KOREAN = "+%0";
	private static final String CHARS_NUMERIC_MODE = "+%$";

	public SoftKeyNumber0(Context context) { super(context); }
	public SoftKeyNumber0(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber0(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected String getTitle() {
		if (tt9 == null) {
			return super.getTitle();
		}

		if (tt9.isNumericModeStrict()) {
			return "0";
		} if (tt9.isNumericModeSigned()) {
			return "+/-";
		} else if (tt9.isInputModePhone()) {
			return "+";
		} else if (tt9.isInputModeNumeric()) {
			return CHARS_NUMERIC_MODE;
		} else if (LanguageKind.isKorean(tt9.getLanguage())) {
			return CHARS_KOREAN;
		}

		return super.getTitle();
	}

	@Override
	protected String getSubTitle() {
		if (tt9 == null) {
			return null;
		}

		if (tt9.isNumericModeStrict()) {
			return null;
		} else if (tt9.isInputModeNumeric()) {
			return "0";
		} else if (LanguageKind.isKorean(tt9.getLanguage())) {
			return getKoreanCharList();
		} else {
			return "␣";
		}
	}

	private String getKoreanCharList() {
		if (tt9 == null || tt9.getLanguage() == null) {
			return null;
		}

		StringBuilder list = new StringBuilder();
		for (String character : tt9.getLanguage().getKeyCharacters(0)) {
			if (Character.isAlphabetic(character.charAt(0))) {
				list.append(character);
			}
		}

		return list.toString();
	}

	@Override
	protected float getSubTitleRelativeSize() {
		if (tt9 != null && !tt9.isInputModeNumeric() && !LanguageKind.isKorean(tt9.getLanguage())) {
			return 1.1f;
		}

		return super.getSubTitleRelativeSize();
	}
}
