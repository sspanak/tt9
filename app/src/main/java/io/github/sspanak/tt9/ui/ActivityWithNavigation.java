package io.github.sspanak.tt9.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.ConsumerCompat;

abstract public class ActivityWithNavigation extends AppCompatActivity {
	protected ConsumerCompat<Integer> onNumberCallback = null;
	protected SettingsStore settings;
	private int lastKeyCode = 0;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		settings = new SettingsStore(this);
	}

	@Override
	final public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!Key.isNumber(keyCode)) {
			return super.onKeyDown(keyCode, event);
		}

		if (settings != null && onNumberKey(Key.codeToNumber(settings, keyCode))) {
			lastKeyCode = keyCode;
			return true;
		} else {
			lastKeyCode = 0;
		}

		return false;
	}

	@Override
	final public boolean onKeyUp(int keyCode, KeyEvent event) {
		return lastKeyCode == keyCode || super.onKeyUp(keyCode, event);
	}

	final public SettingsStore getSettings() {
		if (settings == null) {
			settings = new SettingsStore(this);
		}

		return settings;
	}

	private boolean onNumberKey(int key) {
		if (onNumberCallback != null) {
			onNumberCallback.accept(key);
			return true;
		}

		return false;
	}
}
