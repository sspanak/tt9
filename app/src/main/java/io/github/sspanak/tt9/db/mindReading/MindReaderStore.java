package io.github.sspanak.tt9.db.mindReading;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.db.BaseSyncStore;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class MindReaderStore extends BaseSyncStore {
	@NonNull private final SettingsStore settings;
	@NonNull private final MindReaderContext wordContext;

	public MindReaderStore(@NonNull Context context, @NonNull SettingsStore settings) {
		super(context);
		this.settings = settings;
		wordContext = new MindReaderContext();
	}

	public boolean setContext(@Nullable String beforeCursor) {
		return isOn() && wordContext.set(beforeCursor);
	}

	private boolean isOn() {
		return settings.getAutoMindReading() && !settings.isMainLayoutStealth();
	}
}
