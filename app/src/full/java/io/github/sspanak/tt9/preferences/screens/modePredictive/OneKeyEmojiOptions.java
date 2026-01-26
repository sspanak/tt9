package io.github.sspanak.tt9.preferences.screens.modePredictive;

import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;

public class OneKeyEmojiOptions {
	public enum OPTIONS { NONE, SIMPLE }
	public static final String DEFAULT = OPTIONS.SIMPLE.toString();
	public static final LinkedHashMap<OPTIONS, Integer> OPTION_TITLES = new LinkedHashMap<>() {{
		put(OPTIONS.NONE, R.string.pref_one_key_emoji_none);
		put(OPTIONS.SIMPLE, R.string.pref_one_key_emoji_simple);
	}};
}
