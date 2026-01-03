package io.github.sspanak.tt9.preferences.screens.hotkeys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdBackspace;
import io.github.sspanak.tt9.commands.CmdVoiceInput;
import io.github.sspanak.tt9.commands.Command;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.Logger;

public class HotkeysScreen extends BaseScreenFragment {
	public static final String NAME = "Hotkeys";
	@NonNull static final HashMap<String, PreferenceHotkey> hotkeys = new HashMap<>();


	public HotkeysScreen() { super(); }
	public HotkeysScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_function_keys; }
	@Override protected int getXml() { return R.xml.prefs_screen_hotkeys; }

	@Override
	public void onCreate() {
		createOptions();
		if (activity != null) {
			(new ItemResetKeys(findPreference(ItemResetKeys.NAME), activity, hotkeys.values())).enableClickHandler();
		}
		resetFontSize(true);
	}

	@Override
	public int getPreferenceCount() {
		return -1; // prevent scrolling and item selection using the number keys on this screen
	}

	private void createOptions() {
		if (activity == null) {
			Logger.w(NAME, "Cannot create hotkey preferences: activity is null");
			return;
		}

		PreferenceCategory category = findPreference("category_hotkeys");
		if (category == null) {
			Logger.w(NAME, "Cannot append hotkey preferences to a NULL category");
			return;
		}

		for (Command cmd : CommandCollection.getHotkeyCommands()) {
			Preference old = cmd.getId() != null ? findPreference(cmd.getId()) : null;
			if (old instanceof PreferenceHotkey) {
				hotkeys.put(cmd.getId(), (PreferenceHotkey) old);
				continue;
			}

			PreferenceHotkey hotkeyItem = switch (cmd.getId()) {
				case CmdBackspace.ID -> new PreferenceBackspaceHotkey(activity, activity.getSettings(), cmd);
				case CmdVoiceInput.ID -> new PreferenceVoiceInputHotkey(activity, activity.getSettings(), cmd);
				default -> new PreferenceHotkey(activity, activity.getSettings(), cmd);
			};

			category.addPreference(hotkeyItem);
			hotkeys.put(cmd.getId(), hotkeyItem);
		}
	}
}
