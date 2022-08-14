package io.github.sspanak.tt9.ime;

import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.T9DB;
import io.github.sspanak.tt9.languages.Punctuation;
import io.github.sspanak.tt9.ui.CandidateView;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.preferences.T9Preferences;

import java.util.List;

public class TraditionalT9 extends KeyPadHandler {


	protected boolean handle0(boolean hold) {
		if (mInputMode == T9Preferences.MODE_123) {
			String chr = hold ? "+" : "0";
			currentInputConnection.commitText(chr, 1);
			return true;
		}

		return false;
	}


	protected boolean handle1(boolean hold) {
		if (mInputMode == T9Preferences.MODE_123) {
			return true;
		}

		if (hold) {
			UI.showSymbolDialog(this);
		} else {
			setCandidates(Punctuation.getPunctuation(), 0);
		}

		return true;
	}


	public boolean handleOK() {
		Log.d("handleBackspace", "enter handler");

		if (!isInputViewShown()) {
			showWindow(true);
		}

		commitCurrentCandidate();
		return !isCandidateViewHidden();
	}


	public boolean handleBackspace() {
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


	protected boolean nextCandidate() {
		if (isCandidateViewHidden()) {
			return false;
		}

		mCandidateView.scrollToSuggestion(1);
		return true;
	}


	protected boolean previousCandidate() {
		if (isCandidateViewHidden()) {
			return false;
		}

		mCandidateView.scrollToSuggestion(-1);
		return true;
	}


	/**
	 * Helper function to commit any text being composed in to the editor.
	 */
	protected void commitCurrentCandidate() {
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

		UI.updateStatusIcon(this, mInputMode, mCapsMode);
	}


	protected void nextLang() {
		// @todo: commit current text

		// @todo: select next language
		Log.d("nextLang", "current language: " + mLanguage + ". Selecting next");

		UI.updateStatusIcon(this, mInputMode, mCapsMode);
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

	protected void restoreLastWordIfAny() {
		// mAddingWord = false;
		String word = prefs.getLastWord();
		if (word.equals("")) {
			prefs.setLastWord("");

			// @todo: push the word to the text field
		}
	}
}
