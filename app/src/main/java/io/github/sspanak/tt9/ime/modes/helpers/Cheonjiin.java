package io.github.sspanak.tt9.ime.modes.helpers;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

public class Cheonjiin {
	private static final Pattern MEDIAL_VOWEL = Pattern.compile("[1-3]+[4-9|0]+$");

	public static boolean isThereMediaVowel(@NonNull String digitSequence) {
		return !digitSequence.isEmpty() && MEDIAL_VOWEL.matcher(digitSequence).find();
	}

	public static boolean isVowelDigit(int digit) {
		return digit == 1 || digit == 2 || digit == 3;
	}

	public static boolean endsWithConsonant(@NonNull String digitSequence, int consonant) {
		return !digitSequence.isEmpty() && digitSequence.charAt(digitSequence.length() - 1) - '0' == consonant;
	}

	public static boolean endsWithDashVowel(@NonNull String digitSequence) {
		int lastDigit = digitSequence.isEmpty() ? -1 : digitSequence.charAt(digitSequence.length() - 1) - '0';
		return lastDigit == 1 || lastDigit == 3;
	}

	public static int getRepeatingEndingDigits(@NonNull String digitSequence) {
		int count = 0;
		for (int i = digitSequence.length() - 1; i >= 0; i--) {
			if (digitSequence.charAt(i) == digitSequence.charAt(digitSequence.length() - 1)) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	public static String stripRepeatingEndingDigits(@NonNull String digitSequence) {
		int end = digitSequence.length() - getRepeatingEndingDigits(digitSequence);
		return digitSequence.length() > 1 ? digitSequence.substring(0, end) : digitSequence;
	}
}
