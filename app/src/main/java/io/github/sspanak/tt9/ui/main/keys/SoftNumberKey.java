package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;
import io.github.sspanak.tt9.util.Logger;

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
	protected void handleHold() {
		int keyCode = Key.numberToCode(getUpsideDownNumber(getId()));
		if (keyCode < 0 || !validateTT9Handler()) {
			return;
		}

		preventRepeat();

		vibrate(Vibration.getHoldVibration());
		tt9.onKeyLongPress(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		tt9.onKeyUp(keyCode, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
	}

	@Override
	protected boolean handleRelease() {
		int keyCode = Key.numberToCode(getUpsideDownNumber(getId()));
		if (keyCode < 0 || !validateTT9Handler()) {
			return false;
		}

		tt9.onKeyDown(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		tt9.onKeyUp(keyCode, new KeyEvent(KeyEvent.ACTION_UP, keyCode));

		return true;
	}

	@Override
	protected String getTitle() {
		int number = getNumber(getId());

		if (tt9 != null && !tt9.isInputModeNumeric() && LanguageKind.isArabic(tt9.getLanguage())) {
			complexLabelTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_ARABIC_TITLE_RELATIVE_SIZE;
			return tt9.getLanguage().getKeyNumber(number);
		} else {
			complexLabelTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_TITLE_RELATIVE_SIZE;
			return String.valueOf(number);
		}
	}

	@Override
	protected String getSubTitle() {
		if (tt9 == null) {
			return null;
		}

		int number = getNumber(getId());

		// 0
		if (number == 0) {
			if (tt9.isNumericModeSigned()) {
				return "+/-";
			} else if (tt9.isNumericModeStrict()) {
				return null;
			} else if (tt9.isInputModeNumeric()) {
				return "+";
			} else {
				complexLabelSubTitleSize = 1;
				return "␣";
			}
		}

		// 1
		if (number == 1) {
			return tt9.isNumericModeStrict() ? null : ",:-)";
		}

		// no other special labels in 123 mode
		if (tt9.isInputModeNumeric()) {
			return null;
		}

		// 2-9
		Language language = tt9.getLanguage();
		if (language == null) {
			Logger.d("SoftNumberKey.getLabel", "Cannot generate a label when the language is NULL.");
			return "";
		}

		boolean isLatinBased = LanguageKind.isLatinBased(language);
		boolean isGreekBased = LanguageKind.isGreek(language);

		StringBuilder sb = new StringBuilder();
		ArrayList<String> chars = language.getKeyCharacters(number);
		for (int i = 0; sb.length() < 5 && i < chars.size(); i++) {
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

	protected int getNumber(int keyId) {
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

	protected int getUpsideDownNumber(int keyId) {
		int number = getNumber(keyId);

		if (tt9 != null && tt9.getSettings().getUpsideDownKeys()) {
			if (number == 1) return 7;
			if (number == 2) return 8;
			if (number == 3) return 9;
			if (number == 7) return 1;
			if (number == 8) return 2;
			if (number == 9) return 3;
		}

		return number;
	}
}
