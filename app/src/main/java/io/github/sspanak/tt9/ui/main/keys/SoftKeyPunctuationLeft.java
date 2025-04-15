package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyPunctuationLeft extends SoftKeyPunctuation {
	public SoftKeyPunctuationLeft(Context context) { super(context); }
	public SoftKeyPunctuationLeft(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuationLeft(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected String getKeyChar() {
		if (tt9 == null) return "";
		if (tt9.isInputModePhone()) return "*";
		if (tt9.isInputModeNumeric()) return ",";
		if (tt9.isTextEditingActive()) return "↶";

		if (LanguageKind.isChinese(tt9.getLanguage()) || LanguageKind.isJapanese(tt9.getLanguage())) {
			return Characters.ZH_EXCLAMATION_MARK;
		}

		return "!";
	}


	@Override
	protected boolean handleRelease() {
		return tt9 != null && tt9.isTextEditingActive() ? tt9.onKeyUndo(false) : super.handleRelease();
	}
}
