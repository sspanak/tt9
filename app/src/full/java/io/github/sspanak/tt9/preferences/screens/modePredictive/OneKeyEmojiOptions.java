package io.github.sspanak.tt9.preferences.screens.modePredictive;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class OneKeyEmojiOptions {
	public enum OPTIONS { NONE, SIMPLE }
	public static final String DEFAULT = OPTIONS.SIMPLE.toString();
	public static final LinkedHashMap<OPTIONS, Integer> OPTION_TITLES = new LinkedHashMap<>() {{
		put(OPTIONS.NONE, R.string.pref_one_key_emoji_none);
		put(OPTIONS.SIMPLE, R.string.pref_one_key_emoji_simple);
	}};

	public static ArrayList<OPTIONS> getAll(@NonNull SettingsStore ignored) {
		ArrayList<OPTIONS> options = new ArrayList<>();
		Collections.addAll(options, OPTIONS.values());
		return options;
	}
}
