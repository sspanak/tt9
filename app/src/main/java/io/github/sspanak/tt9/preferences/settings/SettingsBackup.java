package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownAlignment;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownBottomPaddingPortrait;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownColorScheme;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownKeyHeight;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownLayoutType;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadFnKeyScale;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadKeyFontSize;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadShape;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownSettingsFontSize;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownSuggestionFontSize;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownWidth;
import io.github.sspanak.tt9.preferences.screens.appearance.SwitchDoubleTapResize;
import io.github.sspanak.tt9.preferences.screens.appearance.SwitchDragResize;
import io.github.sspanak.tt9.preferences.screens.appearance.SwitchKeyShadows;
import io.github.sspanak.tt9.preferences.screens.appearance.SwitchLeftRightArrows;
import io.github.sspanak.tt9.preferences.screens.appearance.SwitchShowArrowsUpDown;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownAlignmentUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownBottomPaddingPortraitUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownKeyHeightUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownNumpadFnKeyScaleUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownNumpadKeyFontSizeUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownSuggestionFontSizeUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownWidthUnfolded;
import io.github.sspanak.tt9.preferences.screens.debug.ItemDemoMode;
import io.github.sspanak.tt9.preferences.screens.keychars.DropDownPunctuationOrderLanguage;
import io.github.sspanak.tt9.preferences.screens.keychars.PreferenceChars2to9;
import io.github.sspanak.tt9.preferences.screens.keypad.DropDownKeyPadDebounceTime;
import io.github.sspanak.tt9.preferences.screens.keypad.DropDownOneKeyEmoji;
import io.github.sspanak.tt9.preferences.screens.keypad.SwitchAllowSystemTapSound;
import io.github.sspanak.tt9.preferences.screens.keypad.SwitchHapticFeedback;
import io.github.sspanak.tt9.preferences.screens.keypad.SwitchUpsideDownKeys;
import io.github.sspanak.tt9.preferences.screens.languages.SwitchAddWordsWithoutConfirmation;
import io.github.sspanak.tt9.preferences.screens.languages.SwitchRaiseImportLimits;
import io.github.sspanak.tt9.preferences.screens.modeAbc.DropDownAbcAutoAcceptTime;
import io.github.sspanak.tt9.preferences.screens.modePredictive.DropDownPredictiveAutoAcceptTime;
import io.github.sspanak.tt9.preferences.screens.modePredictive.DropDownZeroKeyCharacter;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;


public class SettingsBackup extends SettingsStatic {
	private static final String BACKUP_DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.VERSION.SDK_INT + ")";
	private static final String BACKUP_DEVICE_NAME_KEY = "targetDevice";
	private static final String BACKUP_DEVICE_ONLY_PREFIX = "dev_";

	protected SettingsBackup(Context context) {
		super(context);
	}


	protected Map<String, JSONObject> getExportableKeys() throws JSONException {
		HashMap<String, JSONObject> keys = new HashMap<>() {{
			put(SwitchAddWordsWithoutConfirmation.NAME, new JSONObject().put("t", "boolean").put("v", getAddWordsNoConfirmation()));
			put(SwitchRaiseImportLimits.NAME, new JSONObject().put("t", "boolean").put("v", getRaiseImportLimits()));
			put(DropDownColorScheme.NAME, new JSONObject().put("t", "string").put("v", getColorSchemeId()));
			put("pref_hold_to_type", new JSONObject().put("t", "boolean").put("v", getHoldToType()));
			put("pref_allow_composing_text", new JSONObject().put("t", "boolean").put("v", getAllowComposingText()));
			put("hack_auto_disable_composing", new JSONObject().put("t", "boolean").put("v", getAutoDisableComposing()));
			put("hack_send_with_enter_in_chat_apps", new JSONObject().put("t", "boolean").put("v", getSendWithEnterInChatApps()));
			put("pref_languages", new JSONObject().put("t", "set").put("v", new JSONArray((Set<?>) prefs.getStringSet("pref_languages", null)))); // double-check this
			put("pref_input_language", new JSONObject().put("t", "int").put("v", getInputLanguage()));
			put("pref_input_mode", new JSONObject().put("t", "int").put("v", getInputMode()));
			put("pref_text_case", new JSONObject().put("t", "int").put("v", getTextCase()));
			put("auto_mind_reading_guess", new JSONObject().put("t", "boolean").put("v", getMindReadingGuess()));
			put("auto_mind_reading_complete", new JSONObject().put("t", "boolean").put("v", getMindReadingComplete()));
			put("auto_mind_reading_sort_predictions_last", new JSONObject().put("t", "boolean").put("v", getMindReadingSortPredictionsLast()));
			put("auto_mind_reading_in_all_fields", new JSONObject().put("t", "boolean").put("v", getMindReadingInAllFields()));
			put(SwitchUpsideDownKeys.NAME, new JSONObject().put("t", "boolean").put("v", getUpsideDownKeys()));
			put("show_suggestions_abc", new JSONObject().put("t", "boolean").put("v", getShowSuggestions()));
			put("pref_predict_word_pairs", new JSONObject().put("t", "boolean").put("v", getPredictWordPairs()));
			put("pref_predict_top_words", new JSONObject().put("t", "boolean").put("v", getPredictTopWords()));
			put("pref_predictive_mode", new JSONObject().put("t", "boolean").put("v", getPredictiveMode()));
			put(DropDownOneKeyEmoji.NAME, new JSONObject().put("t", "string").put("v", getOneKeyEmojiMode().toString())); // double-check this
			put(DropDownZeroKeyCharacter.NAME, new JSONObject().put("t", "string").put("v", getDoubleZeroChar()));
			put("backspace_recomposing", new JSONObject().put("t", "boolean").put("v", getBackspaceRecomposing()));
			put("backspace_acceleration", new JSONObject().put("t", "boolean").put("v", getBackspaceAcceleration()));
			put("auto_trim_trailing_space", new JSONObject().put("t", "boolean").put("v", getAutoTrimTrailingSpace()));
			put("auto_capitals_after_newline_predictive", new JSONObject().put("t", "boolean").put("v", getAutoCapitalsAfterNewlinePredictive()));
			put("auto_text_case_predictive", new JSONObject().put("t", "boolean").put("v", getAutoTextCasePredictive()));
			put("auto_space_predictive", new JSONObject().put("t", "boolean").put("v", getAutoSpacePredictive()));
			put(DropDownPredictiveAutoAcceptTime.NAME, new JSONObject().put("t", "string").put("v", String.valueOf(getStringifiedInt(DropDownPredictiveAutoAcceptTime.NAME, DropDownPredictiveAutoAcceptTime.DEFAULT))));
			put("auto_capitals_after_newline_abc", new JSONObject().put("t", "boolean").put("v", getAutoCapitalsAfterNewlineAbc()));
			put("auto_text_case_abc_v2", new JSONObject().put("t", "boolean").put("v", getAutoTextCaseAbc()));
			put("auto_space_abc_v2", new JSONObject().put("t", "boolean").put("v", getAutoSpaceAbc()));
			put(DropDownAbcAutoAcceptTime.NAME, new JSONObject().put("t", "string").put("v", String.valueOf(getStringifiedInt(DropDownAbcAutoAcceptTime.NAME, DropDownAbcAutoAcceptTime.DEFAULT))));
			put(SwitchDragResize.NAME, new JSONObject().put("t", "boolean").put("v", getDragResize()));
			put(SwitchDoubleTapResize.NAME, new JSONObject().put("t", "boolean").put("v", getDoubleTapResize()));
			put(SwitchHapticFeedback.NAME, new JSONObject().put("t", "boolean").put("v", getHapticFeedback()));
			put("pref_quick_switch_language", new JSONObject().put("t", "boolean").put("v", getQuickSwitchLanguage()));
			put(SwitchKeyShadows.NAME, new JSONObject().put("t", "boolean").put("v", getKeyShadows()));
			put("pref_status_icon", new JSONObject().put("t", "boolean").put("v", isStatusIconEnabled()));
			put(SwitchAllowSystemTapSound.NAME, new JSONObject().put("t", "boolean").put("v", getAllowSystemTapSound()));
			put(SwitchLeftRightArrows.NAME, new JSONObject().put("t", "boolean").put("v", getArrowsLeftRight()));
			put(SwitchShowArrowsUpDown.NAME, new JSONObject().put("t", "boolean").put("v", getArrowsUpDown()));
			put("pref_lfn_key_order", new JSONObject().put("t", "string").put("v", getLfnKeyOrder()));
			put("pref_rfn_key_order", new JSONObject().put("t", "string").put("v", getRfnKeyOrder()));
			put(DropDownNumpadShape.NAME, new JSONObject().put("t", "string").put("v", String.valueOf(getNumpadShape())));
			put("pref_tutorial_classic_seen", new JSONObject().put("t", "boolean").put("v", prefs.getBoolean("pref_tutorial_classic_seen", false)));
			put("pref_tutorial_fn_keys_seen", new JSONObject().put("t", "boolean").put("v", prefs.getBoolean("pref_tutorial_fn_keys_seen", false)));

			// device-only`
put(BACKUP_DEVICE_ONLY_PREFIX + HOTKEY_VERSION, new JSONObject().put("t", "boolean").put("v", !areHotkeysInitialized()));
			put(BACKUP_DEVICE_ONLY_PREFIX + DropDownSettingsFontSize.NAME, new JSONObject().put("t", "string").put("v", String.valueOf(getSettingsFontSize())));
			put(BACKUP_DEVICE_ONLY_PREFIX + "pref_suggestion_smooth_scroll", new JSONObject().put("t", "boolean").put("v", getSuggestionSmoothScroll()));
			put(BACKUP_DEVICE_ONLY_PREFIX + "pref_alternative_suggestion_scrolling", new JSONObject().put("t", "boolean").put("v", getSuggestionScrollingDelay() > 0));
			put(BACKUP_DEVICE_ONLY_PREFIX + "pref_clear_insets", new JSONObject().put("t", "boolean").put("v", clearInsets()));
			put(BACKUP_DEVICE_ONLY_PREFIX + DropDownLayoutType.NAME, new JSONObject().put("t", "string").put("v", String.valueOf(getMainViewLayout())));
			put(BACKUP_DEVICE_ONLY_PREFIX + "pref_preferred_large_layout", new JSONObject().put("t", "int").put("v", getPreferredLargeLayout()));
			put(BACKUP_DEVICE_ONLY_PREFIX + "pref_hardware_key_visual_feedback", new JSONObject().put("t", "boolean").put("v", getHardwareKeyVisualFeedback()));
			put(BACKUP_DEVICE_ONLY_PREFIX + DropDownKeyPadDebounceTime.NAME, new JSONObject().put("t", "string").put("v", String.valueOf(getKeyPadDebounceTime())));
		}};

		Set<String> exactKeysDevice = new HashSet<>() {
			{
				add(DropDownBottomPaddingPortrait.NAME);
				add(DropDownBottomPaddingPortraitUnfolded.NAME);
				add(DropDownAlignment.NAME);
				add(DropDownAlignmentUnfolded.NAME);
				add(DropDownSuggestionFontSize.NAME);
				add(DropDownSuggestionFontSizeUnfolded.NAME);
				add(DropDownWidth.NAME);
				add(DropDownWidthUnfolded.NAME);
				add(DropDownKeyHeight.NAME);
				add(DropDownKeyHeightUnfolded.NAME);
				add(DropDownNumpadFnKeyScale.NAME);
				add(DropDownNumpadFnKeyScaleUnfolded.NAME);
				add(DropDownNumpadKeyFontSize.NAME);
				add(DropDownNumpadKeyFontSizeUnfolded.NAME);
			}
		};

		// wildcard keys
		for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
			final Object value = e.getValue();
			final String key = e.getKey();

			String type;
			if (value instanceof Boolean) {
				type = "boolean";
			} else if (value instanceof Integer) {
				type = "int";
			} else if (value instanceof Long) {
				type = "long";
			} else if (value instanceof Float) {
				type = "float";
			} else if (value instanceof String) {
				type = "string";
			} else if (value instanceof Set) {
				type = "set";
			} else {
				Logger.w(LOG_TAG, "Not exporting property '" + key + "' = '" + value + "'. Unrecognized property type.");
				continue; // unknown type, skip
			}

			// hotkeys: "key_", "cmd_"
			if (key.startsWith("key_") || key.startsWith("cmd_") || exactKeysDevice.contains(key)) {
				final JSONObject entry = new JSONObject();
				entry.put("t", type).put("v", value);
				keys.put(BACKUP_DEVICE_ONLY_PREFIX + key, entry);
			}

			// extra key characters / punctuation order
			if (key.startsWith("punctuation_order_") || key.startsWith(PreferenceChars2to9.NAME_PREFIX)) {
				final JSONObject entry = new JSONObject();
				entry.put("t", type).put("v", value);
				keys.put(key, entry);
			}
		}

		return keys;
	}


	public String exportAll() throws JSONException {
		Timer.start(LOG_TAG);
		final JSONObject json = new JSONObject();

		try {
			json.put(BACKUP_DEVICE_NAME_KEY, BACKUP_DEVICE_NAME);
		} catch (JSONException ex) {
			Logger.w(LOG_TAG, "Failed saving device name to settings export. " + ex);
		}

		for (Map.Entry<String, JSONObject> entry : getExportableKeys().entrySet()) {
			json.put(entry.getKey(), entry.getValue());
		}

		final String out = json.toString(2);
		Logger.d(LOG_TAG, "Settings export finished in: " + Timer.stop(LOG_TAG) + " ms");

		return out;
	}


	public void importAll(String jsonString) throws JSONException {
		JSONObject json = new JSONObject(jsonString);

		final Iterator<String> keys = json.keys();
		boolean isSameDevice = false;

		while (keys.hasNext()) {
			final String key = keys.next();
			if (isBlacklistedForBackup(key)) {
				Logger.d(LOG_TAG, "Not importing blacklisted setting '" + key + "'");
				continue;
			}

			if (BACKUP_DEVICE_NAME_KEY.equals(key)) {
				try {
					isSameDevice = BACKUP_DEVICE_NAME.equals(json.getString(key));
				} catch (JSONException ex) {
					Logger.e(LOG_TAG, "Failed getting device name from JSON. " + ex);
				}
				continue;
			}

			final boolean isDeviceOnly = key.startsWith(BACKUP_DEVICE_ONLY_PREFIX);
			if (isDeviceOnly && !isSameDevice) {
				continue;
			}
			final String preferenceKey = isDeviceOnly ? key.substring(BACKUP_DEVICE_ONLY_PREFIX.length()) : key;

			try {
				final JSONObject entry = json.getJSONObject(key);
				final String type = entry.getString("t");

				switch (type) {
					case "boolean":
						getPrefsEditor().putBoolean(preferenceKey, entry.getBoolean("v"));
						Logger.v(LOG_TAG, "Imported setting '" + preferenceKey + "'");
						break;
					case "int":
						getPrefsEditor().putInt(preferenceKey, entry.getInt("v"));
						Logger.v(LOG_TAG, "Imported setting '" + preferenceKey + "'");
						break;
					case "long":
						getPrefsEditor().putLong(preferenceKey, entry.getLong("v"));
						Logger.v(LOG_TAG, "Imported setting '" + preferenceKey + "'");
						break;
					case "float":
						getPrefsEditor().putFloat(preferenceKey, (float) entry.getDouble("v"));
						Logger.v(LOG_TAG, "Imported setting '" + preferenceKey + "'");
						break;
					case "string":
						getPrefsEditor().putString(preferenceKey, entry.getString("v"));
						Logger.v(LOG_TAG, "Imported setting '" + preferenceKey + "'");
						break;
					case "set":
						final JSONArray array = entry.getJSONArray("v");
						final Set<String> set = new HashSet<>();
						for (int i = 0; i < array.length(); i++) {
							set.add(array.getString(i));
						}
						getPrefsEditor().putStringSet(preferenceKey, set);
						Logger.v(LOG_TAG, "Imported setting '" + preferenceKey + "'");
						break;
					default:
						Logger.w(LOG_TAG, "Unknown setting type '" + type + "' for key '" + key + "'");
				}
			} catch (JSONException ex) {
				Logger.e(LOG_TAG, "Failed importing setting '" + key + "' as '" + preferenceKey + "'. " + ex);
			}
		}

		getPrefsEditor().apply();
	}


	protected boolean isBlacklistedForBackup(String key) {
		return
			key.startsWith("mind_reader_factory_ngrams_revision_")
				|| key.equals("pref_enable_system_logs")
				|| key.equals(ItemDemoMode.NAME)
				|| key.equals(DropDownPunctuationOrderLanguage.NAME)
				|| key.equals(FIRST_INSTALL_VERSION_KEY);
	}
}
