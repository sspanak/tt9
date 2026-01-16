package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

public class SettingsStatic extends SettingsColors {
	protected SettingsStatic(Context context) { super(context); }

	/************* internal settings *************/
	public static final int BACKSPACE_ACCELERATION_MAX_CHARS = 20; // maximum chars to be deleted at once in very long words
	public static final int BACKSPACE_ACCELERATION_MAX_CHARS_NO_SPACE = 4; // maximum chars to be deleted at once for languages with no spaces
	public static final int BACKSPACE_ACCELERATION_REPEAT_DEBOUNCE = 5;
	public final static int CLIPBOARD_PREVIEW_LENGTH = 20;
	public final static int CUSTOM_WORDS_IMPORT_MAX_LINES = 250;
	public final static int CUSTOM_WORDS_MAX = 1000;
	public final static int CUSTOM_WORDS_SEARCH_RESULTS_MAX = 50;
	public final static int DICTIONARY_AUTO_LOAD_COOLDOWN_TIME = 1200000; // 20 minutes in ms
	public final static int DICTIONARY_DOWNLOAD_CONNECTION_TIMEOUT = 10000; // ms
	public final static int DICTIONARY_DOWNLOAD_READ_TIMEOUT = 10000; // ms
	public final static int DICTIONARY_IMPORT_BATCH_SIZE = 5000; // words
	public final static int DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME = 250; // ms
	public final static long INPUT_CONNECTION_MAX_WAIT = 50; // ms
	public final static int LANGUAGE_SEARCH_DEBOUNCE_TIME = 500; // ms
	public final static int RESIZE_THROTTLING_TIME = 60; // ms
	public final static int SHIFT_STATE_DEBOUNCE_TIME = 175; // ms
	public final static byte SLOW_QUERY_TIME = 50; // ms
	public final static int SLOW_QUERY_TIMEOUT = 3000; // ms
	public final static float SOFT_KEY_AMOUNT_OF_KEY_SIZE_FOR_SWIPE = 0.5f; // 1 = full key size
	public final static int SOFT_KEY_DOUBLE_CLICK_DELAY = 500; // ms
	public final static int SOFT_KEY_REPEAT_DELAY = 40; // ms
	public static final String SOFT_KEY_TEXT_LEFT_DEFAULT = "!";
	public static final String SOFT_KEY_TEXT_RIGHT_DEFAULT = "?";
	public final static float SOFT_KEY_SCALE_SCREEN_COMPENSATION_NORMAL_SIZE = 360; // dp
	public final static float SOFT_KEY_SCALE_SCREEN_COMPENSATION_MAX = 1.4f;
	public final static int SOFT_KEY_TITLE_MAX_CHARS = 5;
	public final static int SOFT_KEY_TITLE_MAX_CHARS_INDIC = 3;
	public final static float SOFT_KEY_V_SHAPE_RATIO_INNER = 1.1f;
	public final static float SOFT_KEY_V_SHAPE_RATIO_OUTER = (float) Math.pow(SOFT_KEY_V_SHAPE_RATIO_INNER, 2);
	public final static float SOFT_KEY_V_SHAPE_RATIO_CLASSIC = (SOFT_KEY_V_SHAPE_RATIO_OUTER + SOFT_KEY_V_SHAPE_RATIO_INNER) * 0.49f;
	public final static int SUGGESTIONS_MAX = 20;
	public final static int SUGGESTIONS_MIN = 8;
	public final static int SUGGESTIONS_SELECT_ANIMATION_DURATION = 66;
	public final static int SUGGESTIONS_TRANSLATE_ANIMATION_DURATION = 0;
	public final static int WORD_BACKGROUND_TASKS_DELAY = 15000; // ms
	public final static int WORD_FREQUENCY_MAX = 25500;
	public final static int WORD_FREQUENCY_NORMALIZATION_DIVIDER = 100; // normalized frequency = WORD_FREQUENCY_MAX / WORD_FREQUENCY_NORMALIZATION_DIVIDER
	public final static int WORD_PAIR_MAX = 1250;
	public final static int WORD_PAIR_MAX_WORD_LENGTH = 6;
	public final static int ZOMBIE_CHECK_INTERVAL = 5000; // ms
	public final static int ZOMBIE_CHECK_MAX = 2;
	public final static int ZOMBIE_HEARTBEAT_INTERVAL = 2000; // ms

	/************* hacks *************/
	public final static int PREFERENCES_CLICK_DEBOUNCE_TIME = 250; // ms
	public final static int VOICE_INPUT_START_FAILURE_TIMEOUT = 5000; // ms
}
