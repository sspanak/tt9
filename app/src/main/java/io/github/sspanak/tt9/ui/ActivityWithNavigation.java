package io.github.sspanak.tt9.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;

import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

abstract public class ActivityWithNavigation extends EdgeToEdgeActivity {
	public static final String LOG_TAG = ActivityWithNavigation.class.getSimpleName();

	protected SettingsStore settings;
	protected Callable<Integer> getOptionsCount;
	private int lastKey = KeyEvent.KEYCODE_UNKNOWN;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSettings();
	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
		getSettings();
	}


	@Override
	final public boolean onKeyDown(int keyCode, KeyEvent event) {
		// ignore our own key events
		if (event.getSource() == InputDevice.SOURCE_UNKNOWN) {
			return super.onKeyDown(keyCode, event);
		}

		// Reset the last key even if we are not going to process it. This is to avoid
		// detecting a double click, when the user has pressed a different key in between.
		boolean doubleClick = (keyCode == lastKey);
		lastKey = keyCode;

		if (!Key.isNumber(keyCode)) {
			return super.onKeyDown(keyCode, event);
		}

		selectOption(Key.codeToNumber(settings, keyCode), doubleClick);

		if (doubleClick) {
			resetKeyRepeat();
		}

		return true;
	}


	@Override
	final public boolean onKeyUp(int keyCode, KeyEvent event) {
		return Key.isNumber(keyCode) || super.onKeyUp(keyCode, event);
	}


	@NonNull
	final public SettingsStore getSettings() {
		if (settings == null) {
			settings = new SettingsStore(this);
		}

		return settings;
	}


	public void setOptionsCount(@NonNull Callable<Integer> getOptionsCount) {
		this.getOptionsCount = getOptionsCount;
	}


	protected void resetKeyRepeat() {
		lastKey = KeyEvent.KEYCODE_UNKNOWN;
	}


	/**
	 * Simulates a click on the option at the given position. Positions are 1-based.
	 */
	protected void selectOption(int position, boolean click) {
		int optionsCount;

		try {
			optionsCount = getOptionsCount.call();
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Keypad navigation not possible. Failed to get options count. " + e);
			return;
		}

		if (position <= 0 || position > optionsCount) {
			return;
		}

		BaseInputConnection inputConnection = new BaseInputConnection(getWindow().getDecorView(), true);

		// Scroll to the bottom to make sure we have a correct base for counting to the desired position
		// Scrolling to top, then down to the position is not possible, because some phones allow
		// selecting the Back button, but others don't.
		scroll(inputConnection, optionsCount + 1, false);
		scroll(inputConnection, optionsCount - position, true);

		if (click) {
			clickSelected(inputConnection);
		}
	}


	private void scroll(@NonNull InputConnection connection, int positions, boolean up) {
		KeyEvent press = new KeyEvent(KeyEvent.ACTION_DOWN, up ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN);
		KeyEvent release = new KeyEvent(KeyEvent.ACTION_UP, up ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN);
		press.setSource(InputDevice.SOURCE_UNKNOWN);
		release.setSource(InputDevice.SOURCE_UNKNOWN);

		for (int i = 0; i < positions; i++) {
			connection.sendKeyEvent(press);
			connection.sendKeyEvent(release);
		}
	}


	private void clickSelected(@NonNull InputConnection connection) {
		KeyEvent enterPress = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
		KeyEvent enterRelease = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
		connection.sendKeyEvent(enterPress);
		connection.sendKeyEvent(enterRelease);
	}
}
