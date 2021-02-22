package org.nyanya.android.traditionalt9;

import android.content.Intent;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import org.nyanya.android.traditionalt9.LangHelper.LANGUAGE;
import org.nyanya.android.traditionalt9.T9DB.DBSettings.SETTING;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class TraditionalT9 extends InputMethodService implements
		KeyboardView.OnKeyboardActionListener {

	private CandidateView mCandidateView;
	private InterfaceHandler interfacehandler = null;

	private StringBuilder mComposing = new StringBuilder();
	private StringBuilder mComposingI = new StringBuilder();

	private ArrayList<String> mSuggestionStrings = new ArrayList<String>(10);
	private ArrayList<Integer> mSuggestionInts = new ArrayList<Integer>(10);
	private AbstractList<String> mSuggestionSym = new ArrayList<String>(16);

	private static final int NON_EDIT = 0;
	private static final int EDITING = 1;
	private static final int EDITING_NOSHOW = 2;
	private int mEditing = NON_EDIT;

	private boolean mGaveUpdateWarn = false;

	private boolean mFirstPress = false;
	private boolean keyRemap = false;

	private boolean spaceOnZero = false;

	private boolean mIgnoreDPADKeyUp = false;
	private KeyEvent mDPADkeyEvent = null;

	protected boolean mWordFound = true;

	private AbsSymDialog mSymbolPopup = null;
	private AbsSymDialog mSmileyPopup = null;
	protected boolean mAddingWord = false;
	// private boolean mAddingSkipInput = false;
	private int mPrevious;
	private int mCharIndex;

	private String mPreviousWord = "";

	private int mCapsMode;
	private LANGUAGE mLang;
	private int mLangIndex;

	private LANGUAGE[] mLangsAvailable = null;

	private static final int CAPS_OFF = 0;
	private static final int CAPS_SINGLE = 1;
	private static final int CAPS_ALL = 2;
	private final static int[] CAPS_CYCLE = { CAPS_OFF, CAPS_SINGLE, CAPS_ALL };

	private final static int T9DELAY = 900;
	final Handler t9releasehandler = new Handler();
	Runnable mt9release = new Runnable() {
		@Override
		public void run() {
			commitReset();
		}
	};

	private T9DB db;

	public static final int MODE_LANG = 0;
	public static final int MODE_TEXT = 1;
	public static final int MODE_NUM = 2;
	private static final int[] MODE_CYCLE = { MODE_LANG, MODE_TEXT, MODE_NUM };
	private int mKeyMode;

	private InputConnection currentInputConnection = null;

	private Toast modeNotification = null;

	/**
	 * Main initialization of the input method component. Be sure to call to
	 * super class.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mPrevious = -1;
		mCharIndex = 0;
		db = T9DB.getInstance(this);

		if (interfacehandler == null) {
			interfacehandler = new InterfaceHandler(getLayoutInflater().inflate(R.layout.mainview,
					null), this);
		}
	}

	@Override
	public boolean onEvaluateInputViewShown() {
		//Log.d("T9.onEvaluateInputViewShown", "whatis");
		//Log.d("T9.onEval", "fullscreen?: " + isFullscreenMode() + " isshow?: " + isInputViewShown() + " isrequestedshow?: " + isShowInputRequested());
		if (mEditing == EDITING_NOSHOW) {
			return false;
		}
		// TODO: Verify if need this:
//		if (interfacehandler != null) {
//			interfacehandler.showView();
//		}
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
		//updateKeyMode();
		View v = getLayoutInflater().inflate(R.layout.mainview, null);
		interfacehandler.changeView(v);
		if (mKeyMode == MODE_LANG) {
			interfacehandler.showHold(true);
		} else {
			interfacehandler.showHold(false);
		}
		return v;
	}

	/**
	 * Called by the framework when your view for showing candidates needs to be
	 * generated, like {@link #onCreateInputView}.
	 */
	@Override
	public View onCreateCandidatesView() {
		mCandidateView = new CandidateView(this);
		return mCandidateView;
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

	private void clearState() {
		mSuggestionStrings.clear();
		mSuggestionInts.clear();
		mSuggestionSym.clear();
		mPreviousWord = "";
		mComposing.setLength(0);
		mComposingI.setLength(0);
		mWordFound = true;
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

	protected void showAddWord() {
		if (mKeyMode == MODE_LANG) {
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
			awintent.putExtra("org.nyanya.android.traditionalt9.word", template);
			awintent.putExtra("org.nyanya.android.traditionalt9.lang", mLang.id);
			clearState();
			currentInputConnection.setComposingText("", 0);
			currentInputConnection.finishComposingText();
			updateCandidates();
			//onFinishInput();
			mWordFound = true;
			startActivity(awintent);
		}
	}

	// sanitize lang and set index for cycling lang
	// Need to check if last lang is available, if not, set index to -1 and set lang to default to 0
	private LANGUAGE sanitizeLang(LANGUAGE lang) {
		mLangIndex = 0;
		if (mLangsAvailable.length < 1 || lang == LANGUAGE.NONE) {
			Log.e("T9.sanitizeLang", "This shouldn't happen.");
			return mLangsAvailable[0];
		}
		else {
			int index = LangHelper.findIndex(mLangsAvailable, lang);
			mLangIndex = index;
			return mLangsAvailable[index];
		}
	}
	/**
	 * This is the main point where we do our initialization of the input method
	 * to begin operating on an application. At this point we have been bound to
	 * the client, and are now receiving all of the detailed information about
	 * the target of our edits.
	 */
	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		currentInputConnection = getCurrentInputConnection();
		//Log.d("T9.onStartInput", "INPUTTYPE: " + attribute.inputType + " FIELDID: " + attribute.fieldId +
		//	" FIELDNAME: " + attribute.fieldName + " PACKAGE NAME: " + attribute.packageName);
		if (attribute.inputType == 0 || attribute.inputType == 3) {
			mLang = null;
			// don't do anything when not in any kind of edit field.
			// OR in dialer-type inputs. Hopefully OS will translate longpress 0 to +
			// should also turn off input screen and stuff
			mEditing = NON_EDIT;
			requestHideSelf(0);
			hideStatusIcon();
			// TODO: verify if need this
//			if (interfacehandler != null) {
//				interfacehandler.hideView();
//			}
			// Get keyMap setting:
			Object[] setting = db.getSettings(new SETTING[] {SETTING.KEY_REMAP} );
			keyRemap = setting[0].equals(1);
			return;
		}
		mFirstPress = true;
		mEditing = EDITING;
		// Reset our state. We want to do this even if restarting, because
		// the underlying state of the text editor could have changed in any
		// way.
		clearState();

		// get settings
		Object[] settings = db.getSettings(new SETTING[] {
			// 0, 1, 2,
			SETTING.LANG_SUPPORT, SETTING.LAST_LANG, SETTING.MODE_NOTIFY,
			// 3, 4, 5
			SETTING.INPUT_MODE, SETTING.LAST_WORD, SETTING.SPACE_ZERO
		});

		spaceOnZero = settings[5].equals(1);
		mLangsAvailable = LangHelper.buildLangs((Integer)settings[0]);
		mLang = sanitizeLang(LANGUAGE.get((Integer)settings[1]));

		updateCandidates();

		//TODO: Check if "restarting" variable will make things faster/more effecient

		mKeyMode = MODE_TEXT;

		boolean modenotify = settings[2].equals(1);

		if (!modenotify && modeNotification != null) {
			modeNotification = null;
		} else if (modenotify && modeNotification == null){
			modeNotification = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		}

		// We are now going to initialize our state based on the type of
		// text being edited.
		switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
			case InputType.TYPE_CLASS_NUMBER:
			case InputType.TYPE_CLASS_DATETIME:
				// Numbers and dates default to the symbols keyboard, with
				// no extra features.
				mKeyMode = MODE_NUM;
				break;

			case InputType.TYPE_CLASS_PHONE:
				// Phones will also default to the symbols keyboard, though
				// often you will want to have a dedicated phone keyboard.
				mKeyMode = MODE_NUM;
				break;

			case InputType.TYPE_CLASS_TEXT:
				// This is general text editing. We will default to the
				// normal alphabetic keyboard, and assume that we should
				// be doing predictive text (showing candidates as the
				// user types).
				mKeyMode = (Integer)settings[3];

				// We now look for a few special variations of text that will
				// modify our behavior.
				int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
				if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
						|| variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
					// Do not display predictions / what the user is typing
					// when they are entering a password.
					mKeyMode = MODE_TEXT;
				}

				if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
						|| variation == InputType.TYPE_TEXT_VARIATION_URI
						|| variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
					// Our predictions are not useful for e-mail addresses
					// or URIs.
					mKeyMode = MODE_TEXT;
				}

				if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
					// If this is an auto-complete text view, then our predictions
					// will not be shown and instead we will allow the editor
					// to supply their own. We only show the editor's
					// candidates when in fullscreen mode, otherwise relying
					// own it displaying its own UI.
					// ????
					mKeyMode = (Integer)settings[3];
				}

				// handle filter list cases... do not hijack DPAD center and make
				// sure back's go through proper
				if (variation ==  InputType.TYPE_TEXT_VARIATION_FILTER) {
					mEditing = EDITING_NOSHOW;
				}

				// We also want to look at the current state of the editor
				// to decide whether our alphabetic keyboard should start out
				// shifted.
				updateShiftKeyState(attribute);
				break;

			default:
				Log.d("onStartInput", "defaulting");
				// For all unknown input types, default to the alphabetic
				// keyboard with no special features.
				updateShiftKeyState(attribute);
		}
		// Special case for Softbank Sharp 007SH phone book.
		if (attribute.inputType == 65633) {
			mKeyMode = MODE_TEXT;
		}
		String prevword = null;
		if (attribute.privateImeOptions != null
				&& attribute.privateImeOptions.equals("org.nyanya.android.traditionalt9.addword=true")) {
			mAddingWord = true;
			// mAddingSkipInput = true;
			// Log.d("onStartInput", "ADDING WORD");
			mKeyMode = MODE_TEXT;
		} else {
			mAddingWord = false;
			// Log.d("onStartInput", "not adding word");
			prevword = (String)settings[4];
			if (prevword != null) {
				onText(prevword);
				db.storeSettingString(SETTING.LAST_WORD, null);
			}
			if (modenotify) {
				Resources r = getResources();
				if (mKeyMode != MODE_NUM)
					modeNotify(String.format("%s %s %s", r.getStringArray(R.array.pref_lang_titles)[mLang.index],
							r.getStringArray(R.array.keyMode)[mKeyMode], r.getStringArray(R.array.capsMode)[mCapsMode]));
				else
					modeNotify(String.format("%s %s", r.getStringArray(R.array.pref_lang_titles)[mLang.index],
							r.getStringArray(R.array.keyMode)[mKeyMode]));
			}
		}

		// Update the label on the enter key, depending on what the application
		// says it will do.
		// mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
		setSuggestions(null, -1);
		setCandidatesViewShown(false);
		mSuggestionStrings.clear();
		mSuggestionInts.clear();
		mSuggestionSym.clear();
		if (interfacehandler != null) {
			interfacehandler.midButtonUpdate(false);
		}

		updateKeyMode();
		// show Window()?
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
			db.storeSettingInt(SETTING.LAST_LANG, mLang.id);
			commitTyped();
			finish();
		}
	}

	// @Override public void onFinishInputView (boolean finishingInput) {
	// Log.d("onFinishInputView", "? " + finishingInput);
	// }

	private void finish() {
		// Log.d("finish", "why?");
		// Clear current composing text and candidates.
		pickSelectedCandidate(currentInputConnection);
		clearState();
		// updateCandidates();

		// We only hide the candidates window when finishing input on
		// a particular editor, to avoid popping the underlying application
		// up and down if the user is entering text into the bottom of
		// its window.
		// setCandidatesViewShown(false);

		// TODO: check this?
		mEditing = NON_EDIT;
		hideWindow();
		hideStatusIcon();
	}

	@Override
	public void onDestroy() {
		db.close();
		super.onDestroy();
	}

	// @Override public void onStartInputView(EditorInfo attribute, boolean
	// restarting) {
	// Log.d("onStartInputView", "attribute.inputType: " + attribute.inputType +
	// " restarting? " + restarting);
	// //super.onStartInputView(attribute, restarting);
	// }

	/**
	 * Deal with the editor reporting movement of its cursor.
	 */
	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd,
								  int candidatesStart, int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart,
				candidatesEnd);
		if (mKeyMode == MODE_TEXT) { return; } // stops the ghost fast-type commit
		// If the current selection in the text view changes, we should
		// clear whatever candidate text we have.
		if ((mComposing.length() > 0 || mComposingI.length() > 0)
				&& (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
			mComposing.setLength(0);
			mComposingI.setLength(0);
			updateCandidates();
			if (currentInputConnection != null) {
				currentInputConnection.finishComposingText();
			}
		}
	}

	/**
	 * This tells us about completions that the editor has determined based on
	 * the current text in it. We want to use this in fullscreen mode to show
	 * the completions ourself, since the editor can not be seen in that
	 * situation.
	 */
	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
		// ??????????????
	}

	private KeyEvent TranslateKey(int keyCode, KeyEvent event) {
		KeyEvent key = event;
		if (KeyMap.keyMapping.containsKey(keyCode)) {
			key = new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(),
					KeyMap.keyMapping.get(keyCode), event.getRepeatCount(), event.getMetaState(),
					event.getDeviceId(), 0, event.getFlags());
		}
		return key;
	}

	private boolean onKeyDown_(int keyCode, KeyEvent event) {
		//		Log.d("onKeyDown", "Key: " + event + " repeat?: " +
//				event.getRepeatCount() + " long-time: " + event.isLongPress());
		if (mEditing == NON_EDIT) {
			// // catch for UI weirdness on up event thing
			return false;
		}
		mFirstPress = false;

		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (interfacehandler != null) {
				interfacehandler.setPressed(keyCode, true);
			} // pass-through


			if (mEditing == EDITING_NOSHOW) {
				return false;
			}
			return handleDPAD(keyCode, event, true);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mEditing == EDITING_NOSHOW) {
				return false;
			}
			return handleDPAD(keyCode, event, true);
		} else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
			if (!isInputViewShown()) {
				return super.onKeyDown(keyCode, event);
			}

		} else if (keyCode == KeyEvent.KEYCODE_DEL) {// Special handling of the delete key: if we currently are
			// composing text for the user, we want to modify that instead
			// of let the application do the delete itself.
			// if (mComposing.length() > 0) {
			onKey(keyCode, null);
			return true;
			// }
			// break;
		}

		// only handle first press except for delete
		if (event.getRepeatCount() != 0) {
			return true;
		}
		if (mKeyMode == MODE_TEXT) {
			t9releasehandler.removeCallbacks(mt9release);
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {// The InputMethodService already takes care of the back
			// key for us, to dismiss the input method if it is shown.
			// but we will manage it ourselves because native Android handling
			// of the input view is ... flakey at best.
			// Log.d("onKeyDown", "back pres");
			return isInputViewShown();
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {// Let the underlying text editor always handle these.
			return false;

			// special case for softkeys
		} else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
			if (interfacehandler != null) {
				interfacehandler.setPressed(keyCode, true);
			}
			// pass-through


			event.startTracking();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1 || keyCode == KeyEvent.KEYCODE_2 ||
				keyCode == KeyEvent.KEYCODE_3 || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5 ||
				keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7 || keyCode == KeyEvent.KEYCODE_8 ||
				keyCode == KeyEvent.KEYCODE_9 || keyCode == KeyEvent.KEYCODE_POUND || keyCode == KeyEvent.KEYCODE_STAR) {
			event.startTracking();
			return true;
		} else {// KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD).getNumber(keyCode)
			// Log.w("onKeyDown", "Unhandled Key: " + keyCode + "(" +
			// event.toString() + ")");
		}
		Log.w("onKeyDown", "Unhandled Key: " + keyCode + "(" + event.toString() + ")");
		commitReset();
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * Use this to monitor key events being delivered to the application. We get
	 * first crack at them, and can either resume them or let them continue to
	 * the app.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Translate key
		KeyEvent key = TranslateKey(keyCode, event);
		if (key != null) {
			keyCode = key.getKeyCode();
			event = key;
		}

		if (!onKeyDown_(keyCode, event)) {
			if (key == null || !keyRemap) {
				return false;
			}
			else {
				// push key to lower level
				// dumb handling of null for edge weird timing cases.
				if (currentInputConnection == null)
					currentInputConnection = getCurrentInputConnection();
				if (currentInputConnection != null)
					currentInputConnection.sendKeyEvent(event);
			}
		}
		return true;
	}

	protected void launchOptions() {
		Intent awintent = new Intent(this, TraditionalT9Settings.class);
		awintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		awintent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		if (interfacehandler != null) {
			interfacehandler.setPressed(KeyEvent.KEYCODE_SOFT_RIGHT, false);
		}
		hideWindow();
		startActivity(awintent);
	}
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// consume since we will assume we have already handled the long press
		// if greater than 1
		if (event.getRepeatCount() != 1) {
			return true;
		}

		// Log.d("onLongPress", "LONG PRESS: " + keyCode);
		// HANDLE SPECIAL KEYS
		if (keyCode == KeyEvent.KEYCODE_POUND) {
			commitReset();
			// do default action or insert new line
			if (!sendDefaultEditorAction(true)) {
				onText("\n");
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_STAR) {
			if (mKeyMode != MODE_NUM) {
				if (mLangsAvailable.length > 1) {
					nextLang();
				} else {
					showSmileyPage(); // TODO: replace with lang select if lang thing
				}
				return true;
			}

		} else if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
			if (interfacehandler != null) {
				interfacehandler.setPressed(keyCode, false);
			}
			if (mKeyMode == MODE_LANG) {
				if (mWordFound) {
					showAddWord();
				} else {
					showSymbolPage();
				}
			}

		} else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
			if (interfacehandler != null) {
				interfacehandler.setPressed(keyCode, false);
			}
			launchOptions();
			// show Options
			return true;
		}
		if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
			if (mKeyMode == MODE_LANG) {
				commitTyped();
				onText(String.valueOf(keyCode - KeyEvent.KEYCODE_0));
			} else if (mKeyMode == MODE_TEXT) {
				commitReset();
				onText(String.valueOf(keyCode - KeyEvent.KEYCODE_0));
			} else if (mKeyMode == MODE_NUM) {
				if (keyCode == KeyEvent.KEYCODE_0) {
					onText("+");
				}
			}
		}
		return true;
	}

	private boolean onKeyUp_(int keyCode, KeyEvent event) {
	//		Log.d("onKeyUp", "Key: " + keyCode + " repeat?: " +
	//			event.getRepeatCount());
		if (mEditing == NON_EDIT) {
			// if (mButtonClose) {
			// //handle UI weirdness on up event
			// mButtonClose = false;
			// return true;
			// }
			// Log.d("onKeyDown", "returned false");
			return false;
		} else if (mFirstPress) {
			// to make sure changing between input UI elements works correctly.
			return super.onKeyUp(keyCode, event);
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (interfacehandler != null) {
				interfacehandler.setPressed(keyCode, false);
			}
			if (mEditing == EDITING_NOSHOW) {
				return false;
			}
			return handleDPAD(keyCode, event, false);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mEditing == EDITING_NOSHOW) {
				return false;
			}
			return handleDPAD(keyCode, event, false);
		} else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
			if (!isInputViewShown()) {
				return super.onKeyDown(keyCode, event);
			}
		}

		if (event.isCanceled()) {
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isInputViewShown()) {
				hideWindow();
				return true;
			}
			return false;
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			return false;

			// special case for softkeys
		} else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT || keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {// if (mAddingWord){
			// Log.d("onKeyUp", "key: " + keyCode + " skip: " +
			// mAddingSkipInput);
			// if (mAddingSkipInput) {
			// //mAddingSkipInput = false;
			// return true;
			// }
			// }
			if (interfacehandler != null) {
				interfacehandler.setPressed(keyCode, false);
			}
			// pass-through

			if (!isInputViewShown()) {
				showWindow(true);
			}
			onKey(keyCode, null);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1 || keyCode == KeyEvent.KEYCODE_2
				|| keyCode == KeyEvent.KEYCODE_3 || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5 ||
				keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7 || keyCode == KeyEvent.KEYCODE_8 ||
				keyCode == KeyEvent.KEYCODE_9 || keyCode == KeyEvent.KEYCODE_POUND || keyCode == KeyEvent.KEYCODE_STAR) {
			// if (!isInputViewShown()){
			// Log.d("onKeyUp", "showing window.");
			// //showWindow(true);
			// }
			if (!isInputViewShown()) {
				showWindow(true);
			}
			onKey(keyCode, null);
			return true;
		} else {// KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD).getNumber(keyCode)
			Log.w("onKeyUp", "Unhandled Key: " + keyCode + "(" + event.toString() + ")");
		}
		commitReset();
		return super.onKeyUp(keyCode, event);
	}
	/**
	 * Use this to monitor key events being delivered to the application. We get
	 * first crack at them, and can either resume them or let them continue to
	 * the app.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Translate key
		KeyEvent key = TranslateKey(keyCode, event);
		if (key != null) {
			keyCode = key.getKeyCode();
			event = key;
		}

		if (!onKeyUp_(keyCode, event)) {
			if (key == null || !keyRemap) {
				return false;
			}
			else {
				// push key to lower level
				// dumb handling of null for edge weird timing cases.
				if (currentInputConnection == null)
					currentInputConnection = getCurrentInputConnection();
				if (currentInputConnection != null)
					currentInputConnection.sendKeyEvent(event);
			}
		}
		return true;
	}

	/**
	 * Helper function to commit any text being composed in to the editor.
	 */
	// private void commitTyped() { commitTyped(getCurrentInputConnection()); }
	private void commitTyped() {
		if (interfacehandler != null) {
			interfacehandler.midButtonUpdate(false);
			interfacehandler.showNotFound(false);
		}

		pickSelectedCandidate(currentInputConnection);

		clearState();
		updateCandidates();
		setCandidatesViewShown(false);
	}

	/**
	 * Helper to update the shift state of our keyboard based on the initial
	 * editor state.
	 */
	private void updateShiftKeyState(EditorInfo attr) {
		// Log.d("updateShift", "CM start: " + mCapsMode);
		if (attr != null && mCapsMode != CAPS_ALL) {
			int caps = 0;
			if (attr.inputType != InputType.TYPE_NULL) {
				caps = currentInputConnection.getCursorCapsMode(attr.inputType);
			}
			// mInputView.setShifted(mCapsLock || caps != 0);
			// Log.d("updateShift", "caps: " + caps);
			if ((caps & TextUtils.CAP_MODE_CHARACTERS) == TextUtils.CAP_MODE_CHARACTERS) {
				mCapsMode = CAPS_ALL;
			} else if ((caps & TextUtils.CAP_MODE_SENTENCES) == TextUtils.CAP_MODE_SENTENCES) {
				mCapsMode = CAPS_SINGLE;
			} else if ((caps & TextUtils.CAP_MODE_WORDS) == TextUtils.CAP_MODE_WORDS) {
				mCapsMode = CAPS_SINGLE;
			} else {
				mCapsMode = CAPS_OFF;
			}
			updateKeyMode();
		}
		// Log.d("updateShift", "CM end: " + mCapsMode);
	}

	/**
	 * Helper to send a key down / key up pair to the current editor. NOTE: Not
	 * supposed to use this apparently. Need to use it for DEL. For other things
	 * I'll have to onText
	 */
	private void keyDownUp(int keyEventCode) {
		currentInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		currentInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}
	private void keyDownUp(String keys) {
		currentInputConnection.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), keys, 0, 0));
	}

	// Implementation of KeyboardViewListener
	@Override
	public void onKey(int keyCode, int[] keyCodes) {
		// Log.d("OnKey", "pri: " + keyCode);
		// Log.d("onKey", "START Cm: " + mCapsMode);
		// HANDLE SPECIAL KEYS
		if (keyCode == KeyEvent.KEYCODE_DEL) {
			handleBackspace();
		} else if (keyCode == KeyEvent.KEYCODE_STAR) {
			// change case
			if (mKeyMode == MODE_NUM) {
				handleCharacter(KeyEvent.KEYCODE_STAR);
			} else {
				handleShift();
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			handleClose();
		} else if (keyCode == KeyEvent.KEYCODE_POUND) {
			// space
			handleCharacter(KeyEvent.KEYCODE_POUND);
		} else if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
			if (mWordFound) {
				showSymbolPage();
			} else {
				showAddWord();
			}

		} else if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
			nextKeyMode();

		} else {
			if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
				handleCharacter(keyCode);
			} else {
				Log.e("onKey", "This shouldn't happen, unknown key");
			}
		}
		// Log.d("onKey", "END Cm: " + mCapsMode);
	}

	@Override
	public void onText(CharSequence text) {
		if (currentInputConnection == null)
			return;
		currentInputConnection.beginBatchEdit();
		if (mComposing.length() > 0 || mComposingI.length() > 0) {
			commitTyped();
		}
		currentInputConnection.commitText(text, 1);
		currentInputConnection.endBatchEdit();
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	/**
	 * Used from interface to either close the input UI if not composing text or
	 * to accept the composing text
	 */
	protected void handleMidButton() {
		if (!isInputViewShown()) {
			showWindow(true);
			return;
		}
		if (mComposing.length() > 0) {
			switch (mKeyMode) {
				case MODE_LANG:
					commitTyped();
					break;
				case MODE_TEXT:
					commitTyped();
					charReset();
					break;
				case MODE_NUM:
					// shouldn't happen
					break;
			}
		} else {
			hideWindow();
		}
	}

	/**
	 * Update the list of available candidates from the current composing text.
	 * Do a lot of complicated stuffs.
	 */
	private void updateCandidates() {
		updateCandidates(false);
	}
	private void updateCandidates(boolean backspace) {
		if (mKeyMode == MODE_LANG) {
			int len = mComposingI.length();
			if (len > 0) {
				if (mComposingI.charAt(len - 1) == '1') {
					boolean suggestions = !mSuggestionStrings.isEmpty();
					String prefix = "";
					if (mPreviousWord.length() == 0) {
						if (suggestions && !backspace) {
							prefix = mPreviousWord = mSuggestionStrings.get(mCandidateView.mSelectedIndex);
						}
					} else {
						if (backspace) {
							prefix = mPreviousWord;
						} else {
							if (suggestions) {
								if (mCandidateView.mSelectedIndex == -1) { mCandidateView.mSelectedIndex = 0; }
								prefix = mPreviousWord = mSuggestionStrings.get(mCandidateView.mSelectedIndex);
							} else {
								prefix = mPreviousWord;
							}
						}
					}
					mSuggestionInts.clear();
					mSuggestionStrings.clear();
					mSuggestionSym.clear();
					db.updateWords("1", mSuggestionSym, mSuggestionInts, mCapsMode, mLang);
					for (String a : mSuggestionSym) {
						if (!prefix.equals("")) {
							mSuggestionStrings.add(prefix + a);
						} else {
							mSuggestionStrings.add(String.valueOf(a));
							mComposingI.setLength(0);
							mComposingI.append("1");
						}
					}
				} else {
					db.updateWords(mComposingI.toString(), mSuggestionStrings, mSuggestionInts,
							mCapsMode, mLang);
				}
				if (!mSuggestionStrings.isEmpty()) {
					mWordFound = true;
					mComposing.setLength(0);
					mComposing.append(mSuggestionStrings.get(0));
					if (interfacehandler != null) {
						interfacehandler.showNotFound(false);
					}
				} else {
					mWordFound = false;
					mComposingI.setLength(len - 1);
					setCandidatesViewShown(false);
					if (interfacehandler != null) {
						interfacehandler.showNotFound(true);
					}
				}
				setSuggestions(mSuggestionStrings, 0);
				} else {
				setSuggestions(null, -1);
				setCandidatesViewShown(false);
				if (interfacehandler != null) {
					interfacehandler.showNotFound(false);
				}
			}
		} else if (mKeyMode == MODE_TEXT) {
			if (mComposing.length() > 0) {
				//Log.d("updateCandidates", "Previous: " + mComposing.toString());
				mSuggestionStrings.clear();

				char[] ca = CharMap.T9TABLE[mLang.index][mPrevious];
				for (char c : ca) {
					mSuggestionStrings.add(String.valueOf(c));
				}
				setSuggestions(mSuggestionStrings, mCharIndex);
				//Log.d("updateCandidates", "newSuggestedIndex: " + mCharIndex);
			} else {
				setSuggestions(null, -1);
			}
		}
	}

	private void setSuggestions(List<String> suggestions, int initialSel) {
		if (suggestions != null && suggestions.size() > 0) {
			setCandidatesViewShown(true);
		}
		if (mCandidateView != null) {
			mCandidateView.setSuggestions(suggestions, initialSel);
		}
	}

	private void handleBackspace() {
		final int length = mComposing.length();
		final int length2 = mComposingI.length();
		if (mKeyMode == MODE_TEXT) {
			charReset();
			if (interfacehandler != null) {
				interfacehandler.midButtonUpdate(false);
			}
			setCandidatesViewShown(false);
		}
		//Log.d("handleBS", "Stage1: (" + length + "," + length2 + ")");
		//Log.d("handleBS", "Stage1: (" + mComposingI.toString() + ")");
		if (length2 > 1) {
			if (mComposingI.charAt(length2 - 1) == '1') {
				// revert previous word
				mPreviousWord = mPreviousWord.substring(0, mPreviousWord.length() - 1);
			}
			mComposingI.delete(length2 - 1, length2);
			if (length2 - 1 > 1) {
				if (mComposingI.charAt(length2 - 2) != '1') {
					if (mComposingI.indexOf("1") == -1) {
						// no longer contains punctuation so we no longer care
						mPreviousWord = "";
					}
				}
			} else {
				mPreviousWord = "";
			}
			updateCandidates(true);
			currentInputConnection.setComposingText(mComposing, 1);
		} else if (length > 0 || length2 > 0) {
			//Log.d("handleBS", "resetting thing");
			mComposing.setLength(0);
			mComposingI.setLength(0);
			interfacehandler.midButtonUpdate(false);
			interfacehandler.showNotFound(false);
			mSuggestionStrings.clear();
			mPreviousWord = "";
			currentInputConnection.commitText("", 0);
			updateCandidates();
		} else {
			mPreviousWord = "";
			keyDownUp(KeyEvent.KEYCODE_DEL);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
		// Log.d("handleBS", "Cm: " + mCapsMode);
		// Why do I need to call this twice, android...
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	private void handleShift() {
		// do my own thing here
		if (mCapsMode == CAPS_CYCLE.length - 1) {
			mCapsMode = 0;
		} else {
			mCapsMode++;
		}

		if (mKeyMode == MODE_LANG && mComposing.length() > 0) {
			updateCandidates();
			currentInputConnection.setComposingText(mComposing, 1);
		}
		updateKeyMode();
		if (modeNotification != null)
			modeNotify(getResources().getStringArray(R.array.capsMode)[mCapsMode]);
	}

	/**
	 * handle input of a character. Precondition: ONLY 0-9 AND *# ARE ALLOWED
	 *
	 * @param keyCode
	 */
	private void handleCharacter(int keyCode) {
		switch (mKeyMode) {
			case MODE_LANG:
				// it begins
				// take note of spaceOnZero
				if (keyCode == KeyEvent.KEYCODE_POUND ||
						( spaceOnZero && (keyCode == KeyEvent.KEYCODE_0) )) {
					if (mComposing.length() > 0) {
						commitTyped();
					}
					onText(" ");
				} else {
					// do things
					if (interfacehandler != null) {
						interfacehandler.midButtonUpdate(true);
					}
					keyCode = keyCode - KeyEvent.KEYCODE_0;
					mComposingI.append(keyCode);
					updateCandidates();
					currentInputConnection.setComposingText(mComposing, 1);
				}
				break;

			case MODE_TEXT:
				t9releasehandler.removeCallbacks(mt9release);
				if (keyCode == KeyEvent.KEYCODE_POUND) {
					keyCode = 10;
				} else {
					keyCode = keyCode - KeyEvent.KEYCODE_0;
				}
				// special translation of that keyCode (which is now T9TABLE index
				if (spaceOnZero) {
					if (keyCode == 0)
						keyCode = 11;
					if (keyCode == 10)
						keyCode = 12;
				}
				//Log.d("handleChar", "Key: " + keyCode + "Previous Key: " + mPrevious + " Index:" + mCharIndex);

				boolean newChar = false;
				if (mPrevious == keyCode) {
					mCharIndex++;
				} else {
					//Log.d("handleChar", "COMMITING:" + mComposing.toString());
					commitTyped();
					// updateShiftKeyState(getCurrentInputEditorInfo());
					newChar = true;
					mCharIndex = 0;
					mPrevious = keyCode;
				}

				// start at caps if CapMode
				// Log.d("handleChar", "Cm: " + mCapsMode);
				if (mCharIndex == 0 && mCapsMode != CAPS_OFF) {
					mCharIndex = CharMap.T9CAPSTART[mLang.index][keyCode];
				}

				mComposing.setLength(0);
				mComposingI.setLength(0);
				char[] ca = CharMap.T9TABLE[mLang.index][keyCode];
				if (mCharIndex >= ca.length) {
					mCharIndex = 0;
				}
				//Log.d("handleChar", "Index: " + mCharIndex);
				mComposing.append(ca[mCharIndex]);
				//Log.d("handleChar", "settingCompose: " + mComposing.toString());
				currentInputConnection.setComposingText(mComposing, 1);

				t9releasehandler.postDelayed(mt9release, T9DELAY);
				if (newChar) {
					// consume single caps
					if (mCapsMode == CAPS_SINGLE) {
						mCapsMode = CAPS_OFF;
					}
				}
				updateCandidates();
				updateShiftKeyState(getCurrentInputEditorInfo());
				break;

			case MODE_NUM:
				if (keyCode == KeyEvent.KEYCODE_POUND) {
					onText("#");
				} else if (keyCode == KeyEvent.KEYCODE_STAR) {
					onText("*");
				} else {
					onText(String.valueOf(keyCode - KeyEvent.KEYCODE_0));
				}
				break;
			default:
				Log.e("handleCharacter", "Unknown input?");
		}
	}

	// This is a really hacky way to handle DPAD long presses in a way that we can pass them on to
	// the underlying edit box in a somewhat reliable manner.
	// (somewhat because there are a few cases where this doesn't work properly or acts strangely.)
	private boolean handleDPAD(int keyCode, KeyEvent event, boolean keyDown) {
		// Log.d("handleConsumeDPAD", "keyCode: " + keyCode + " isKeyDown: " +
		// isKeyDown);
		if (keyDown) {
			// track key, if seeing repeat count < 0, start sending this event
			// and previous to super
			if (event.getRepeatCount() == 0) {
				// store event
				mDPADkeyEvent = event;
				return true;
			} else {
				if (mIgnoreDPADKeyUp) {
					// pass events to super
					return super.onKeyDown(keyCode, event);
				} else {
					// pass previous event and future events to super
					mIgnoreDPADKeyUp = true;
					currentInputConnection.sendKeyEvent(mDPADkeyEvent);
					return super.onKeyDown(keyCode, event);
				}
			}
		} else {
			// if we have been sending previous down events to super, do the
			// same for up, else process the event
			if (mIgnoreDPADKeyUp) {
				mIgnoreDPADKeyUp = false;
				return super.onKeyUp(keyCode, event);
			} else {
				if (mKeyMode != MODE_NUM && mComposing.length() > 0) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						mCandidateView.scrollSuggestion(1);
						if (mSuggestionStrings.size() > mCandidateView.mSelectedIndex)
							currentInputConnection.setComposingText(mSuggestionStrings.get(mCandidateView.mSelectedIndex), 1);
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						mCandidateView.scrollSuggestion(-1);
						if (mSuggestionStrings.size() > mCandidateView.mSelectedIndex)
							currentInputConnection.setComposingText(mSuggestionStrings.get(mCandidateView.mSelectedIndex), 1);
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						if (mKeyMode == MODE_LANG) {
							commitTyped();
						} else if (mKeyMode == MODE_TEXT) {
							commitReset();
						}
						// getCurrentInputConnection().sendKeyEvent(mDPADkeyEvent);
						// return super.onKeyUp(keyCode, event);
						return true;
					}
				}
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					handleMidButton();
					return true;
				} else {// Send stored event to input connection then pass current
					// event onto super
					currentInputConnection.sendKeyEvent(mDPADkeyEvent);
					return super.onKeyUp(keyCode, event);
				}
			}
		}
	}

	private void commitReset() {
		commitTyped();
		charReset();
		if (mCapsMode == CAPS_SINGLE) {
			mCapsMode = CAPS_OFF;
		}
		// Log.d("commitReset", "CM pre: " + mCapsMode);
		updateShiftKeyState(getCurrentInputEditorInfo());
		// Log.d("commitReset", "CM post: " + mCapsMode);
	}

	private void charReset() {
		t9releasehandler.removeCallbacks(mt9release);
		mPrevious = -1;
		mCharIndex = 0;
	}

	private void handleClose() {
		commitTyped();
		requestHideSelf(0);
	}

	protected void nextKeyMode() {
		if (mKeyMode == MODE_CYCLE.length - 1) {
			mKeyMode = 0;
		}
		else {
			mKeyMode++;
		}
		updateKeyMode();
		resetKeyMode();
		if (modeNotification != null)
			modeNotify(getResources().getStringArray(R.array.keyMode)[mKeyMode]);
	}

	private void modeNotify(String s) {
		modeNotification.setText(s);
		modeNotification.show();
		modeNotification.cancel(); 	// TODO: This will not always hide the Toast.
									// will probably need to implement custom view
	}

	private void nextLang() {
		mLangIndex++;
		if (mLangIndex == mLangsAvailable.length) {
			mLangIndex = 0;
		}
		mLang = mLangsAvailable[mLangIndex];
		updateKeyMode();
		if (modeNotification != null) {
			modeNotify(getResources().getStringArray(R.array.pref_lang_titles)[mLang.index]);
		}
	}

	private void resetKeyMode() {
		charReset();
		if (mKeyMode != MODE_NUM) {
			commitTyped();
		}
		mComposing.setLength(0);
		mComposingI.setLength(0);
		currentInputConnection.finishComposingText();
	}

	/**
	 * Set the status icon that is appropriate in current mode (based on
	 * openwmm-legacy)
	 */
	private void updateKeyMode() {
		int icon = 0;

		switch (mKeyMode) {
			case MODE_TEXT:
				interfacehandler.showHold(false);
				icon = LangHelper.ICONMAP[mLang.index][mKeyMode][mCapsMode];
				break;
			case MODE_LANG:
				if (!db.ready) {
					if (!mGaveUpdateWarn) {
						Toast.makeText(this, getText(R.string.updating_database_unavailable), Toast.LENGTH_LONG).show();
						mGaveUpdateWarn = true;
					}
					nextKeyMode();
					return;
				}
				if (mLangIndex == -1) {
					nextKeyMode();
					return;
				}
				if (mAddingWord) {
					interfacehandler.showHold(false);
				} else {
					interfacehandler.showHold(true);
				}
				//Log.d("T9.updateKeyMode", "lang: " + mLang + " mKeyMode: " + mKeyMode + " mCapsMode"
				// + mCapsMode);
				icon = LangHelper.ICONMAP[mLang.index][mKeyMode][mCapsMode];
				break;
			case MODE_NUM:
				interfacehandler.showHold(false);
				icon = R.drawable.ime_number;
				break;
			default:
				Log.e("updateKeyMode", "How.");
				break;
		}
		showStatusIcon(icon);
	}

	private void pickSelectedCandidate(InputConnection ic) {
		pickSuggestionManually(-1, ic);
	}

	private void pickSuggestionManually(int index, InputConnection ic) {
		// Log.d("pickSuggestMan", "Doing");
		if (mComposing.length() > 0 || mComposingI.length() > 0) {
			// If we were generating candidate suggestions for the current
			// text, we would commit one of them here. But for this sample,
			// we will just commit the current text.
			if (!mSuggestionStrings.isEmpty()) {
				if (index < 0) {
					// Log.d("pickSuggestMan", "picking SELECTED: " +
					// mSuggestionStrings.get(mCandidateView.mSelectedIndex));
					// get and commit selected suggestion
					ic.commitText(mSuggestionStrings.get(mCandidateView.mSelectedIndex), 1);
					if (mKeyMode == MODE_LANG) {
						// update freq
						db.incrementWord(mSuggestionInts.get(mCandidateView.mSelectedIndex));
					}
				} else {
					// commit suggestion index
					ic.commitText(mSuggestionStrings.get(index), 1);
					if (mKeyMode == MODE_LANG) {
						db.incrementWord(mSuggestionInts.get(index));
					}
				}
			}
		}
	}

	/**
	 * Ignore this for now.
	 */
	@Override
	public void swipeRight() {
		// if (mPredictionOn) {
		// pickDefaultCandidate();
		// }
	}

	@Override
	public void swipeLeft() {
		handleBackspace();
	}

	@Override
	public void swipeDown() {
		handleClose();
	}

	@Override
	public void swipeUp() {
	}

	@Override
	public void onPress(int primaryCode) {
	}

	@Override
	public void onRelease(int primaryCode) {
	}

}
