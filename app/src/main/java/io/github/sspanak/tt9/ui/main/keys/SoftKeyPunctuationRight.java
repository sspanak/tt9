package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyPunctuationRight extends SoftKeyPunctuation {
	public SoftKeyPunctuationRight(Context context) { super(context); }
	public SoftKeyPunctuationRight(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuationRight(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	protected String getKeyChar() {
		if (tt9 == null) return "";
		if (tt9.isInputModePhone()) return "#";
		if (tt9.isInputModeNumeric()) return ".";
		if (tt9.isTextEditingActive()) return "↷";

		if (LanguageKind.isArabic(tt9.getLanguage())) return Characters.AR_QUESTION_MARK;
		if (LanguageKind.isGreek(tt9.getLanguage())) return Characters.GR_QUESTION_MARK;
		if (LanguageKind.isChinese(tt9.getLanguage()) || LanguageKind.isJapanese(tt9.getLanguage())) {
			return Characters.ZH_QUESTION_MARK;
		}

		return "?";
	}


	@Override
	protected boolean handleRelease() {
		return tt9 != null && tt9.isTextEditingActive() ? tt9.onKeyRedo(false) : super.handleRelease();
	}
}
