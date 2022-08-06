package io.github.sspanak.tt9;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import io.github.sspanak.tt9.LangHelper.LANGUAGE;
import io.github.sspanak.tt9.Utils.SpecialInputType;
import io.github.sspanak.tt9.db.T9DB;
import io.github.sspanak.tt9.ime.InterfaceHandler;
import io.github.sspanak.tt9.preferences.T9Preferences;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class TraditionalT9 extends InputMethodService {
	private InputConnection currentInputConnection = null;

	private InterfaceHandler interfacehandler = null;
	private CandidateView mCandidateView;
	private AbsSymDialog mSmileyPopup = null;
	private AbsSymDialog mSymbolPopup = null;

	private T9DB db;
	private T9Preferences prefs;

	private static final int NON_EDIT = 0;
	private static final int EDITING = 1;
	private static final int EDITING_NOSHOW = 2;
	private int mEditing;


	/**
	 * Main initialization of the input method component. Be sure to call to
	 * super class.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		db = T9DB.getInstance(this);
		prefs = new T9Preferences(this);

		if (interfacehandler == null) {
			interfacehandler = new InterfaceHandler(getLayoutInflater().inflate(R.layout.mainview,
					null), this);
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
		interfacehandler.changeView(v);
		// if (mKeyMode == T9Preferences.MODE_PREDICTIVE) {
		// 	interfacehandler.showHold(true);
		// } else {
		// 	interfacehandler.showHold(false);
		// }
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
		if (inputField.inputType == InputType.TYPE_NULL) {
			finish();
			return;
		}


		// @todo: get relevant settings

		// @todo: initialize typing mode

		// @todo: determine case from input

		// @todo: show or hide UI elements

		// @todo: show status icon

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
		Log.d("onKeyDown", "Key: " + event + " repeat?: " + event.getRepeatCount() + " long-time: " + event.isLongPress());

/*		switch(keyCode) {
			case KeyEvent.KEYCODE_SOFT_LEFT:
			case KeyEvent.KEYCODE_SOFT_RIGHT:
				interfacehandler.setPressedInUI(keyCode, true);
				return true;
		}*/

		return false;
	}


	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Log.d("onLongPress", "LONG PRESS: " + keyCode);

		// if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
		// 	handleBackspace();
		// }


		// Break for keys with no repeat function
		if (event.getRepeatCount() > 1) {
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
			Log.d("onKeyUp", "Key: " + keyCode + " repeat?: " +
				event.getRepeatCount());

/*		switch(keyCode) {
			case KeyEvent.KEYCODE_SOFT_LEFT:
				showPreferencesScreen();
				return true;

			case KeyEvent.KEYCODE_SOFT_RIGHT:
				handleBackspace();
				interfacehandler.setPressedInUI(keyCode, false);
				return true;
		}*/

		return false;
	}


	protected void handleEnter() {
		if (!isInputViewShown()) {
			showWindow(true);
			return;
		}

		// @todo: commit current text
	}


	public void handleBackspace() {
		Log.d("handleBackspace", "backspace hit");

		// @todo: erase the cursor before the cursor

		// @todo: commit current text
	}


	private boolean isSpecializedTextField(EditorInfo inputField) {
		int variation = inputField.inputType & InputType.TYPE_MASK_VARIATION;

		return (
				variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
				|| variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
				|| variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
				|| variation == InputType.TYPE_TEXT_VARIATION_URI
				|| variation == InputType.TYPE_TEXT_VARIATION_FILTER
		);
	}


	/**
	 * isFilterTextField
	 * handle filter list cases... do not hijack DPAD center and make sure back's go through proper
	 *
	 * @param  inputField
	 * @return boolean
	 */
	private boolean isFilterTextField(EditorInfo inputField) {
		int inputType = inputField.inputType & InputType.TYPE_MASK_CLASS;
		int inputVariation = inputField.inputType & InputType.TYPE_MASK_VARIATION;

		return inputType == InputType.TYPE_CLASS_TEXT && inputVariation == InputType.TYPE_TEXT_VARIATION_FILTER;
	}


	private boolean isThereText() {
		if (getCurrentInputConnection() == null) {
			return false;
		}

		ExtractedText extractedText = getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0);
		return extractedText != null && extractedText.text.length() > 0;
	}


	private void clearState() {
		// @todo: clear suggestions
		// @todo: clear composition
		// @todo: clear previous word
		// @todo: switch to NON_EDIT
	}


	private void finish() {
		clearState();

		// @todo: language = null

		// @todo: hide window and status icon
	}


	/**
	 * determineInputMode
	 * Determine the typing mode based on the input field being edited.
	 *
	 * @param  inputField
	 * @return T9Preferences.MODE_ABC | T9Preferences.MODE_123 | T9Preferences.MODE_PREDICTIVE
	 */
	private int determineInputMode(EditorInfo inputField) {
		if (inputField.inputType == SpecialInputType.TYPE_SHARP_007H_PHONE_BOOK) {
			return T9Preferences.MODE_ABC;
		}

		if (inputField.privateImeOptions != null && inputField.privateImeOptions.equals("io.github.sspanak.tt9.addword=true")) {
			return T9Preferences.MODE_ABC;
		}

		switch (inputField.inputType & InputType.TYPE_MASK_CLASS) {
			case InputType.TYPE_CLASS_NUMBER:
			case InputType.TYPE_CLASS_DATETIME:
				// Numbers and dates default to the symbols keyboard, with
				// no extra features.
			case InputType.TYPE_CLASS_PHONE:
				// Phones will also default to the symbols keyboard, though
				// often you will want to have a dedicated phone keyboard.
				return T9Preferences.MODE_123;

			case InputType.TYPE_CLASS_TEXT:
				// This is general text editing. We will default to the
				// normal alphabetic keyboard, and assume that we should
				// be doing predictive text (showing candidates as the
				// user types).

				return isSpecializedTextField(inputField) ? T9Preferences.MODE_ABC : prefs.getInputMode();

			default:
				// For all unknown input types, default to the alphabetic
				// keyboard with no special features.
				return T9Preferences.MODE_ABC;
		}
	}


	/**
	 * Helper to update the shift state of our keyboard based on the initial
	 * editor state.
	 */
	private void deterimineTextCase(EditorInfo inputField) {
		// Log.d("updateShift", "CM start: " + mCapsMode);
		// if (inputField != null && mCapsMode != T9Preferences.CASE_UPPER) {
		// 	int caps = 0;
		// 	if (inputField.inputType != InputType.TYPE_NULL) {
		// 		caps = currentInputConnection.getCursorCapsMode(inputField.inputType);
		// 	}
		// 	// mInputView.setShifted(mCapsLock || caps != 0);
		// 	// Log.d("updateShift", "caps: " + caps);
		// 	if ((caps & TextUtils.CAP_MODE_CHARACTERS) == TextUtils.CAP_MODE_CHARACTERS) {
		// 		mCapsMode = T9Preferences.CASE_UPPER;
		// 	} else if ((caps & TextUtils.CAP_MODE_SENTENCES) == TextUtils.CAP_MODE_SENTENCES) {
		// 		mCapsMode = T9Preferences.CASE_CAPITALIZE;
		// 	} else if ((caps & TextUtils.CAP_MODE_WORDS) == TextUtils.CAP_MODE_WORDS) {
		// 		mCapsMode = T9Preferences.CASE_CAPITALIZE;
		// 	} else {
		// 		mCapsMode = T9Preferences.CASE_LOWER;
		// 	}
		// 	updateStatusIcon();
		// }
		// Log.d("updateShift", "CM end: " + mCapsMode);
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
		if (word != "") {
			prefs.setLastWord("");

			// @todo: push the word to the text field
		}
	}


	private String getSurroundingWord() {
		CharSequence before = currentInputConnection.getTextBeforeCursor(50, 0);
		CharSequence after = currentInputConnection.getTextAfterCursor(50, 0);
		int bounds = -1;
		if (!TextUtils.isEmpty(before)) {
			bounds = before.length() -1;
			while (bounds > 0 && !Character.isWhitespace(before.charAt(bounds))) {
				bounds--;
			}
			before = before.subSequence(bounds, before.length());
		}
		if (!TextUtils.isEmpty(after)) {
			bounds = 0;
			while (bounds < after.length() && !Character.isWhitespace(after.charAt(bounds))) {
				bounds++;
			}
			after = after.subSequence(0, bounds);
		}
		return before.toString().trim() + after.toString().trim();
	}


	// sanitize lang and set index for cycling lang
	// Need to check if last lang is available, if not, set index to -1 and set lang to default to 0
	private LANGUAGE sanitizeLang(LANGUAGE lang) {
		return null;
		// mLangIndex = 0;
		// if (mLangsAvailable.length < 1 || lang == LANGUAGE.NONE) {
		// 	Log.e("T9.sanitizeLang", "This shouldn't happen.");
		// 	return mLangsAvailable[0];
		// }
		// else {
		// 	int index = LangHelper.findIndex(mLangsAvailable, lang);
		// 	mLangIndex = index;
		// 	return mLangsAvailable[index];
		// }
	}


	protected void nextKeyMode() {
		// @todo: commit current text

		// @todo: select next mode

		updateStatusIcon();
	}


	private void nextLang() {
		// @todo: commit current text

		// @todo: select next language

		updateStatusIcon();
	}


	/**
	 * updateStatusIcon
	 * Set the status icon that is appropriate in current mode (based on
	 * openwmm-legacy)
	 *
	 * @return void
	 */
	private void updateStatusIcon() {
/*		switch (mKeyMode) {
			case T9Preferences.MODE_ABC:
				showStatusIcon(LangHelper.ICONMAP[mLang.index][mKeyMode][mCapsMode]);
				break;
			case T9Preferences.MODE_PREDICTIVE:
				showStatusIcon(LangHelper.ICONMAP[mLang.index][mKeyMode][mCapsMode]);
				break;
			case T9Preferences.MODE_123:
				showStatusIcon(R.drawable.ime_number);
				break;
			default:
				Log.i("updateStatusIcon", "Unknown key mode. Hiding status icon.");
				hideStatusIcon();
				break;
		}*/
	}


	protected void showAddWord() {
/*		if (mKeyMode == T9Preferences.MODE_PREDICTIVE) {
			// decide if we are going to look for work to base on
			String template = mComposing.toString();
			if (template.length() == 0) {
				//get surrounding word:
				template = getSurroundingWord();
			}
			Log.d("showAddWord", "WORD: "+template);
			Intent awintent = new Intent(this, AddWordAct.class);
			awintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			awintent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			awintent.putExtra("io.github.sspanak.tt9.word", template);
			awintent.putExtra("io.github.sspanak.tt9.lang", mLang.id);
			clearState();
			currentInputConnection.setComposingText("", 0);
			currentInputConnection.finishComposingText();
			updateCandidates();
			//onFinishInput();
			mWordFound = true;
			startActivity(awintent);
		}*/
	}


	protected void showSymbolPage() {
		if (mSymbolPopup == null) {
			mSymbolPopup = new SymbolDialog(this, getLayoutInflater().inflate(R.layout.symbolview,
					null));
		}
		mSymbolPopup.doShow(getWindow().getWindow().getDecorView());
	}


	protected void showSmileyPage() {
		if (mSmileyPopup == null) {
			mSmileyPopup = new SmileyDialog(this, getLayoutInflater().inflate(R.layout.symbolview,
					null));
		}
		mSmileyPopup.doShow(getWindow().getWindow().getDecorView());
	}


	public void showPreferencesScreen() {
		Intent awintent = new Intent(this, TraditionalT9Settings.class);
		awintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		awintent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		if (interfacehandler != null) {
			interfacehandler.setPressedInUI(KeyEvent.KEYCODE_SOFT_LEFT, false);
		}
		hideWindow();
		startActivity(awintent);
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
