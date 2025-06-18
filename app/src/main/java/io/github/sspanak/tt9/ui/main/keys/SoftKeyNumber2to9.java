package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyNumber2to9 extends SoftKeyNumber {
	public SoftKeyNumber2to9(Context context) { super(context); }
	public SoftKeyNumber2to9(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber2to9(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected String getHoldText() {
		if (tt9 == null || tt9.isInputModeNumeric()) {
			return null;
		}

		return getLocalizedNumber(getNumber(getId()));
	}


	@Override
	protected String getTitle() {
		if (tt9 != null && !tt9.isInputModeNumeric()) {
			return getKeyChars(tt9, getNumber(getId()));
		} else {
			return getLocalizedNumber(getNumber(getId()));
		}
	}


	@Override
	protected float getTitleScale() {
		return super.getTitleScale() * (isBopomofo() ? TITLE_SCALE_BOPOMOFO : 1);
	}


	private String getKeyChars(@NonNull TraditionalT9 tt9, int number) {
		Language language = tt9.getLanguage();
		if (language == null) {
			Logger.d("SoftKeyNumber.getLabel", "Cannot generate a label when the language is NULL.");
			return null;
		}

		ArrayList<String> chars = language.getKeyCharacters(number);
		boolean isArabic = LanguageKind.isArabic(language) || LanguageKind.isFarsi(language);
		boolean isGreek = LanguageKind.isGreek(language);
		boolean isLatinBased = LanguageKind.isLatinBased(language);
		boolean isUppercase = tt9.getTextCase() == InputMode.CASE_UPPER;
		int maxChars = LanguageKind.isIndic(language) ? SettingsStore.SOFT_KEY_TITLE_MAX_CHARS_INDIC : SettingsStore.SOFT_KEY_TITLE_MAX_CHARS;
		maxChars = isArabic ? maxChars * 2 : maxChars; // Arabic chars are split by ZWNJ, so we must take it into account

		String displayChars = getDefaultCharList(chars, language.getLocale(), isArabic, isGreek, isLatinBased, isUppercase);

		if (displayChars.length() > maxChars || (isArabic && (number == 2 || number == 4))) {
			String abbreviationSign = isArabic ? "…" : "–"; // prevent vertical alignment issues on some devices
			displayChars = abbreviateCharList(displayChars, abbreviationSign, language.getLocale(), isUppercase);
		}

		return displayChars.isEmpty() ? "--" : displayChars;
	}


	/**
	 * Joins the key characters into a single string, skipping accented characters
	 * when necessary
	 */
	private String getDefaultCharList(ArrayList<String> chars, Locale locale, boolean isArabic, boolean isGreek, boolean isLatinBased, boolean isUppercase) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chars.size(); i++) {
			String currentLetter = chars.get(i);

			if (shouldSkipAccents(currentLetter.charAt(0), isGreek, isLatinBased)) {
				continue;
			}

			sb.append(
				isUppercase ? currentLetter.toUpperCase(locale) : currentLetter
			);

			if (isArabic && i < chars.size() - 1) {
				sb.append(Characters.ZWNJ);
			}
		}

		return sb.toString();
	}


	/**
	 * In some languages there are many characters for a single key. Naturally, they can not all fit
	 * on one key. As suggested by the community, we could display them as "A-Z".
	 * @see <a href="https://github.com/sspanak/tt9/issues/628">Issue #628</a>
	 * Additionally, for combining characters, we want to add a dummy base, to ensure they look properly.
	 */
	private String abbreviateCharList(String chars, String abbreviationSign, Locale locale, boolean isUppercase) {
		String firstLetter = chars.substring(0, 1);
		firstLetter = TextTools.isCombining(firstLetter) ? Characters.COMBINING_BASE + firstLetter : firstLetter;

		String lastLetter = chars.substring(chars.length() - 1);
		lastLetter = TextTools.isCombining(lastLetter) ? Characters.COMBINING_BASE + lastLetter : lastLetter;

		String list = firstLetter + abbreviationSign + lastLetter;
		return isUppercase ? list.toUpperCase(locale) : list;
	}


	/**
	 * Reduces the number of displayed characters by leaving the most descriptive ones. This prevents
	 * the visual clutter on the keys.
	 */
	private boolean shouldSkipAccents(char currentLetter, boolean isGreek, boolean isLatinBased) {
		return
			// Latin. As suggested by the community, there is no need to display the accented letters. People are
			// used to seeing just "ABC", "DEF", etc.
			(isLatinBased && currentLetter > 'z')
			// Cyrillic. Same as above.
			|| currentLetter == 'ѝ' || currentLetter == 'ґ' || currentLetter == 'љ' || currentLetter == 'њ' || currentLetter == 'ћ'
			// Korean double consonants
			|| (currentLetter == 'ㄲ' || currentLetter == 'ㄸ' || currentLetter == 'ㅃ' || currentLetter == 'ㅆ' || currentLetter == 'ㅉ')
			// Greek diacritics and ending sigma
			|| currentLetter == 'ς'
			|| (isGreek && (currentLetter < 'α' || currentLetter > 'ω'))
			// Hindi combining
			|| (currentLetter >= 0x0900 && currentLetter <= 0x0903) || (currentLetter >= 0x093A && currentLetter <= 0x094F)
			|| (currentLetter >= 0x0951 && currentLetter <= 0x0957) || currentLetter == 0x0962 || currentLetter == 0x0963
			// Gujarati combining
			|| (currentLetter >= 0x0A81 && currentLetter <= 0x0A83) || (currentLetter >= 0xABC && currentLetter <= 0x0ACD)
			|| currentLetter == 0x0AE2 || currentLetter == 0x0AE3
		;
	}
}
