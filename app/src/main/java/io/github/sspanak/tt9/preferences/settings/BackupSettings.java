package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.github.sspanak.tt9.preferences.screens.appearance.DropDownBottomPaddingPortrait;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownLayoutType;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownNumpadKeyFontSize;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownSuggestionFontSize;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownWidth;
import io.github.sspanak.tt9.preferences.screens.appearance.ItemAlternativeSuggestionScrolling;
import io.github.sspanak.tt9.preferences.screens.appearance.ItemClearInsets;
import io.github.sspanak.tt9.preferences.screens.appearance.ItemStatusIcon;
import io.github.sspanak.tt9.preferences.screens.appearance.ItemSuggestionSmoothScroll;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownAlignmentUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownBottomPaddingPortraitUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownKeyHeightUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownNumpadFnKeyScaleUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownNumpadKeyFontSizeUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownSuggestionFontSizeUnfolded;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.DropDownWidthUnfolded;
import io.github.sspanak.tt9.preferences.screens.debug.ItemDemoMode;
import io.github.sspanak.tt9.preferences.screens.keychars.DropDownPunctuationOrderLanguage;
import io.github.sspanak.tt9.preferences.screens.keypad.DropDownKeyPadDebounceTime;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;


public class BackupSettings extends BaseSettings {
	private static final String BACKUP_DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.VERSION.SDK_INT + ")";
	private static final String BACKUP_DEVICE_NAME_KEY = "targetDevice";
	private static final String BACKUP_DEVICE_ONLY_PREFIX = "dev_";

	BackupSettings(Context context) {
		super(context);
	}


	public String exportAll() throws JSONException {
		Timer.start(LOG_TAG);
		final JSONObject json = new JSONObject();

		try {
			json.put(BACKUP_DEVICE_NAME_KEY, BACKUP_DEVICE_NAME);
		} catch (JSONException ex) {
			Logger.w(LOG_TAG, "Failed saving device name to settings export. " + ex);
		}

		for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
			final String key = e.getKey();

			if (isBlacklistedForBackup(key)) {
				Logger.d(LOG_TAG, "Not exporting blacklisted setting '" + key + "'");
				continue;
			}


			final String jsonKey = isDeviceOnlyForBackup(key) ? BACKUP_DEVICE_ONLY_PREFIX + key : key;
			final Object value = e.getValue();
			final JSONObject entry = new JSONObject();

			try {
				if (value instanceof Boolean) {
					entry.put("t", "boolean").put("v", value);
				} else if (value instanceof Integer) {
					entry.put("t", "int").put("v", value);
				} else if (value instanceof Long) {
					entry.put("t", "long").put("v", value);
				} else if (value instanceof Float) {
					entry.put("t", "float").put("v", value);
				} else if (value instanceof String) {
					entry.put("t", "string").put("v", value);
				} else if (value instanceof Set) {
					entry.put("t", "set").put("v", new JSONArray((Set<?>) value));
				} else {
					continue; // unknown type, skip
				}

				json.put(jsonKey, entry);
				Logger.v(LOG_TAG, "Exported setting '" + key + "' = '" + value + "' as '" + jsonKey + "'");
			} catch (JSONException ex) {
				Logger.w(LOG_TAG, "Failed exporting setting '" + key + "' = '" + value + "' as '" + jsonKey + "'. " + ex);
			}
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


	protected boolean isDeviceOnlyForBackup(String key) {
		return
			key.equals("hack_messenger_reply_extra_padding")
			|| key.equals(DropDownAlignmentUnfolded.NAME)
			|| key.equals(DropDownBottomPaddingPortraitUnfolded.NAME)
			|| key.equals(DropDownKeyHeightUnfolded.NAME)
			|| key.equals(DropDownNumpadFnKeyScaleUnfolded.NAME)
			|| key.equals(DropDownNumpadKeyFontSizeUnfolded.NAME)
			|| key.equals(DropDownSuggestionFontSizeUnfolded.NAME)
			|| key.equals(DropDownWidthUnfolded.NAME)
			|| key.equals(ItemAlternativeSuggestionScrolling.NAME)
			|| key.equals(DropDownBottomPaddingPortrait.NAME)
			|| key.equals(ItemClearInsets.NAME)
			|| key.equals("pref_hardware_key_visual_feedback")
			|| key.equals(DropDownKeyPadDebounceTime.NAME)
			|| key.equals(DropDownLayoutType.NAME)
			|| key.equals(DropDownNumpadKeyFontSize.NAME)
			|| key.equals(DropDownWidth.NAME)
			|| key.equals(ItemStatusIcon.NAME)
			|| key.equals(DropDownSuggestionFontSize.NAME)
			|| key.equals(ItemSuggestionSmoothScroll.NAME)
			|| key.startsWith(SettingsHotkeys.HOTKEY_VERSION_PREFIX)
			|| key.startsWith("key_");
	}
}
