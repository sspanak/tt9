package io.github.sspanak.tt9.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract public class ActivityWithNavigation extends AppCompatActivity {
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

		if (settings != null) {
			clickOption(Key.codeToNumber(settings, keyCode));
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


	/**
	 * Simulates a click on the option at the given position.
	 */
	public void clickOption(int position) {
		BaseInputConnection inputConnection = new BaseInputConnection(getWindow().getDecorView(), true);
		scrollToTop(inputConnection);
		selectOption(inputConnection, position);
		clickSelectedOption(inputConnection);
	}


	private void scrollToTop(@NonNull InputConnection connection) {
		KeyEvent upPress = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP);
		KeyEvent upRelease = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP);

		for (int i = getOptionsCount(); i > 0; i--) {
			connection.sendKeyEvent(upPress);
			connection.sendKeyEvent(upRelease);
		}
	}


	private void selectOption(@NonNull InputConnection connection, int position) {
		KeyEvent downPress = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN);
		KeyEvent downRelease = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN);

		for (int i = position; i > 0; i--) {
			connection.sendKeyEvent(downPress);
			connection.sendKeyEvent(downRelease);
		}
	}


	private void clickSelectedOption(@NonNull InputConnection connection) {
		KeyEvent enterPress = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
		KeyEvent enterRelease = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
		connection.sendKeyEvent(enterPress);
		connection.sendKeyEvent(enterRelease);
	}


	abstract protected int getOptionsCount();
}
