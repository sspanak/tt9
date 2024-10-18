package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.TextTools;

public class SoftKeyNumber extends SoftKey {
	private final static SparseArray<Integer> NUMBERS = new SparseArray<>() {{
		put(R.id.soft_key_0, 0);
		put(R.id.soft_key_1, 1);
		put(R.id.soft_key_2, 2);
		put(R.id.soft_key_3, 3);
		put(R.id.soft_key_4, 4);
		put(R.id.soft_key_5, 5);
		put(R.id.soft_key_6, 6);
		put(R.id.soft_key_7, 7);
		put(R.id.soft_key_8, 8);
		put(R.id.soft_key_9, 9);
	}};

	private final static SparseArray<Integer> UPSIDE_DOWN_NUMBERS = new SparseArray<>() {{
		put(1, 7);
		put(2, 8);
		put(3, 9);
		put(7, 1);
		put(8, 2);
		put(9, 3);
	}};

	private static final String PUNCTUATION_LABEL = ",:-)";


	public SoftKeyNumber(Context context) {
		super(context);
	}

	public SoftKeyNumber(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyNumber(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	private boolean isArabicNumber() {
		return tt9 != null && !tt9.isInputModeNumeric() && LanguageKind.isArabic(tt9.getLanguage());
	}


	@Override
	protected float getTitleRelativeSize() {
		return isArabicNumber() ? SettingsStore.SOFT_KEY_COMPLEX_LABEL_ARABIC_TITLE_RELATIVE_SIZE : SettingsStore.SOFT_KEY_COMPLEX_LABEL_TITLE_RELATIVE_SIZE;
	}


	@Override
	protected float getSubTitleRelativeSize() {
		if (tt9 != null && !tt9.isInputModeNumeric() && getNumber(getId()) == 0) {
			return 1.1f;
		}

		return super.getSubTitleRelativeSize();
	}


	@Override
	protected void handleHold() {
		preventRepeat();

		int keyCode = Key.numberToCode(getUpsideDownNumber(getId()));
		if (keyCode < 0 || !validateTT9Handler()) {
			return;
		}

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


	protected int getNumber(int keyId) {
		return NUMBERS.get(keyId, -1);
	}


	protected int getUpsideDownNumber(int keyId) {
		int number = getNumber(keyId);

		if (tt9 == null || !tt9.getSettings().getUpsideDownKeys()) {
			return number;
		}

		return UPSIDE_DOWN_NUMBERS.get(number, number);
	}


	@Override
	protected String getTitle() {
		int number = getNumber(getId());

		if (isArabicNumber() && tt9.getLanguage() != null) {
			return tt9.getLanguage().getKeyNumber(number);
		} else {
			return String.valueOf(number);
		}
	}


	@Override
	protected String getSubTitle() {
		if (tt9 == null) {
			return null;
		}

		int number = getNumber(getId());

		return switch (number) {
			case 0 -> getSpecialCharList(tt9);
			case 1 -> tt9.isNumericModeStrict() ? null : PUNCTUATION_LABEL;
			default -> getKeyCharList(tt9, number);
		};
	}


	private String getSpecialCharList(@NonNull TraditionalT9 tt9) {
		if (tt9.isNumericModeSigned()) {
			return "+/-";
		} else if (tt9.isNumericModeStrict()) {
			return null;
		} else if (tt9.isInputModeNumeric()) {
			return "+";
		} else {
			return "␣";
		}
	}


	private String getKeyCharList(@NonNull TraditionalT9 tt9, int number) {
		if (tt9.isInputModeNumeric()) {
			return null; // no special labels in 123 mode
		}

		Language language = tt9.getLanguage();
		if (language == null) {
			Logger.d("SoftKeyNumber.getLabel", "Cannot generate a label when the language is NULL.");
			return null;
		}

		ArrayList<String> chars = language.getKeyCharacters(number);
		boolean isBulgarian = LanguageKind.isBulgarian(language);
		boolean isGreek = LanguageKind.isGreek(language);
		boolean isLatinBased = LanguageKind.isLatinBased(language);
		boolean isUkrainian = LanguageKind.isUkrainian(language);
		boolean isUppercase = tt9.getTextCase() == InputMode.CASE_UPPER;

		if (
			isBulgarian
			|| isGreek
			|| isLatinBased
			|| (isUkrainian && number == 2)
			|| chars.size() < SettingsStore.SOFT_KEY_TITLE_MAX_CHARS) {
			return getDefaultCharList(chars, language.getLocale(), isGreek, isLatinBased, isUppercase);
		} else {
			return abbreviateCharList(chars, language.getLocale(), isUppercase);
		}
	}


	/**
	 * Joins the key characters into a single string, skipping accented characters
	 * when neccessary
	 */
	private String getDefaultCharList(ArrayList<String> chars, Locale locale, boolean isGreek, boolean isLatinBased, boolean isUppercase) {
		StringBuilder sb = new StringBuilder();
		for (String currentLetter : chars) {
			if (shouldSkipAccents(currentLetter.charAt(0), isGreek, isLatinBased)) {
				continue;
			}

			sb.append(
				isUppercase ? currentLetter.toUpperCase(locale) : currentLetter
			);
		}

		return sb.toString();
	}


	/**
	 * In some languages there are many characters for a single key. Naturally, they can not all fit
	 * on one key. As suggested by the community, we could display them as "A-Z".
	 * @see <a href="https://github.com/sspanak/tt9/issues/628">Issue #628</a>
	 */
	private String abbreviateCharList(ArrayList<String> chars, Locale locale, boolean isUppercase) {
		boolean containsCombiningChars = TextTools.isCombining(chars.get(0)) || TextTools.isCombining(chars.get(chars.size() - 1));
		return
			(isUppercase ? chars.get(0).toUpperCase(locale) : chars.get(0))
			+ (containsCombiningChars ? "–  " : "–")
			+ (isUppercase ? chars.get(chars.size() - 1).toUpperCase(locale) : chars.get(chars.size() - 1));
	}


	/**
	 * As suggested by the community, there is no need to display the accented letters.
	 * People are used to seeing just "ABC", "DEF", etc.
	 */
	private boolean shouldSkipAccents(char currentLetter, boolean isGreek, boolean isLatinBased) {
		return
			currentLetter == 'ѝ'
			|| currentLetter == 'ґ'
			|| currentLetter == 'ς'
			|| (isLatinBased && currentLetter > 'z')
			|| (isGreek && (currentLetter < 'α' || currentLetter > 'ω'));
	}
}
