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

	public static String stripEndingConsonantDigits(@NonNull String digitSequence) {
		return digitSequence.replaceAll("[4-9|0]+$", "");
	}
}
