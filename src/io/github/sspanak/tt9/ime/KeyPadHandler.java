package io.github.sspanak.tt9.ime;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.preferences.SettingsStore;


abstract class KeyPadHandler extends InputMethodService {
	protected InputConnection currentInputConnection = null;

	protected SettingsStore settings;

	// temporal key handling
	private boolean isBackspaceHandled = false;

	private int ignoreNextKeyUp = 0;

	private int lastKeyCode = 0;
	private int keyRepeatCounter = 0;

	private int lastNumKeyCode = 0;
	private int numKeyRepeatCounter = 0;


	/**
	 * Main initialization of the input method component. Be sure to call to
	 * super class.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		settings = new SettingsStore(getApplicationContext());

		onInit();
	}


	@Override
	public boolean onEvaluateInputViewShown() {
		super.onEvaluateInputViewShown();
		onRestart(getCurrentInputEditorInfo());
		return shouldBeVisible();
	}


	@Override
	public boolean onEvaluateFullscreenMode() {
		return false;
	}


	/**
	 * Called by the framework when your view for creating input needs to be
	 * generated. This will be called the first time your input method is
	 * displayed, and every time it needs to be re-created such as due to a
	 * configuration change.
	 */
	@Override
	public View onCreateInputView() {
		return createSoftKeyView();
	}


	/**
	 * This is the main point where we do our initialization of the input method
	 * to begin operating on an application. At this point we have been bound to
	 * the client, and are now receiving all of the detailed information about
	 * the target of our edits.
	 */
	@Override
	public void onStartInput(EditorInfo inputField, boolean restarting) {
		currentInputConnection = getCurrentInputConnection();
		// Logger.d("T9.onStartInput", "inputType: " + inputField.inputType + " fieldId: " + inputField.fieldId + " fieldName: " + inputField.fieldName + " packageName: " + inputField.packageName);
		onStart(inputField);
	}


	@Override
	public void onStartInputView(EditorInfo inputField, boolean restarting) {
		currentInputConnection = getCurrentInputConnection();
		onRestart(inputField);
	}


	@Override
	public void onFinishInputView(boolean finishingInput) {
		super.onFinishInputView(finishingInput);
		onFinishTyping();
	}


	/**
	 * Use this to monitor key events being delivered to the application. We get
	 * first crack at them, and can either resume them or let them continue to
	 * the app.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (shouldBeOff()) {
			return false;
		}

//		Logger.d("onKeyDown", "Key: " + event + " repeat?: " + event.getRepeatCount() + " long-time: " + event.isLongPress());

		// "backspace" key must repeat its function when held down, so we handle it in a special way
		if (Key.isBackspace(settings, keyCode) && onBackspace()) {
			return isBackspaceHandled = true;
		} else {
			isBackspaceHandled = false;
		}

		// start tracking key hold
		if (Key.isNumber(keyCode) || Key.isHotkey(settings, -keyCode)) {
			event.startTracking();
		}

		if (Key.isBack(keyCode)) {
			return onBack() && super.onKeyDown(keyCode, event);
		}

		return
			Key.isNumber(keyCode)
			|| Key.isOK(keyCode)
			|| handleHotkey(keyCode, true, false, true) // hold a hotkey, handled in onKeyLongPress())
			|| handleHotkey(keyCode, false, keyRepeatCounter + 1 > 0, true) // press a hotkey, handled in onKeyUp()
			|| Key.isPoundOrStar(keyCode) && onText(String.valueOf((char) event.getUnicodeChar()), true)
			|| super.onKeyDown(keyCode, event); // let the system handle the keys we don't care about (usually, the touch "buttons")
	}


	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (shouldBeOff()) {
			return false;
		}

//		Logger.d("onLongPress", "LONG PRESS: " + keyCode);

		if (event.getRepeatCount() > 1) {
			return true;
		}

		ignoreNextKeyUp = keyCode;
		if (Key.isNumber(keyCode)) {
			numKeyRepeatCounter = 0;
			lastNumKeyCode = 0;
		} else {
			keyRepeatCounter = 0;
			lastKeyCode = 0;
		}

		if (handleHotkey(keyCode, true, false, false)) {
			return true;
		}

		if (Key.isNumber(keyCode)) {
			return onNumber(Key.codeToNumber(settings, keyCode), true, 0);
		}

		ignoreNextKeyUp = 0;
		return false;
	}


	/**
	 * Use this to monitor key events being delivered to the application. We get
	 * first crack at them, and can either resume them or let them continue to
	 * the app.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (shouldBeOff()) {
			return false;
		}

		//		Logger.d("onKeyUp", "Key: " + keyCode + " repeat?: " + event.getRepeatCount());

		if (keyCode == ignoreNextKeyUp) {
//			Logger.d("onKeyUp", "Ignored: " + keyCode);
			ignoreNextKeyUp = 0;
			return true;
		}

		if (isBackspaceHandled) {
			return true;
		}

		keyRepeatCounter = (lastKeyCode == keyCode) ? keyRepeatCounter + 1 : 0;
		lastKeyCode = keyCode;

		if (Key.isNumber(keyCode)) {
			numKeyRepeatCounter = (lastNumKeyCode == keyCode) ? numKeyRepeatCounter + 1 : 0;
			lastNumKeyCode = keyCode;
			return onNumber(Key.codeToNumber(settings, keyCode), false, numKeyRepeatCounter);
		}

		if (Key.isBack(keyCode)) {
			return onBack() && super.onKeyUp(keyCode, event);
		}

		return
			Key.isOK(keyCode) && onOK()
			|| handleHotkey(keyCode, false, keyRepeatCounter > 0, false)
			|| Key.isPoundOrStar(keyCode) && onText(String.valueOf((char) event.getUnicodeChar()), false)
			|| super.onKeyUp(keyCode, event); // let the system handle the keys we don't care about (usually, the touch "buttons")
	}


	private boolean handleHotkey(int keyCode, boolean hold, boolean repeat, boolean validateOnly) {
		if (keyCode == settings.getKeyAddWord() * (hold ? -1 : 1)) {
			return onKeyAddWord(validateOnly);
		}

		if (keyCode == settings.getKeyFilterClear() * (hold ? -1 : 1)) {
			return onKeyFilterClear(validateOnly);
		}

		if (keyCode == settings.getKeyFilterSuggestions() * (hold ? -1 : 1)) {
			return onKeyFilterSuggestions(validateOnly, repeat);
		}

		if (keyCode == settings.getKeyNextLanguage() * (hold ? -1 : 1)) {
			return onKeyNextLanguage(validateOnly);
		}

		if (keyCode == settings.getKeyNextInputMode() * (hold ? -1 : 1)) {
			return onKeyNextInputMode(validateOnly);
		}

		if (keyCode == settings.getKeyPreviousSuggestion() * (hold ? -1 : 1)) {
			return onKeyScrollSuggestion(validateOnly, true);
		}

		if (keyCode == settings.getKeyNextSuggestion() * (hold ? -1 : 1)) {
			return onKeyScrollSuggestion(validateOnly, false);
		}

		if (keyCode == settings.getKeyShowSettings() * (hold ? -1 : 1)) {
			return onKeyShowSettings(validateOnly);
		}

		return false;
	}


	protected void resetKeyRepeat() {
		numKeyRepeatCounter = 0;
		keyRepeatCounter = 0;
		lastNumKeyCode = 0;
		lastKeyCode = 0;
	}


	// hardware key handlers
	abstract protected boolean onBack();
	abstract public boolean onBackspace();
	abstract protected boolean onNumber(int key, boolean hold, int repeat);
	abstract public boolean onOK();
	abstract public boolean onText(String text, boolean validateOnly); // used for "#", "*" and whatnot

	// hotkey handlers
	abstract protected boolean onKeyAddWord(boolean validateOnly);
	abstract protected boolean onKeyFilterClear(boolean validateOnly);
	abstract protected boolean onKeyFilterSuggestions(boolean validateOnly, boolean repeat);
	abstract protected boolean onKeyNextLanguage(boolean validateOnly);
	abstract protected boolean onKeyNextInputMode(boolean validateOnly);
	abstract protected boolean onKeyScrollSuggestion(boolean validateOnly, boolean backward);
	abstract protected boolean onKeyShowSettings(boolean validateOnly);

	// helpers
	abstract protected void onInit();
	abstract protected void onStart(EditorInfo inputField);
	abstract protected void onRestart(EditorInfo inputField);
	abstract protected void onFinishTyping();

	// UI
	abstract protected View createSoftKeyView();
	abstract protected boolean shouldBeVisible();
	abstract protected boolean shouldBeOff();
}
