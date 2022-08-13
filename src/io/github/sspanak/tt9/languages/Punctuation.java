package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Arrays;

public class Punctuation {
	public static ArrayList<String> getPunctuation() {
		ArrayList<String> punctuation = new ArrayList<String>(Arrays.asList(
			".", ",", "-", "?", "!", "'", "\"", "@", ":", "/", ";", "%", "1"
		));

		return punctuation;
	}


	public static ArrayList<String> getEmoji() {
		ArrayList<String> emoji = new ArrayList<String>(Arrays.asList(
			":)", ":D", ";)", ":(", ":*", ":P"
		));

		return emoji;
	}
}
