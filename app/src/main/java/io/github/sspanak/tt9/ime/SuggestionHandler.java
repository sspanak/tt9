package io.github.sspanak.tt9.ime;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.mindReading.MindReader;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.helpers.SuggestionOps;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;
import io.github.sspanak.tt9.util.sys.Clipboard;

abstract public class SuggestionHandler extends TypingHandler {
	@Nullable private Handler suggestionHandler;
	@NonNull private MindReader mindReader = new MindReader();


	@Override
	protected void onInit() {
		super.onInit();
		mindReader = new MindReader(settings, executor);
	}


	private Handler getAsyncHandler() {
		if (suggestionHandler == null) {
			suggestionHandler = new Handler(Looper.getMainLooper());
		} else {
			suggestionHandler.removeCallbacksAndMessages(null);
		}

		return suggestionHandler;
	}


	@Override
	protected void onFinishTyping() {
		if (suggestionHandler != null) {
			suggestionHandler.removeCallbacksAndMessages(null);
			suggestionHandler = null;
		}

		clearGuessingContext();
		super.onFinishTyping();
	}


	private String[] onAcceptPreviousSuggestion() {
		final int lastWordLength = InputModeKind.isABC(mInputMode) ? 1 : mInputMode.getSequenceLength() - 1;
		String lastWord = suggestionOps.getCurrent(mLanguage, lastWordLength);
		if (Characters.PLACEHOLDER.equals(lastWord)) {
			lastWord = "";
		}

		suggestionOps.commitCurrent(false, true);
		mInputMode.onAcceptSuggestion(lastWord, true);
		final String[] surroundingText = autoCorrectSpace(
			lastWord,
			textField.getSurroundingStringForAutoAssistance(settings, mInputMode),
			false,
			mInputMode.getFirstKey()
		);
		mInputMode.determineNextWordTextCase(surroundingText[0], -1);
		mindReader.setContext(mInputMode, mLanguage, surroundingText, lastWord);

		return surroundingText;
	}


	@Override
	protected void onAcceptSuggestionsDelayed(String word) {
		onAcceptSuggestionManually(word, -1);
		forceShowWindow();
	}


	protected void onAcceptSuggestionManually(String word, int fromKey) {
		mInputMode.onAcceptSuggestion(word);
		if (Clipboard.contains(word)) {
			Clipboard.copy(this, word);
		}

		if (!word.isEmpty()) {
			String[] surroundingText = autoCorrectSpace(
				word,
				textField.getSurroundingStringForAutoAssistance(settings, mInputMode),
				true,
				fromKey
			);
			updateShiftState(surroundingText[0], true, false);
			resetKeyRepeat();
			guessNextWord(surroundingText, word, true);
		}

		if (!Characters.getSpace(mLanguage).equals(word)) {
			waitForSpaceTrimKey();
		}
	}


	@NonNull
	@Override
	public SuggestionOps getSuggestionOps() {
		return suggestionOps;
	}


	/**
	 * Ask the InputMode to load suggestions for the current state. No action is taken if the dictionary
	 * is still loading. Note that onComplete is called even if the loading was skipped.
	 */
	@Override
	protected void getSuggestions(@Nullable String currentWord, @Nullable Runnable onComplete) {
		if (InputModeKind.isPredictive(mInputMode) && DictionaryLoader.getInstance(this).isRunning()) {
			mInputMode.reset();
			UI.toastShortSingle(this, R.string.dictionary_loading_please_wait);
			if (onComplete != null) {
				onComplete.run();
			}
		} else {
			mInputMode
				.setOnSuggestionsUpdated(() -> handleSuggestionsAsync(onComplete))
				.loadSuggestions(currentWord == null ? suggestionOps.getCurrent() : currentWord);
		}
	}


	@WorkerThread
	protected void handleSuggestionsAsync() {
		handleSuggestionsAsync(null);
	}


	@WorkerThread
	protected void handleSuggestionsAsync(@Nullable Runnable onComplete) {
		getAsyncHandler().post(() -> handleSuggestions(onComplete));
	}


	@MainThread
	protected void handleSuggestions(@Nullable Runnable onComplete) {
		// Second pass, analyze the available suggestions and decide if combining them with the
		// last key press makes up a compound word like: (it)'s, (I)'ve, l'(oiseau), or it is
		// just the end of a sentence, like: "word." or "another?"
		String[] surroundingText = null;
		if (mInputMode.shouldAcceptPreviousSuggestion(suggestionOps.getCurrent())) {
			surroundingText = onAcceptPreviousSuggestion();
		}

		final ArrayList<String> suggestions = mInputMode.getSuggestions();
		suggestionOps.set(suggestions, mInputMode.getRecommendedSuggestionIdx(), mInputMode.containsGeneratedSuggestions());

		// either accept the first one automatically (when switching from punctuation to text
		// or vice versa), or schedule auto-accept in N seconds (in ABC mode)
		if (suggestionOps.scheduleDelayedAccept(mInputMode.getAutoAcceptTimeout())) {
			return;
		}

		// We have not accepted anything yet, which means the user is composing a word.
		// put the first suggestion in the text field, but cut it off to the length of the sequence
		// (the count of key presses), for a more intuitive experience.
		String trimmedWord;

		if (InputModeKind.isRecomposing(mInputMode)) {
			// highlight the current letter, when editing a word
			trimmedWord = mInputMode.getWordStem() + suggestionOps.getCurrent();
			appHacks.setComposingTextPartsWithHighlightedJoining(trimmedWord, mInputMode.getRecomposingSuffix());
		} else {
			// or highlight the stem, when filtering
			trimmedWord = suggestionOps.getCurrent(mLanguage, mInputMode.getSequenceLength());
			appHacks.setComposingTextWithHighlightedStem(trimmedWord, mInputMode.getWordStem(), mInputMode.isStemFilterFuzzy());
		}

		onAfterSuggestionsHandled(onComplete, surroundingText, trimmedWord, suggestions.isEmpty());
	}


	private void onAfterSuggestionsHandled(@Nullable Runnable callback, @Nullable String[] surroundingText, @Nullable String trimmedWord, boolean noSuggestions) {
		final String shiftStateContext = surroundingText != null ? surroundingText[0] + trimmedWord : trimmedWord;
		if (noSuggestions) {
			updateShiftStateDebounced(shiftStateContext, true, false);
		} else {
			updateShiftStateDebounced(shiftStateContext, false, true);
		}

		forceShowWindow();

		if (callback != null) {
			callback.run();
		}
	}


	@NonNull
	protected ArrayList<String> getCurrentGuesses() {
		return mindReader.getCurrentWords(mInputMode.getTextCase());
	}


	@Override
	protected void clearGuessingContext() {
		mindReader.clearContext();
	}


	@Override
	protected void setGuessingContext(@NonNull String[] surroundingText, @Nullable String lastWord) {
		mindReader.setContext(mInputMode, mLanguage, surroundingText, lastWord);
	}


	@Override
	protected void guessOnNumber(@NonNull String[] surroundingText, @Nullable String lastWord, int number) {
		if (mInputMode.getSequenceLength() != 1 || new Text(mLanguage, surroundingText[1]).startsWithWord()) {
			return;
		}

		if (number == 0 && !mLanguage.hasLettersOnAllKeys()) {
			return;
		}

		if (mLanguage.hasSpaceBetweenWords()) {
			final String space = Characters.getSpace(mLanguage);
			if (space.equals(lastWord) || surroundingText[0].endsWith(space)) {
				guessCurrentWord(surroundingText, number);
			}
		} else {
			guessCurrentWord(surroundingText, number);
		}
	}


	private void guessCurrentWord(@NonNull String[] surroundingText, int number) {
		mindReader.guessCurrent(mInputMode, (NaturalLanguage) mLanguage, surroundingText, number, () -> {
			// @todo: intercept these in handleSuggestions together with the regular suggestions
			Logger.d(getClass().getSimpleName(), "======+> guesses for number " + number + ": " + getCurrentGuesses());
		});
	}


	@Override
	protected void guessNextWord(@NonNull String[] surroundingText, @Nullable String lastWord, boolean saveContext) {
		mindReader.guessNext(mInputMode, mLanguage, surroundingText, lastWord, saveContext, this::handleGuessesAsync);
	}


	@WorkerThread
	private void handleGuessesAsync() {
		getAsyncHandler().post(() -> handleGuesses(getCurrentGuesses()));
	}


	@MainThread
	private void handleGuesses(@NonNull ArrayList<String> guesses) {
		if (!guesses.isEmpty()) {
			appHacks.setComposingText(guesses.get(0));
			suggestionOps.addGuesses(guesses);
		}
	}
}
