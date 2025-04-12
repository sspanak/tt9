package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyPunctuation extends SoftKey {
	public SoftKeyPunctuation(Context context) { super(context); }
	public SoftKeyPunctuation(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuation(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	protected boolean isHiddenWhenLongSpace() {
		return
			tt9 != null
			&& tt9.getSettings().isNumpadShapeLongSpace()
			&& !tt9.isInputModeNumeric()
			&& !hasLettersOnAllKeys();
	}

	protected boolean isTransparentWhenTextEditing() {
		return tt9 != null && tt9.isTextEditingActive();
	}

	@Override
	protected boolean handleRelease() {
		return
			tt9 != null
			&& !tt9.isTextEditingActive()
			&& tt9.onText(getKeyChar(), false);
	}

	@Override
	protected String getTitle() {
		String keyChar = getKeyChar();
		return switch (keyChar) {
			case "*" -> "✱";
			case Characters.ZH_QUESTION_MARK -> "?";
			case Characters.ZH_EXCLAMATION_MARK -> "!";
			default -> keyChar;
		};
	}

	protected String getKeyChar() {
		int keyId = getId();
		if (keyId == R.id.soft_key_punctuation_1) {
			return getKey1Char();
		} else if (keyId == R.id.soft_key_punctuation_2) {
			return getKey2Char();
		}

		return "";
	}

	protected String getKey1Char() {
		if (tt9 == null) return "";
		if (tt9.isInputModePhone()) return "*";
		if (tt9.isInputModeNumeric()) return ",";

		if (LanguageKind.isChinese(tt9.getLanguage()) || LanguageKind.isJapanese(tt9.getLanguage())) {
			return Characters.ZH_EXCLAMATION_MARK;
		}

		return "!";
	}

	protected String getKey2Char() {
		if (tt9 == null) return "";
		if (tt9.isInputModePhone()) return "#";
		if (tt9.isInputModeNumeric()) return ".";

		if (LanguageKind.isArabic(tt9.getLanguage())) return Characters.AR_QUESTION_MARK;
		if (LanguageKind.isGreek(tt9.getLanguage())) return Characters.GR_QUESTION_MARK;
		if (LanguageKind.isChinese(tt9.getLanguage()) || LanguageKind.isJapanese(tt9.getLanguage())) {
			return Characters.ZH_QUESTION_MARK;
		}

		return "?";
	}

	@Override
	public void setHeight(int height) {
		if (tt9 != null && tt9.getSettings().isMainLayoutNumpad() && tt9.getSettings().isNumpadShapeV()) {
			height = Math.round(height * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_INNER);
		}

		super.setHeight(height);
	}


	@Override
	public void render() {
		if (isHiddenWhenLongSpace()) {
			setVisibility(GONE);
		} else if (isTransparentWhenTextEditing()) {
			setVisibility(INVISIBLE);
		} else {
			setVisibility(VISIBLE);
		}

		super.render();
	}
}
