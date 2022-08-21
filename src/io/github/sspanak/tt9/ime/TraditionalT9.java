package io.github.sspanak.tt9.ime;

import android.os.Build;
import android.util.Log;
import android.view.View;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.PreferenceValidator;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.preferences.T9Preferences;

import java.util.ArrayList;
import java.util.List;

public class TraditionalT9 extends KeyPadHandler {
	private SoftKeyHandler softKeyHandler = null;
	private View softKeyView = null;

	protected Language mLanguage;
	protected ArrayList<Integer> mEnabledLanguages;


	private void loadPreferences() {
		mLanguage = LanguageCollection.getLanguage(prefs.getInputLanguage());
		mEnabledLanguages = prefs.getEnabledLanguageIds();

		// @todo: get the input mode and case as well
	}

	private void validatePreferences() {
		mEnabledLanguages = PreferenceValidator.validateEnabledLanguages(prefs, mEnabledLanguages);
		mLanguage = PreferenceValidator.validateLanguage(prefs, mLanguage, mEnabledLanguages);
	}


	protected void onInit() {
		if (softKeyHandler == null) {
			softKeyHandler = new SoftKeyHandler(getLayoutInflater().inflate(R.layout.mainview, null), this);
		}

		loadPreferences();
		validatePreferences();
	}


	protected void onRestart() {
		// in case we are back from Preferences screen, update the language list
		mEnabledLanguages = prefs.getEnabledLanguageIds();
		validatePreferences();

		// reset all UI elements
		setSuggestions(null);
		UI.updateStatusIcon(this, mLanguage, mInputMode, mCapsMode);
		displaySoftKeyMenu();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && mEditing != EDITING_STRICT_NUMERIC) {
			requestShowSelf(1);
		}

		// @todo: handle word adding
	}


	protected void onFinish() {
		setSuggestions(null);
		if (currentInputConnection != null) {
			currentInputConnection.setComposingText("", 0);
			// currentInputConnection.finishComposingText();
		}

		// @todo: clear previous word

		hideStatusIcon();
		hideWindow();

		if (softKeyView != null) {
			softKeyView.setVisibility(View.GONE);
		}
	}


	public boolean onBackspace() {
		if (!InputFieldHelper.isThereText(currentInputConnection)) {
			Log.d("handleBackspace", "backspace ignored");
			return false;
		}

		commitCurrentSuggestion();
		// @todo: typing in the dial field behaves incorrectly after BACKSPACE
		// 				check if this is the best way of deleting text.
		currentInputConnection.deleteSurroundingText(1, 0);

		Log.d("handleBackspace", "backspace handled");
		return true;
	}


	public boolean onOK() {
		Log.d("handleBackspace", "enter handler");

		if (!isInputViewShown()) {
			showWindow(true);
			displaySoftKeyMenu();
		}

		commitCurrentSuggestion();
		return !isSuggestionViewHidden();
	}


	protected boolean onUp() {
		return previousSuggestion();
	}


	protected boolean onDown() {
		return nextSuggestion();
	}


	protected boolean on0(boolean hold) {
		commitCurrentSuggestion();

		if (!hold && nextSuggestionInModeAbc()) {
			return true;
		}

		setSuggestions(
			mInputMode == T9Preferences.MODE_ABC && !hold ? mLanguage.getKeyCharacters(0): null,
			0
		);

		if (hold) {
			String chr = mInputMode == T9Preferences.MODE_123 ? "+" : "0";
			currentInputConnection.commitText(chr, 1);
		} else if (mInputMode == T9Preferences.MODE_PREDICTIVE) {
			currentInputConnection.commitText(" ", 1);
		} else if (mInputMode == T9Preferences.MODE_123) {
			currentInputConnection.commitText("0", 1);
		}

		return true;
	}


	protected boolean on1(boolean hold) {
		if (mInputMode == T9Preferences.MODE_123) {
			return false;
		}

		commitCurrentSuggestion();

		if (!hold && nextSuggestionInModeAbc()) {
			return true;
		}

		if (hold) {
			Log.d("on1", "showSymbolDialog is broken!");
			// @todo: UI.showSymbolDialog(this); // it is broken
		} else {
			setSuggestions(mLanguage.getKeyCharacters(1), 0);
		}

		return true;
	}


	protected boolean on2to9(int key, boolean hold) {
		if (mInputMode == T9Preferences.MODE_123) {
			return false;
		}

		if (hold) {
			commitCurrentSuggestion();
			currentInputConnection.commitText(String.valueOf(key), 1);
			return true;
		} else if (nextSuggestionInModeAbc()) {
			return true;
		} else if (mInputMode == T9Preferences.MODE_ABC) {
			commitCurrentSuggestion();
			setSuggestions(mLanguage.getKeyCharacters(key, mCapsMode == T9Preferences.CASE_LOWER), 0);
			return true;
		}

		return false;
	}


	protected boolean onPound() {
		currentInputConnection.commitText("#", 1);
		return true;
	}


	protected boolean onStar() {
		currentInputConnection.commitText("*", 1);
		return true;
	}


	protected boolean onKeyInputMode(boolean hold) {
		if (hold) {
			nextLang();
		} else {
			nextKeyMode();
		}

		return true;
	}


	protected boolean onKeyOtherAction(boolean hold) {
		if (hold) {
			UI.showPreferencesScreen(this);
		} else if (mInputMode == T9Preferences.MODE_PREDICTIVE) {
			showAddWord();
		}

		return true;
	}


	protected boolean isSuggestionViewHidden() {
		return mSuggestionView == null || !mSuggestionView.isShown();
	}


	private boolean previousSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		mSuggestionView.scrollToSuggestion(-1);

		// @todo: also add composing text to the input connection?

		return true;
	}


	private boolean nextSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		mSuggestionView.scrollToSuggestion(1);

		// @todo: also add composing text to the input connection?

		return true;
	}


	private boolean nextSuggestionInModeAbc() {
		return isNumKeyRepeated && mInputMode == T9Preferences.MODE_ABC && nextSuggestion();
	}

	/**
	 * Helper function to commit any text being composed in to the editor.
	 */
	private void commitCurrentSuggestion() {
		if (currentInputConnection != null && !isSuggestionViewHidden()) {
			String word = mSuggestionView.getCurrentSuggestion();
			currentInputConnection.commitText(word, word.length());
		}

		setSuggestions(null);
	}


	protected void setSuggestions(List<String> suggestions) {
		setSuggestions(suggestions, 0);
	}

	protected void setSuggestions(List<String> suggestions, int initialSel) {
		if (mSuggestionView == null) {
			return;
		}

		boolean show = suggestions != null && suggestions.size() > 0;

		mSuggestionView.setSuggestions(suggestions, initialSel);
		setCandidatesViewShown(show);
	}


	private void nextKeyMode() {
		if (mEditing == EDITING_STRICT_NUMERIC) {
			setSuggestions(null);
			mInputMode = T9Preferences.MODE_123;
		}
		// when typing a word or viewing scrolling the suggestions, only change the case
		else if (!isSuggestionViewHidden()) {
			determineAllowedCapsModes();

			int modeIndex = (allowedCapsModes.indexOf(mCapsMode) + 1) % allowedCapsModes.size();
			mCapsMode = allowedCapsModes.get(modeIndex);

			mSuggestionView.changeCase(mCapsMode, mLanguage.getLocale());
		}
		// make "abc" and "ABC" separate modes from user perspective
		else if (mInputMode == T9Preferences.MODE_ABC && mCapsMode == T9Preferences.CASE_LOWER) {
			mCapsMode = T9Preferences.CASE_UPPER;
		} else {
			int modeIndex = (allowedEditingModes.indexOf(mInputMode) + 1) % allowedEditingModes.size();
			mInputMode = allowedEditingModes.get(modeIndex);

			mCapsMode = mInputMode == T9Preferences.MODE_PREDICTIVE ? T9Preferences.CASE_CAPITALIZE : T9Preferences.CASE_LOWER;
		}

		// @todo: save the key mode and the caps mode

		UI.updateStatusIcon(this, mLanguage, mInputMode, mCapsMode);
	}


	private void nextLang() {
		if (mEditing == EDITING_STRICT_NUMERIC) {
			return;
		}

		setSuggestions(null);

		// select the next language
		int previousLangId = mEnabledLanguages.indexOf(mLanguage.getId());
		int nextLangId = previousLangId == -1 ? 0 : (previousLangId + 1) % mEnabledLanguages.size();
		mLanguage = LanguageCollection.getLanguage(mEnabledLanguages.get(nextLangId));

		validatePreferences();

		// save it for the next time
		prefs.setInputLanguage(mLanguage.getId());

		UI.updateStatusIcon(this, mLanguage, mInputMode, mCapsMode);
	}


	private void showAddWord() {
		setSuggestions(null);

		String template = "";

		// @todo: get the current word template from the input connection
		// template = getSurroundingWord();

		UI.showAddWordDialog(this, mLanguage.getId(), template);
	}


	private void restoreLastWordIfAny() {
		// mAddingWord = false;
		String word = prefs.getLastWord();
		if (word.equals("")) {
			prefs.setLastWord("");

			// @todo: push the word to the text field
		}
	}

	/**
	 * createSoftKeyView
	 * Generates the actual UI of TT9.
	 */
	protected View createSoftKeyView() {
		if (softKeyView == null) {
			softKeyView = getLayoutInflater().inflate(R.layout.mainview, null);
		}
		softKeyHandler.changeView(softKeyView);
		return softKeyView;
	}


	private void displaySoftKeyMenu() {
		createSoftKeyView();
		softKeyView.setVisibility(View.VISIBLE);
	}
}
