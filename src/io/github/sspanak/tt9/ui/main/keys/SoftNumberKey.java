package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import java.util.ArrayList;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class SoftNumberKey extends SoftKey {
	public SoftNumberKey(Context context) {
		super(context);
	}

	public SoftNumberKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftNumberKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected boolean handleHold() {
		if (tt9 == null) {
			return super.handleHold();
		}

		preventRepeat();
		int keyCode = Key.numberToCode(getNumber(getId()));
		tt9.onKeyLongPress(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		return true;
	}

	@Override
	protected boolean handleRelease() {
		int keyCode = Key.numberToCode(getNumber(getId()));
		if (keyCode < 0 || !validateTT9Handler()) {
			return false;
		}

		tt9.onKeyDown(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		tt9.onKeyUp(keyCode, new KeyEvent(KeyEvent.ACTION_UP, keyCode));

		return true;
	}

	@Override
	protected String getTitle() {
		return String.valueOf(getNumber(getId()));
	}

	@Override
	protected String getSubTitle() {
		if (tt9 == null) {
			return null;
		}

		int number = getNumber(getId());

		if (number == 0) {
			if (tt9.getInputMode() == InputMode.MODE_123) {
				return "+";
			} else {
				COMPLEX_LABEL_SUB_TITLE_SIZE = 1;
				return "␣";
			}
		}

		// 1
		if (number == 1) {
			return ",:-)";
		}

		// no other special labels in 123 mode
		if (tt9.getInputMode() == InputMode.MODE_123) {
			return null;
		}

		// 2-9
		Language language = LanguageCollection.getLanguage(tt9.getApplicationContext(), tt9.getSettings().getInputLanguage());
		if (language == null) {
			Logger.d("SoftNumberKey.getLabel", "Cannot generate a label when the language is NULL.");
			return "";
		}

		boolean isLatinBased = language.isLatinBased();
		boolean isGreekBased = language.isGreek();

		StringBuilder sb = new StringBuilder();
		ArrayList<String> chars = language.getKeyCharacters(number, false);
		for (int i = 0; i < 5 && i < chars.size(); i++) {
			String currentLetter = chars.get(i);
			if (
				(isLatinBased && currentLetter.charAt(0) > 'z')
				|| (isGreekBased && (currentLetter.charAt(0) < 'α' || currentLetter.charAt(0) > 'ω'))
			) {
				// As suggested by the community, there is no need to display the accented letters.
				// People are used to seeing just A-Z.
				continue;
			}

			sb.append(
				tt9.getTextCase() == InputMode.CASE_UPPER ? currentLetter.toUpperCase(language.getLocale()) : currentLetter
			);
		}

		return sb.toString();
	}

	private int getNumber(int keyId) {
		if (keyId == R.id.soft_key_0) return 0;
		if (keyId == R.id.soft_key_1) return 1;
		if (keyId == R.id.soft_key_2) return 2;
		if (keyId == R.id.soft_key_3) return 3;
		if (keyId == R.id.soft_key_4) return 4;
		if (keyId == R.id.soft_key_5) return 5;
		if (keyId == R.id.soft_key_6) return 6;
		if (keyId == R.id.soft_key_7) return 7;
		if (keyId == R.id.soft_key_8) return 8;
		if (keyId == R.id.soft_key_9) return 9;

		return -1;
	}
}
