package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.languages.LanguageKind;

public class SoftKeyNumber1 extends SoftKeyNumber {
	private static final String DEFAULT_LABEL = ",:-)";
	private static final String KOREAN_LABEL = "ㅣ,:)";

	public SoftKeyNumber1(Context context) { super(context); }
	public SoftKeyNumber1(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber1(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected String getSubTitle() {
		if (tt9 == null || tt9.isNumericModeStrict()) {
			return null;
		}

		return LanguageKind.isKorean(tt9.getLanguage()) ? KOREAN_LABEL : DEFAULT_LABEL;
	}
}
