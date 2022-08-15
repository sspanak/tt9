package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Arrays;

public class Punctuation {
	public static ArrayList<String> getPunctuation() {
		return new ArrayList<>(Arrays.asList(
			".", ",", "-", "?", "!", "'", "\"", "@", ":", "/", ";", "%", "1"
		));
	}


	public static ArrayList<String> getSecondaryPunctuation() {
		return new ArrayList<>(Arrays.asList(
			" ", "+", "\n", "0"
		));
	}


	public static ArrayList<String> getEmoji() {
		return new ArrayList<>(Arrays.asList(
			":)", ":D", ";)", ":(", ":*", ":P"
		));
	}
}
