package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.languages.LanguageKind;

public class SoftKeyNumber0 extends SoftKeyNumber {
	private static final String CHARS_NUMERIC_MODE = "+%$";

	public SoftKeyNumber0(Context context) { super(context); }
	public SoftKeyNumber0(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber0(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private boolean isTransparentWhenTextEditing() {
		return tt9 != null && LanguageKind.isKorean(tt9.getLanguage()) && tt9.isTextEditingActive();
	}


	protected boolean isHiddenWhenLongSpace() {
		return tt9 != null
			&& tt9.getSettings().isNumpadShapeLongSpace()
			&& !tt9.isInputModeNumeric()
			&& !LanguageKind.isKorean(tt9.getLanguage());
	}


	@Override
	protected int getNumber(int keyId) {
		return 0;
	}


	@Override
	protected String getHoldText() {
		if (tt9 == null || isHiddenWhenLongSpace()) {
			return null;
		}

		if (tt9.isTextEditingActive() || tt9.isNumericModeStrict()) {
			return "";
		} if (tt9.isNumericModeSigned()) {
			return "+/-";
		} else if (tt9.isInputModePhone()) {
			return "+";
		} else if (tt9.isInputModeNumeric() || LanguageKind.isKorean(tt9.getLanguage())) {
			return CHARS_NUMERIC_MODE;
		}

		return super.getLocalizedNumber(getNumber(getId()));
	}


	@Override
	protected String getTitle() {
		if (tt9 == null || tt9.isInputModeNumeric()) {
			return "0";
		}

		return (LanguageKind.isKorean(tt9.getLanguage())) ? getKoreanCharList() : "␣";
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
	protected float getTitleScale() {
		if (tt9 != null && !tt9.isInputModeNumeric() && !LanguageKind.isKorean(tt9.getLanguage())) {
			return 1.3f * Math.min(1, getTT9Height()) * getScreenScaleY();
		}

		return super.getTitleScale();
	}


	private void setVisibility() {
		getOverlayWrapper();
		if (isHiddenWhenLongSpace()) {
			overlay.setVisibility(GONE);
		} else if (isTransparentWhenTextEditing()) {
			overlay.setVisibility(INVISIBLE);
		} else {
			overlay.setVisibility(VISIBLE);
		}
	}


	private void setEnabled() {
		setEnabled(
			tt9 != null
				&& (
					!tt9.isTextEditingActive()
					|| (!LanguageKind.isKorean(tt9.getLanguage()) && !tt9.isInputModeNumeric())
				)
		);
	}


	@Override
	public void render() {
		setVisibility();
		setEnabled();
		super.render();
	}
}
