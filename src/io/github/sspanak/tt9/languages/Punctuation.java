package io.github.sspanak.tt9.languages;

import java.util.ArrayList;
import java.util.Arrays;

public class Punctuation {
	final public static ArrayList<String> Main = new ArrayList<>(Arrays.asList(
		".", ",", "-", "(", ")", "[", "]", "&", "\"", ":", ";", "'", "!", "?"
	));

	final public static ArrayList<String> Secondary = new ArrayList<>(Arrays.asList(
		" ", "\n", "@", "%", "#", "{", "}", "~", "`", "<", ">", "\\", "/", "=", "*", "+"
	));

	final public static ArrayList<String> Faces = new ArrayList<>(Arrays.asList(
		"🙂", "😀", "🤣", "😉", "😛", "😳", "😲", "😱", "😭", "😢", "🙁"
	));

	final public static ArrayList<String> Hands = new ArrayList<>(Arrays.asList(
		"👍", "👋", "✌️", "👏", "🤝", "💪", "🤘", "🖖", "👎"
	));

	final public static ArrayList<String> Emotions = new ArrayList<>(Arrays.asList(
		"❤", "🤗", "😍", "😘", "😈", "🎉", "🤓", "😎", "🥶", "😬"
	));
}
