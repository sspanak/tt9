package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;


public class SettingsStore extends SettingsUI {
	public SettingsStore(Context context) { super(context); }

		/************* internal settings *************/
	public final static int DELETE_WORDS_SEARCH_DELAY = 500; // ms
	public final static int DICTIONARY_AUTO_LOAD_COOLDOWN_TIME = 1200000; // 20 minutes in ms
	public final static int DICTIONARY_DOWNLOAD_CONNECTION_TIMEOUT = 10000; // ms
	public final static int DICTIONARY_DOWNLOAD_READ_TIMEOUT = 10000; // ms
	public final static int DICTIONARY_IMPORT_BATCH_SIZE = 5000; // words
	public final static int DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME = 250; // ms
	public final static int RESIZE_THROTTLING_TIME = 60; // ms
	public final static byte SLOW_QUERY_TIME = 50; // ms
	public final static int SOFT_KEY_DOUBLE_CLICK_DELAY = 500; // ms
	public final static int SOFT_KEY_REPEAT_DELAY = 40; // ms
	public final static int SOFT_KEY_TITLE_SIZE = 18; // sp
	public final static float SOFT_KEY_COMPLEX_LABEL_TITLE_RELATIVE_SIZE = 0.55f;
	public final static float SOFT_KEY_COMPLEX_LABEL_ARABIC_TITLE_RELATIVE_SIZE = 0.72f;
	public final static float SOFT_KEY_COMPLEX_LABEL_SUB_TITLE_RELATIVE_SIZE = 0.8f;
	public final static int SUGGESTIONS_MAX = 20;
	public final static int SUGGESTIONS_MIN = 8;
	public final static int SUGGESTIONS_SELECT_ANIMATION_DURATION = 66;
	public final static int SUGGESTIONS_TRANSLATE_ANIMATION_DURATION = 0;
	public final static int WORD_FREQUENCY_MAX = 25500;
	public final static int WORD_FREQUENCY_NORMALIZATION_DIVIDER = 100; // normalized frequency = WORD_FREQUENCY_MAX / WORD_FREQUENCY_NORMALIZATION_DIVIDER
	public final static int WORD_NORMALIZATION_DELAY = 15000; // ms

	/************* hacks *************/
	public final static int PREFERENCES_CLICK_DEBOUNCE_TIME = 250; // ms
}
