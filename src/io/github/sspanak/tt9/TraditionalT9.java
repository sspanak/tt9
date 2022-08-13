package io.github.sspanak.tt9;

import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import io.github.sspanak.tt9.db.T9DB;
import io.github.sspanak.tt9.ui.CandidateView;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ime.InputFieldHelper;
import io.github.sspanak.tt9.ime.SoftKeyHandler;
import io.github.sspanak.tt9.preferences.T9Preferences;

import java.util.List;

public class TraditionalT9 extends InputMethodService {
	private InputConnection currentInputConnection = null;

	private SoftKeyHandler softKeyHandler = null;
	private CandidateView mCandidateView;


	private T9DB db;
	private T9Preferences prefs;

	private int mInputMode = T9Preferences.MODE_123;
	private int mCapsMode = T9Preferences.CASE_LOWER;
	private int mLanguage = 0;

	private static final int NON_EDIT = 0;
	private static final int EDITING = 1;
	private static final int EDITING_NOSHOW = 2;
	private int mEditing = NON_EDIT;

	// throttling
	private static final int BACKSPACE_DEBOUNCE_TIME = 100;
	private long lastBackspaceCall = 0;
	private int ignoreNextKeyUp = 0;

	/**
	 * Main initialization of the input method component. Be sure to call to
	 * super class.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		db = T9DB.getInstance(this);
		prefs = T9Preferences.getInstance(this);

		if (softKeyHandler == null) {
			softKeyHandler = new SoftKeyHandler(getLayoutInflater().inflate(R.layout.mainview, null), this);
		}
	}


	@Override
	public boolean onEvaluateInputViewShown() {
		super.onEvaluateInputViewShown();

		//Log.d("T9.onEvaluateInputViewShown", "whatis");
		//Log.d("T9.onEval", "fullscreen?: " + isFullscreenMode() + " isshow?: " + isInputViewShown() + " isrequestedshow?: " + isShowInputRequested());

		if (mEditing == EDITING_NOSHOW) {
			return false;
		}

		return true;
	}


	/**
	 * Called by the framework when your view for creating input needs to be
	 * generated. This will be called the first time your input method is
	 * displayed, and every time it needs to be re-created such as due to a
	 * configuration change.
	 */
	@Override
	public View onCreateInputView() {
		View v = getLayoutInflater().inflate(R.layout.mainview, null);
		softKeyHandler.changeView(v);
		return v;
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
		if (currentInputConnection == null || inputField.inputType == InputType.TYPE_NULL) {
			finish();
			return;
		}


		// @todo: get relevant settings
		mLanguage = prefs.getInputLanguage();

		// initialize typing mode
		mEditing = InputFieldHelper.isFilterTextField(inputField) ? EDITING_NOSHOW : EDITING;
		mInputMode = InputFieldHelper.determineInputMode(inputField, prefs.getInputMode());

		// @todo: determine case from input

		// @todo: show or hide UI elements

		updateStatusIcon();

		// @todo: handle word adding
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

			finish();
		}
	}


	@Override
	public void onDestroy() {
		db.close();
		super.onDestroy();
	}


	/**
	 * Deal with the editor reporting movement of its cursor.
	 */
	@Override
	public void onUpdateSelection(
		int oldSelStart,
		int oldSelEnd,
		int newSelStart,
		int newSelEnd,
		int candidatesStart,
		int candidatesEnd
	) {
		// @todo: pass-throught to super.onUpdateSelection()?

		// @todo: commit text

		// @todo: clear candidates
	}


	/**
	 * Use this to monitor key events being delivered to the application. We get
	 * first crack at them, and can either resume them or let them continue to
	 * the app.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isOff()) {
			return false;
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
		event.startTracking();

		if (keyCode == prefs.getKeyOtherActions() || keyCode == prefs.getKeyInputMode()) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}


	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (isOff()) {
			return false;
		}

		Log.d("onLongPress", "LONG PRESS: " + keyCode);

		if (event.getRepeatCount() > 1) {
			return true;
		}

		if (keyCode == prefs.getKeyOtherActions()) {
			ignoreNextKeyUp = keyCode;
			UI.showPreferencesScreen(this);
			return true;
		}

		if (keyCode == prefs.getKeyInputMode()) {
			ignoreNextKeyUp = keyCode;
			nextLang();
			return true;
		}

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

		Log.d("onKeyUp", "Key: " + keyCode + " repeat?: " + event.getRepeatCount());

		if (keyCode == ignoreNextKeyUp) {
			ignoreNextKeyUp = 0;
			return true;
		}

		if (keyCode == prefs.getKeyBackspace() && InputFieldHelper.isThereText(currentInputConnection)) {
			return true;
		}

		if (keyCode == prefs.getKeyOtherActions()) {
			showAddWord();
			return true;
		}

		if (keyCode == prefs.getKeyInputMode()) {
			nextKeyMode();
			// @todo: if in predictive mode and composing a word, change the case only
			return true;
		}

		switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
				return handleEnter();

		}

		return super.onKeyUp(keyCode, event);
	}


	public boolean handleEnter() {
		Log.d("handleBackspace", "enter handler");

		if (!isInputViewShown()) {
			showWindow(true);
		}

		// @todo: commit current text and return true


		return false;
	}


	public boolean handleBackspace() {
		if (!InputFieldHelper.isThereText(currentInputConnection)) {
			Log.d("handleBackspace", "backspace ignored");
			return false;
		}

		// @todo: hide candidates in ABC and Predictive mode

		// @todo: commit the current text

		currentInputConnection.deleteSurroundingText(1, 0);

		Log.d("handleBackspace", "backspace handled");
		return true;
	}


	public boolean handleBackspaceHold() {
		if (System.currentTimeMillis() - lastBackspaceCall < BACKSPACE_DEBOUNCE_TIME) {
			return true;
		}

		boolean handled = handleBackspace();
		lastBackspaceCall = System.currentTimeMillis();

		return handled;
	}


	private boolean isOff() {
		return currentInputConnection == null || mEditing == NON_EDIT;
	}


	private void clearState() {
		// @todo: clear suggestions
		// @todo: clear composition
		// @todo: clear previous word
		mEditing = NON_EDIT;
		mLanguage = 0;
	}


	private void finish() {
		clearState();
		hideStatusIcon();
		hideWindow();
	}


	/**
	 * Helper function to commit any text being composed in to the editor.
	 */
	private void commitText() {
		// @todo: pick current candidate
		// @todo: add it to the text field
		clearState();
		setCandidatesViewShown(false);
		// @todo: clear candidates
	}


	private void setCandidates(List<String> suggestions, int initialSel) {
		if (suggestions != null && suggestions.size() > 0) {
			setCandidatesViewShown(true);
		}
		if (mCandidateView != null) {
			mCandidateView.setSuggestions(suggestions, initialSel);
		}
	}


	private void restoreLastWordIfAny() {
		// mAddingWord = false;
		String word = prefs.getLastWord();
		if (word.equals("")) {
			prefs.setLastWord("");

			// @todo: push the word to the text field
		}
	}


	protected void nextKeyMode() {
		// @todo: commit current text

		// select next mode
		if (mInputMode == T9Preferences.MODE_PREDICTIVE) {
			mInputMode = T9Preferences.MODE_123;
		} else if (mInputMode == T9Preferences.MODE_123) {
			mInputMode = T9Preferences.MODE_ABC;
			mCapsMode = T9Preferences.CASE_LOWER;
		} else if (mInputMode == T9Preferences.MODE_ABC && mCapsMode == T9Preferences.CASE_LOWER) {
			mCapsMode = T9Preferences.CASE_UPPER;
		} else {
			mInputMode = T9Preferences.MODE_PREDICTIVE;
			mCapsMode = T9Preferences.CASE_CAPITALIZE;
		}

		updateStatusIcon();
	}


	private void nextLang() {
		// @todo: commit current text

		// @todo: select next language
		Log.d("nextLang", "current language: " + mLanguage + ". Selecting next");

		updateStatusIcon();
	}


	/**
	 * updateStatusIcon
	 * Set the status icon that is appropriate in current mode (based on
	 * openwmm-legacy)
	 */
	private void updateStatusIcon() {
		switch (mInputMode) {
			case T9Preferences.MODE_ABC:
				// @todo: show the proper status icon
				showStatusIcon(LangHelper.ICONMAP[0][mInputMode][mCapsMode]);
				break;
			case T9Preferences.MODE_PREDICTIVE:
				// @todo: show the proper status icon
				showStatusIcon(LangHelper.ICONMAP[0][mInputMode][mCapsMode]);
				break;
			case T9Preferences.MODE_123:
				showStatusIcon(R.drawable.ime_number);
				break;
			default:
				Log.i("updateStatusIcon", "Unknown inputMode mode: " + mInputMode + ". Hiding status icon.");
				hideStatusIcon();
				break;
		}
	}


	protected void showAddWord() {
		if (mInputMode != T9Preferences.MODE_PREDICTIVE) {
			return;
		}

		clearState();
		// @todo: clear the candidate list
		currentInputConnection.setComposingText("", 0);
		currentInputConnection.finishComposingText();

		String template = "";

		// @todo: get the current word template from the input connection
		// template = getSurroundingWord();

		UI.showAddWordDialog(this, mLanguage, template);
	}


	/**
	 * This tells us about completions that the editor has determined based on
	 * the current text in it. We want to use this in fullscreen mode to show
	 * the completions ourself, since the editor can not be seen in that
	 * situation.
	 */
	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
		// @todo: see if this can be deleted or it should be an empty function
	}
}
