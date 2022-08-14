package io.github.sspanak.tt9.ime;

import android.util.Log;

import io.github.sspanak.tt9.languages.Punctuation;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.preferences.T9Preferences;

import java.util.List;

public class TraditionalT9 extends KeyPadHandler {
	public boolean onBackspace() {
		if (!InputFieldHelper.isThereText(currentInputConnection)) {
			Log.d("handleBackspace", "backspace ignored");
			return false;
		}

		commitCurrentCandidate();
		setCandidates(null);
		currentInputConnection.deleteSurroundingText(1, 0);

		Log.d("handleBackspace", "backspace handled");
		return true;
	}


	public boolean onOK() {
		Log.d("handleBackspace", "enter handler");

		if (!isInputViewShown()) {
			showWindow(true);
		}

		commitCurrentCandidate();
		return !isCandidateViewHidden();
	}


	protected boolean onUp() {
		return previousCandidate();
	}


	protected boolean onDown() {
		return nextCandidate();
	}


	protected boolean on0(boolean hold) {
		if (mInputMode == T9Preferences.MODE_123) {
			String chr = hold ? "+" : "0";
			currentInputConnection.commitText(chr, 1);
			return true;
		}

		return false;
	}


	protected boolean on1(boolean hold) {
		if (mInputMode == T9Preferences.MODE_123) {
			return true;
		}

		if (hold) {
			Log.d("on1", "showSymbolDialog is broken!");
			// @todo: UI.showSymbolDialog(this); // it is broken
		} else {
			setCandidates(Punctuation.getPunctuation(), 0);
		}

		return true;
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
		// @todo: sort out number-only and non-predictive fields

		if (hold) {
			nextLang();
		} else {
			nextKeyMode();
			// @todo: if in predictive mode and composing a word, change the case only
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


	protected boolean isCandidateViewHidden() {
		return mCandidateView == null || !mCandidateView.isShown();
	}


	private boolean nextCandidate() {
		if (isCandidateViewHidden()) {
			return false;
		}

		mCandidateView.scrollToSuggestion(1);
		return true;
	}


	private boolean previousCandidate() {
		if (isCandidateViewHidden()) {
			return false;
		}

		mCandidateView.scrollToSuggestion(-1);
		return true;
	}


	/**
	 * Helper function to commit any text being composed in to the editor.
	 */
	private void commitCurrentCandidate() {
		if (currentInputConnection != null && !isCandidateViewHidden()) {
			String word = mCandidateView.getCurrentSuggestion();
			currentInputConnection.commitText(word, word.length());
		}

		setCandidates(null);
	}


	protected void setCandidates(List<String> suggestions) {
		setCandidates(suggestions, 0);
	}

	protected void setCandidates(List<String> suggestions, int initialSel) {
		if (mCandidateView == null) {
			return;
		}

		boolean show = suggestions != null && suggestions.size() > 0;

		mCandidateView.setSuggestions(suggestions, initialSel);
		setCandidatesViewShown(show);
	}


	private void nextKeyMode() {
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

		UI.updateStatusIcon(this, mInputMode, mCapsMode);
	}


	private void nextLang() {
		// @todo: commit current text

		// @todo: select next language
		Log.d("nextLang", "current language: " + mLanguage + ". Selecting next");

		UI.updateStatusIcon(this, mInputMode, mCapsMode);
	}


	private void showAddWord() {
		clearState();
		currentInputConnection.setComposingText("", 0);
		currentInputConnection.finishComposingText();

		String template = "";

		// @todo: get the current word template from the input connection
		// template = getSurroundingWord();

		UI.showAddWordDialog(this, mLanguage, template);
	}

	protected void restoreLastWordIfAny() {
		// mAddingWord = false;
		String word = prefs.getLastWord();
		if (word.equals("")) {
			prefs.setLastWord("");

			// @todo: push the word to the text field
		}
	}
}
