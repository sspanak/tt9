package io.github.sspanak.tt9.preferences.screens.hotkeys;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.commands.Command;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class PreferenceVoiceInputHotkey extends PreferenceHotkey {
	public PreferenceVoiceInputHotkey(@NonNull Context context, @NonNull SettingsStore settings, @NonNull Command command) {
		super(context, settings, command);
	}

	@Override
	public void populate() {
		boolean isAvailable = new VoiceInputOps(getContext(), null, null, null, null).isAvailable();
		setVisible(isAvailable);
		if (isAvailable) {
			super.populate();
		}
	}
}
