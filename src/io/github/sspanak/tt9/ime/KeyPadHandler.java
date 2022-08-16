package io.github.sspanak.tt9.ime;

import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import io.github.sspanak.tt9.db.T9DB;
import io.github.sspanak.tt9.ui.CandidateView;
import io.github.sspanak.tt9.preferences.T9Preferences;

import java.util.ArrayList;
import java.util.List;

public abstract class KeyPadHandler extends InputMethodService {
	protected InputConnection currentInputConnection = null;

	protected CandidateView mCandidateView;
	protected T9DB db;
	protected T9Preferences prefs;

	// input mode
	protected int mInputMode = T9Preferences.MODE_123;
	protected int mCapsMode = T9Preferences.CASE_LOWER;

	protected static final int NON_EDIT = 0;
	protected static final int EDITING = 1;
	protected static final int EDITING_NOSHOW = 2;
	protected static final int EDITING_STRICT_NUMERIC = 3;
	protected int mEditing = NON_EDIT;
	protected ArrayList<Integer> allowedEditingModes;

	// temporal key handling
	private int ignoreNextKeyUp = 0;
	private int lastKeyCode = 0;
	protected boolean isKeyCodeRepeated = false;

	// throttling
	private static final int BACKSPACE_DEBOUNCE_TIME = 100;
	private long lastBackspaceCall = 0;



	/**
	 * Main initialization of the input method component. Be sure to call to
	 * super class.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		db = T9DB.getInstance(this);
		prefs = T9Preferences.getInstance(this);

		onInit();
	}


	@Override
	public boolean onEvaluateInputViewShown() {
		super.onEvaluateInputViewShown();
		return mEditing != EDITING_NOSHOW;
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
	 * Called by the framework when your view for showing candidates needs to be
	 * generated, like {@link #onCreateInputView}.
	 */
	@Override
	public View onCreateCandidatesView() {
		if (mCandidateView == null) {
			mCandidateView = new CandidateView(this);
		}
		return mCandidateView;
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
		// Log.d("T9.onStartInput", "INPUTTYPE: " + inputField.inputType + " FIELDID: " + inputField.fieldId +
		// 	" FIELDNAME: " + inputField.fieldName + " PACKAGE NAME: " + inputField.packageName);

		clearState();

		// https://developer.android.com/reference/android/text/InputType#TYPE_NULL
		// Special or limited input type. This means the input connection is not rich,
		// or it can not process or show things like candidate text, nor retrieve the current text.
		// We just let Android handle this input.
		if (currentInputConnection == null || inputField == null || inputField.inputType == InputType.TYPE_NULL) {
			onFinish();
			return;
		}

		initTypingMode(inputField);

		// @todo: determine case from input

		onRestart();
	}


	/**
	 * This is called when the user is done editing a field. We can use this to
	 * reset our state.
	 */
	@Override
	public void onFinishInput() {
		super.onFinishInput();
		// Log.d("onFinishInput", "When is this called?");
		if (mEditing == EDITING || mEditing == EDITING_NOSHOW) {
			// @todo: save last language

			// @todo: pick the current candidate in PREDICTIVE mode

			// @todo: commit current text

			onFinish();
		}
	}


	@Override
	public void onDestroy() {
		db.close();
		super.onDestroy();
	}


	/**
	 * Use this to monitor key events being delivered to the application. We get
	 * first crack at them, and can either resume them or let them continue to
	 * the app.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isOff()) {
			return super.onKeyDown(keyCode, event);
		}

		Log.d("onKeyDown", "Key: " + event + " repeat?: " + event.getRepeatCount() + " long-time: " + event.isLongPress());

		// backspace key must repeat its function when held down, so we handle it in a special way
		if (keyCode == prefs.getKeyBackspace()) {
			boolean isThereTextBefore = InputFieldHelper.isThereText(currentInputConnection);
			boolean backspaceHandleStatus = handleBackspaceHold();

			// Allow BACK key to function as back when there is no text
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				return isThereTextBefore;
			} else {
				return backspaceHandleStatus;
			}
		}

		// start tracking key hold
		// @todo: consider tracking only when necessary
		event.startTracking();

		if (keyCode == KeyEvent.KEYCODE_0) {
			return true;
		}

		// in numeric text fields, we do not want to handle anything,
		// but backspace or 0 (because of +)
		if (mEditing == EDITING_STRICT_NUMERIC) {
			return false;
		}

		if (
				keyCode == prefs.getKeyOtherActions()
				|| keyCode == prefs.getKeyInputMode()
				|| keyCode == KeyEvent.KEYCODE_STAR
				|| keyCode == KeyEvent.KEYCODE_POUND
				|| (isNumber(keyCode) && mInputMode != T9Preferences.MODE_123)
				|| (!isCandidateViewHidden() && (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN))
		) {
			return true;
		}

		return false;
	}


	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (isOff()) {
			return super.onKeyDown(keyCode, event);
		}

		Log.d("onLongPress", "LONG PRESS: " + keyCode);

		if (event.getRepeatCount() > 1) {
			return true;
		}

		ignoreNextKeyUp = keyCode;

		if (keyCode == prefs.getKeyOtherActions()) {
			return onKeyOtherAction(true);
		}

		if (keyCode == prefs.getKeyInputMode()) {
			return onKeyInputMode(true);
		}

		switch (keyCode) {
			case KeyEvent.KEYCODE_0: return on0(true);
			case KeyEvent.KEYCODE_1: return on1(true);
			case KeyEvent.KEYCODE_2:
			case KeyEvent.KEYCODE_3:
			case KeyEvent.KEYCODE_4:
			case KeyEvent.KEYCODE_5:
			case KeyEvent.KEYCODE_6:
			case KeyEvent.KEYCODE_7:
			case KeyEvent.KEYCODE_8:
			case KeyEvent.KEYCODE_9:
				return on2to9(keyCodeToKeyNumber(keyCode), true);
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
		if (isOff()) {
			return false;
		}

		if (keyCode == ignoreNextKeyUp) {
			Log.d("onKeyUp", "Ignored: " + keyCode);
			ignoreNextKeyUp = 0;
			return true;
		}

		isKeyCodeRepeated = (lastKeyCode == keyCode);
		lastKeyCode = keyCode;

		Log.d("onKeyUp", "Key: " + keyCode + " repeat?: " + event.getRepeatCount());

		if (keyCode == prefs.getKeyBackspace() && InputFieldHelper.isThereText(currentInputConnection)) {
			return true;
		}

		// handle 0 even in STRICT_NUMERIC mode, because of "+"
		if (keyCode == KeyEvent.KEYCODE_0) {
			return on0(false);
		}

		// in numeric text fields, we do not want to handle anything,
		// but backspace or 0 (because of +)
		if (mEditing == EDITING_STRICT_NUMERIC) {
			return false;
		}

		if (keyCode == prefs.getKeyOtherActions()) {
			return onKeyOtherAction(false);
		}

		if (keyCode == prefs.getKeyInputMode()) {
			return onKeyInputMode(false);
		}

		switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER: return onOK();
			case KeyEvent.KEYCODE_DPAD_UP: return onUp();
			case KeyEvent.KEYCODE_DPAD_DOWN: return onDown();
			case KeyEvent.KEYCODE_1: return on1(false);
			case KeyEvent.KEYCODE_2:
			case KeyEvent.KEYCODE_3:
			case KeyEvent.KEYCODE_4:
			case KeyEvent.KEYCODE_5:
			case KeyEvent.KEYCODE_6:
			case KeyEvent.KEYCODE_7:
			case KeyEvent.KEYCODE_8:
			case KeyEvent.KEYCODE_9:
				return on2to9(keyCodeToKeyNumber(keyCode), false);
			case KeyEvent.KEYCODE_STAR: return onStar();
			case KeyEvent.KEYCODE_POUND: return onPound();
		}

		return false;
	}


	protected boolean handleBackspaceHold() {
		if (System.currentTimeMillis() - lastBackspaceCall < BACKSPACE_DEBOUNCE_TIME) {
			return true;
		}

		boolean handled = onBackspace();
		lastBackspaceCall = System.currentTimeMillis();

		return handled;
	}


	protected void clearState() {
		setCandidates(null);
		currentInputConnection.setComposingText("", 0);
		currentInputConnection.finishComposingText();
		// @todo: clear previous word
		mEditing = NON_EDIT;
	}


	private boolean isOff() {
		return currentInputConnection == null || mEditing == NON_EDIT;
	}


	private boolean isNumber(int keyCode) {
		return keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9;
	}


	private void initTypingMode(EditorInfo inputField) {
		allowedEditingModes = InputFieldHelper.determineInputModes(inputField);

		int lastInputMode = prefs.getInputMode();
		if (allowedEditingModes.contains(lastInputMode)) {
			mInputMode = lastInputMode;
		} else if (allowedEditingModes.contains(T9Preferences.MODE_ABC)) {
			mInputMode = T9Preferences.MODE_ABC;
		} else {
			mInputMode = allowedEditingModes.get(0);
		}

		if (mInputMode == T9Preferences.MODE_123 && allowedEditingModes.size() == 1) {
			mEditing = EDITING_STRICT_NUMERIC;
		} else {
			// @todo: honor EDITING_NOSHOW, where appropriate
			mEditing = InputFieldHelper.isFilterTextField(inputField) ? EDITING_NOSHOW : EDITING;
		}
	}


	private int keyCodeToKeyNumber(int keyCode) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_0:
				return 0;
			case KeyEvent.KEYCODE_1:
				return 1;
			case KeyEvent.KEYCODE_2:
				return 2;
			case KeyEvent.KEYCODE_3:
				return 3;
			case KeyEvent.KEYCODE_4:
				return 4;
			case KeyEvent.KEYCODE_5:
				return 5;
			case KeyEvent.KEYCODE_6:
				return 6;
			case KeyEvent.KEYCODE_7:
				return 7;
			case KeyEvent.KEYCODE_8:
				return 8;
			case KeyEvent.KEYCODE_9:
				return 9;
			default:
				return -1;
		}
	}


	// default hardware key handlers
	abstract public boolean onBackspace();
	abstract public boolean onOK();
	abstract protected boolean onUp();
	abstract protected boolean onDown();
	abstract protected boolean on0(boolean hold);
	abstract protected boolean on1(boolean hold);
	abstract protected boolean on2to9(int key, boolean hold);
	abstract protected boolean onStar();
	abstract protected boolean onPound();

	// customized key handlers
	abstract protected boolean onKeyInputMode(boolean hold);
	abstract protected boolean onKeyOtherAction(boolean hold);

	// helpers
	abstract protected void onInit();
	abstract protected void onRestart();
	abstract protected void onFinish();
	abstract protected View createSoftKeyView();
	abstract protected void setCandidates(List<String> suggestions);
	abstract protected void setCandidates(List<String> suggestions, int initialSel);
	abstract protected boolean isCandidateViewHidden();

///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////// THE ONES BELOW MAY BE UNNECESSARY. IMPLEMENT IF NEEDED. /////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///
	/**
	 * Deal with the editor reporting movement of its cursor.
	 */
/*	@Override
	public void onUpdateSelection(
		int oldSelStart,
		int oldSelEnd,
		int newSelStart,
		int newSelEnd,
		int candidatesStart,
		int candidatesEnd
	) {
		// @todo: implement if necessary

		// @todo: pass-through to super.onUpdateSelection()?

		// @todo: commit text

		// @todo: clear candidates
	}*/


	/**
	 * This tells us about completions that the editor has determined based on
	 * the current text in it. We want to use this in fullscreen mode to show
	 * the completions ourself, since the editor can not be seen in that
	 * situation.
	 */
/*	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
		// @todo: see if this can be deleted or it should be an empty function
	}*/
}
