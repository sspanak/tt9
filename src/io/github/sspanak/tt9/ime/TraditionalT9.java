package io.github.sspanak.tt9.ime;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;

import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.db.InsertBlankWordException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.Punctuation;
import io.github.sspanak.tt9.preferences.T9Preferences;
import io.github.sspanak.tt9.ui.UI;

public class TraditionalT9 extends KeyPadHandler {
	private SoftKeyHandler softKeyHandler = null;
	private View softKeyView = null;

	private String predictionSequence = "";

	protected Language mLanguage;
	protected ArrayList<Integer> mEnabledLanguages;


	private void loadPreferences() {
		mLanguage = LanguageCollection.getLanguage(prefs.getInputLanguage());
		mEnabledLanguages = prefs.getEnabledLanguages();
		mInputMode = prefs.getInputMode();
		mTextCase = prefs.getTextCase();
	}

	private void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(prefs, mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(prefs, mLanguage, mEnabledLanguages);
	}

	private void validatePreferences() {
		validateLanguages();
		mInputMode = InputModeValidator.validateMode(prefs, mInputMode, allowedInputModes);
		mTextCase = InputModeValidator.validateTextCase(prefs, mTextCase, allowedTextCases);
	}


	protected void onInit() {
		if (softKeyHandler == null) {
			softKeyHandler = new SoftKeyHandler(getLayoutInflater().inflate(R.layout.mainview, null), this);
		}

		loadPreferences();
	}


	protected void onRestart() {
		// in case we are back from Preferences screen, update the language list
		mEnabledLanguages = prefs.getEnabledLanguages();
		validatePreferences();

		// reset all UI elements
		predictionSequence = "";
		clearSuggestions();
		UI.updateStatusIcon(this, mLanguage, mInputMode, mTextCase);
		displaySoftKeyMenu();
		if (!isInputViewShown()) {
			showWindow(true);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && mEditing != EDITING_STRICT_NUMERIC && mEditing != EDITING_DIALER) {
			requestShowSelf(1);
		}

		// @todo: handle word adding
	}


	protected void onFinish() {
		predictionSequence = "";
		clearSuggestions();

		// @todo: clear previous word

		hideStatusIcon();
		hideWindow();

		if (softKeyView != null) {
			softKeyView.setVisibility(View.GONE);
		}
	}


	public boolean onBackspace() {
		if (!InputFieldHelper.isThereText(currentInputConnection)) {
			Logger.d("onBackspace", "backspace ignored");
			return false;
		}

		resetKeyRepeat();

		if (mInputMode == T9Preferences.MODE_PREDICTIVE && predictionSequence.length() > 0) {
			predictionSequence = predictionSequence.substring(0, predictionSequence.length() - 1);
			applyPredictionSequence();
		} else {
			commitCurrentSuggestion();
			super.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
		}

		Logger.d("onBackspace", "backspace handled");
		return true;
	}


	public boolean onOK() {
		Logger.d("onOK", "enter handler");

		acceptCurrentSuggestion();
		resetKeyRepeat();

		return !isSuggestionViewHidden();
	}


	protected boolean onUp() {
		return previousSuggestion();
	}


	protected boolean onDown() {
		return nextSuggestion();
	}


	protected boolean on0(boolean hold) {
		if (!hold && nextSuggestionInModeAbc()) {
			return true;
		}

		commitCurrentSuggestion();

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

	/**
	 * on1to9
	 *
	 * @param key   Must be a number from 1 to 9, not a "KeyEvent.KEYCODE_X"
	 * @param hold  If "true" we are calling the handler, because the key is being held.
	 * @return boolean
	 */
	protected boolean on1to9(int key, boolean hold) {
		if (mInputMode == T9Preferences.MODE_123) {
			return false;
		}

		if (hold) {
			commitCurrentSuggestion(); // commit the previous one before adding the "hold" character
			currentInputConnection.commitText(String.valueOf(key), 1);
			return true;
		} else if (wordInPredictiveMode(key)) {
			return true;
		} else if (emoticonInPredictiveMode(key)) {
			return true;
		} else if (nextSuggestionInModeAbc()) {
			return true;
		} else if (mInputMode == T9Preferences.MODE_ABC || mInputMode == T9Preferences.MODE_PREDICTIVE) {
			commitCurrentSuggestion(); // commit the previous one before suggesting the new one
			setSuggestions(mLanguage.getKeyCharacters(key, mTextCase == T9Preferences.CASE_LOWER));
			setComposingTextFromCurrentSuggestion();
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
		} else {
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

		String word = mSuggestionView.getCurrentSuggestion();
		currentInputConnection.setComposingText(word, word.length());

		return true;
	}


	private boolean nextSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		mSuggestionView.scrollToSuggestion(1);

		String word = mSuggestionView.getCurrentSuggestion();
		currentInputConnection.setComposingText(word, word.length());

		return true;
	}


	private boolean emoticonInPredictiveMode(int key) {
		if (key != 1 || mInputMode != T9Preferences.MODE_PREDICTIVE || !isNumKeyRepeated) {
			return false;
		}

		setSuggestions(Punctuation.Emoticons);
		setComposingTextFromCurrentSuggestion();

		return true;
	}


	private boolean wordInPredictiveMode(int key) {
		if (
			mInputMode != T9Preferences.MODE_PREDICTIVE ||
			// 0 is not a word, but space, so we handle it in on0().
			key == 0 ||
			// double 1 is not a word, but an emoticon and it is handled elsewhere
			(key == 1 && isNumKeyRepeated)
		) {
			return false;
		}


		if (
			// Punctuation is considered "a word", so that we can increase the priority as needed
			// Also, it must break the current word.
			(key == 1 && predictionSequence.length() > 0) ||
			// On the other hand, letters also "break" punctuation.
			(key != 1 && predictionSequence.endsWith("1"))
		) {
			acceptCurrentSuggestion();
		}

		predictionSequence += key;
		applyPredictionSequence();

		return true;
	}

	private final Handler handleSuggestions = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			ArrayList<String> suggestions = msg.getData().getStringArrayList("suggestions");
			suggestions = guessSuggestionsWhenNone(suggestions, mSuggestionView.getCurrentSuggestion());

			setSuggestions(suggestions);
			mSuggestionView.changeCase(mTextCase, mLanguage.getLocale());

			// Put the first suggestion in the text field,
			// but cut it off to the length of the sequence (how many keys were pressed),
			// for a more intuitive experience.
			String word = mSuggestionView.getCurrentSuggestion();
			word = mSuggestionView.getCurrentSuggestion().substring(0, Math.min(predictionSequence.length(), word.length()));
			currentInputConnection.setComposingText(word, word.length());
		}
	};

	private void applyPredictionSequence() {
		if (predictionSequence.length() == 0) {
			return;
		}

		DictionaryDb.getSuggestions(
			this,
			handleSuggestions,
			mLanguage.getId(),
			predictionSequence,
			prefs.getSuggestionsMin(),
			prefs.getSuggestionsMax()
		);
	}

	private ArrayList<String> guessSuggestionsWhenNone(ArrayList<String> suggestions, String lastWord) {
		if (
			(suggestions != null && suggestions.size() > 0) ||
			predictionSequence.length() == 0 ||
			predictionSequence.charAt(0) == '1'
		) {
			return suggestions;
		}

		lastWord = lastWord.substring(0, Math.min(predictionSequence.length(), lastWord.length()));
		try {
			int lastDigit = predictionSequence.charAt(predictionSequence.length() - 1) - '0';
			lastWord += mLanguage.getKeyCharacters(lastDigit).get(0);
		} catch (Exception e) {
			lastWord += predictionSequence.charAt(predictionSequence.length() - 1);
		}

		return new ArrayList<>(Collections.singletonList(lastWord));
	}


	private boolean nextSuggestionInModeAbc() {
		return isNumKeyRepeated && mInputMode == T9Preferences.MODE_ABC && nextSuggestion();
	}


	private void acceptCurrentSuggestion() {
		// bring this word up in the suggestions list next time
		if (mInputMode == T9Preferences.MODE_PREDICTIVE) {
			String currentWord = mSuggestionView.getCurrentSuggestion();
			if (currentWord.length() == 0) {
				Logger.i("acceptCurrentSuggestion", "Current word is empty. Nothing to accept.");
				return;
			}

			try {
				String sequence = mLanguage.getDigitSequenceForWord(currentWord);
				DictionaryDb.incrementWordFrequency(this, mLanguage.getId(), currentWord, sequence);
			} catch (Exception e) {
				Logger.e(getClass().getName(), "Failed incrementing priority of word: '" + currentWord + "'. " + e.getMessage());
			}
		}

		commitCurrentSuggestion();
	}


	private void commitCurrentSuggestion() {
		predictionSequence = "";

		// commit the current suggestion to the input field
		if (!isSuggestionViewHidden()) {
			if (mSuggestionView.getCurrentSuggestion().equals(" ")) {
				// finishComposingText() seems to ignore a single space,
				// so we have to force commit it.
				currentInputConnection.commitText(" ", 1);
			} else {
				currentInputConnection.finishComposingText();
			}
		}

		setSuggestions(null);
	}


	private void clearSuggestions() {
		if (currentInputConnection != null) {
			currentInputConnection.setComposingText("", 1);
			currentInputConnection.finishComposingText();
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
		if (mEditing == EDITING_STRICT_NUMERIC || mEditing == EDITING_DIALER) {
			clearSuggestions();
			mInputMode = T9Preferences.MODE_123;
		}
		// when typing a word or viewing scrolling the suggestions, only change the case
		else if (!isSuggestionViewHidden()) {
			determineAllowedTextCases();

			int modeIndex = (allowedTextCases.indexOf(mTextCase) + 1) % allowedTextCases.size();
			mTextCase = allowedTextCases.get(modeIndex);

			mSuggestionView.changeCase(mTextCase, mLanguage.getLocale());
			setComposingTextFromCurrentSuggestion();
		}
		// make "abc" and "ABC" separate modes from user perspective
		else if (mInputMode == T9Preferences.MODE_ABC && mTextCase == T9Preferences.CASE_LOWER) {
			mTextCase = T9Preferences.CASE_UPPER;
		} else {
			int modeIndex = (allowedInputModes.indexOf(mInputMode) + 1) % allowedInputModes.size();
			mInputMode = allowedInputModes.get(modeIndex);

			mTextCase = mInputMode == T9Preferences.MODE_PREDICTIVE ? T9Preferences.CASE_CAPITALIZE : T9Preferences.CASE_LOWER;
		}

		// save the settings for the next time
		prefs.saveInputMode(mInputMode);
		prefs.saveTextCase(mTextCase);

		UI.updateStatusIcon(this, mLanguage, mInputMode, mTextCase);
	}

	private void setComposingTextFromCurrentSuggestion() {
		if (!isSuggestionViewHidden()) {
			currentInputConnection.setComposingText(mSuggestionView.getCurrentSuggestion(), 1);
		}
	}


	private void nextLang() {
		if (mEditing == EDITING_STRICT_NUMERIC || mEditing == EDITING_DIALER) {
			return;
		}

		clearSuggestions();

		// select the next language
		int previousLangId = mEnabledLanguages.indexOf(mLanguage.getId());
		int nextLangId = previousLangId == -1 ? 0 : (previousLangId + 1) % mEnabledLanguages.size();
		mLanguage = LanguageCollection.getLanguage(mEnabledLanguages.get(nextLangId));

		validateLanguages();

		// save it for the next time
		prefs.saveInputLanguage(mLanguage.getId());

		UI.updateStatusIcon(this, mLanguage, mInputMode, mTextCase);
	}


	private void showAddWord() {
		if (mInputMode != T9Preferences.MODE_PREDICTIVE) {
			UI.toastLong(this, R.string.add_word_only_in_predictive_mode);
			return;
		}

		//////////////////////////////////////////////////////////////
		// @todo: remove this try..catch in #55 and display the dialog
		try {
			DictionaryDb.insertWord(this, "a", mLanguage.getId());
		} catch (InsertBlankWordException e) {
			Logger.e("tt9/showAddWord", e.getMessage());
			UI.toastLong(this, R.string.add_word_blank);
			return;
		} catch (InvalidLanguageException e) {
			Logger.e("tt9/showAddWord", e.getMessage());
			UI.toastLong(this, R.string.add_word_invalid_language);
			return;
		} catch (NotActiveException e) {
			UI.toastLong(this, e.getMessage());
			return;
		}
		//////////////////////////////////////////////////////////////

		clearSuggestions();

		String template = "";

		// @todo: get the current word template from the input connection
		// template = getSurroundingWord();

		UI.showAddWordDialog(this, mLanguage.getId(), template);
	}


	private void restoreLastWordIfAny() {
		// mAddingWord = false;
		String word = prefs.getLastWord();
		if (word.equals("")) {
			prefs.saveLastWord("");

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
