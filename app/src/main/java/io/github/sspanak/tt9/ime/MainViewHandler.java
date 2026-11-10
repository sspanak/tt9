package io.github.sspanak.tt9.ime;

import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.ArrayDeque;

import io.github.sspanak.tt9.hacks.AppHacks;
import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.extensions.ChineseExtensions;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.helpers.TextSelection;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.ime.mindreader.MindReader;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ime.modes.ModePinyin;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;
import io.github.sspanak.tt9.util.Ternary;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

/**
 * Informational methods for the on-screen keyboard
 **/
abstract public class MainViewHandler extends HotkeyHandler {
	private int previousOrientation = Configuration.ORIENTATION_UNDEFINED;

	private boolean touchExplorationEnabled = false;
	private float normalizedWidth = -1;
	private float normalizedHeight = -1;
	private int width = 0;
	private final ArrayDeque<Runnable> pendingPinyinKeys = new ArrayDeque<>();


	@Override
	protected boolean onStart(EditorInfo field, boolean restarting) {
		resetCandidateRepeat();
		resetNormalizedDimensions();
		touchExplorationEnabled = DeviceInfo.isTouchExplorationEnabled(this);
		return super.onStart(field, restarting);
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation != previousOrientation) {
			previousOrientation = newConfig.orientation;
			onOrientationChanged();
		}
	}


	@Override
	public boolean onBackspace(int repeat) {
		if (deferPinyinKey(() -> onBackspace(repeat))) return true;
		suggestionOps.collapse();
		mainView.renderClickFn(KeyEvent.KEYCODE_DEL);
		return super.onBackspace(repeat);
	}


	@Override
	protected boolean onNumber(int key, boolean hold, int repeat) {
		if (deferPinyinKey(() -> onNumber(key, hold, repeat))) return true;
		if (mInputMode instanceof ModePinyin pinyin && pinyin.isPinyinSequenceEditing() && (key < 2 || hold)) {
			finishPinyinEdit(pinyin, false);
			return true;
		}
		if (suggestionOps.isExpanded() && mInputMode.supportsExpandedSuggestions() && suggestionOps.usesNumberKeys() && key >= 1 && key <= 9) {
			mainView.renderClickNumber(key);
			if (!hold && suggestionOps.selectNumbered(key)) {
				super.onOK(Key.numberToCode(settings, key));
			}
			return true;
		}
		suggestionOps.collapse();
		mainView.renderClickNumber(key);
		return super.onNumber(key, hold, repeat);
	}


	@Override
	public boolean onOK() {
		if (deferPinyinKey(this::onOK)) return true;
		if (mInputMode instanceof ModePinyin pinyin && pinyin.isPinyinSequenceEditing()) {
			finishPinyinEdit(pinyin, false);
			return true;
		}
		mainView.renderClickFn(KeyEvent.KEYCODE_ENTER);
		return super.onOK();
	}


	@Override
	public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly) {
		if (!validateOnly) { // on release
			mainView.renderClickFn(keyCode);
		}
		if (isPinyinPreviewPending()
			&& (Key.isArrow(keyCode) || Key.isPoundOrStar(Math.abs(keyCode)) || Key.isHotkey(settings, keyCode))) {
			if (!validateOnly) deferPinyinKey(() -> onHotkey(keyCode, repeat, false));
			return true;
		}
		if (mInputMode instanceof ModePinyin pinyin && pinyin.isPinyinSequenceEditing()
			&& (Key.isArrow(keyCode) || Key.isPoundOrStar(Math.abs(keyCode)) || Key.isHotkey(settings, keyCode))) {
			if (!validateOnly) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					pinyin.movePinyinSequenceCursor(keyCode == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : 1);
				} else if (keyCode != KeyEvent.KEYCODE_DPAD_UP) {
					finishPinyinEdit(pinyin, false);
				}
			}
			return true;
		}

		if (suggestionOps.isExpanded()) {
			if (keyCode >= KeyEvent.KEYCODE_DPAD_UP && keyCode <= KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (!validateOnly) {
					moveInCandidatePanel(keyCode);
				}
				return true;
			} else if (!validateOnly) {
				suggestionOps.collapse();
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && canExpandCandidates()) {
			return validateOnly || suggestionOps.expand();
		}

		return super.onHotkey(keyCode, repeat, validateOnly);
	}

	private boolean canExpandCandidates() {
		return settings.getKeyFilterClear() == KeyEvent.KEYCODE_DPAD_DOWN
			&& mInputMode.supportsExpandedSuggestions() && InputModeKind.isPredictive(mInputMode)
			&& !isFnPanelVisible() && !isVoiceInputActive()
			&& !suggestionOps.containsNoOrdinaryWords() && !suggestionOps.containsStem()
			&& !mInputMode.isStemFilterFuzzy()
			&& suggestionOps.canExpand();
	}

	@Override
	protected boolean canRepeatCandidateArrow(int keyCode) {
		if (suggestionOps.isExpanded()) return true;
		if (!(Key.isArrowLeft(keyCode) || Key.isArrowRight(keyCode))
			|| mInputMode instanceof ModePinyin pinyin && pinyin.isPinyinSequenceEditing()
			|| isFnPanelVisible() || isVoiceInputActive() || suggestionOps.isEmpty()) return false;
		// A configured hold action (including recomposition's letter editing) takes precedence.
		return (keyCode == settings.getKeyPreviousSuggestion() || keyCode == settings.getKeyNextSuggestion())
			&& !getFinalContext().isHoldHotkey(-keyCode);
	}

	@Override
	protected int getCandidateArrowRepeatInterval() {
		var chinese = suggestionOps.isExpanded() ? ChineseExtensions.get() : null;
		return chinese == null ? super.getCandidateArrowRepeatInterval() : chinese.getExpandedCandidateRepeatInterval(settings);
	}

	private void moveInCandidatePanel(int keyCode) {
		int horizontal = keyCode == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : 0;
		int vertical = keyCode == KeyEvent.KEYCODE_DPAD_UP ? -1 : keyCode == KeyEvent.KEYCODE_DPAD_DOWN ? 1 : 0;
		suggestionOps.moveInGrid(horizontal, vertical, !isCandidateArrowRepeating());
		// The Up that closes the panel must not count as the first press of the spelling filter.
		if (!suggestionOps.isExpanded()) resetKeyRepeat();
		mInputMode.setWordStem(suggestionOps.getCurrent(), true);
		appHacks.setComposingTextWithHighlightedStem(suggestionOps.getCurrent(), mInputMode.getWordStem(), mInputMode.isStemFilterFuzzy());
		mainView.renderDynamicKeys();
	}

	@Override
	public Ternary onBack() {
		if (mInputMode instanceof ModePinyin pinyin && pinyin.isPinyinSequenceEditing()) {
			pendingPinyinKeys.clear();
			finishPinyinEdit(pinyin, true);
			return Ternary.TRUE;
		}
		if (deferPinyinKey(this::onBack)) return Ternary.TRUE;
		return suggestionOps.collapse() ? Ternary.TRUE : super.onBack();
	}

	private void finishPinyinEdit(ModePinyin pinyin, boolean cancel) {
		// Keep the composing span until the Chinese query replaces the Pinyin preview.
		if (cancel) {
			pinyin.cancelPinyinSequenceEdit();
		} else {
			pinyin.finishPinyinSequenceEdit();
		}
		resetKeyRepeat();
	}

	private boolean isPinyinPreviewPending() {
		return mInputMode instanceof ModePinyin pinyin && pinyin.isPinyinSequenceTransitionPending();
	}

	private boolean deferPinyinKey(Runnable key) {
		if (!isPinyinPreviewPending()) return false;
		InputMode mode = mInputMode;
		pendingPinyinKeys.add(() -> { if (mInputMode == mode) key.run(); });
		return true;
	}

	@Override
	protected void handleSuggestions(double loadingId, @Nullable Runnable onComplete) {
		super.handleSuggestions(loadingId, onComplete);
		while (!pendingPinyinKeys.isEmpty() && !isPinyinPreviewPending()) {
			pendingPinyinKeys.remove().run();
		}
	}

	@Override
	protected void onFinishTyping(boolean willExitInput) {
		resetCandidateRepeat();
		pendingPinyinKeys.clear();
		suggestionOps.collapse();
		super.onFinishTyping(willExitInput);
	}


	private void onOrientationChanged() {
		suggestionOps.collapse();
		width = 0;
		resetNormalizedDimensions();
		if (mainView != null) {
			mainView.onOrientationChanged();
		}
	}


	@Override
	protected void cleanUp() {
		super.cleanUp();
		if (mainView != null) {
			mainView.destroy();
		}
	}

	public boolean areEmojiCategoriesVisible() {
		return false; // only in premium
	}


	public boolean isFilteringFuzzy() {
		return mInputMode.isStemFilterFuzzy();
	}


	public boolean isFilteringOn() {
		String stem = mInputMode.getWordStem();
		return stem != null && !stem.isEmpty();
	}

	public boolean isFnPanelVisible() {
		return mainView != null && mainView.isFnPanelVisible();
	}


	public boolean isInputLimited() {
		return inputType.isLimited();
	}


	public boolean isInputModeABC() {
		return InputModeKind.isABC(mInputMode);
	}


	public boolean isInputModeNumeric() {
		return InputModeKind.isNumeric(mInputMode);
	}

	public boolean isInputTypeNumeric() {
		return inputType.isNumeric();
	}

	public boolean isInputTypeDecimal() {
		return (inputType.isDecimal() || inputType.isUnspecifiedNumber());
	}

	public boolean isInputTypeSigned() {
		return (inputType.isSignedNumber() || inputType.isUnspecifiedNumber());
	}

	public boolean isInputTypePhone() {
		return inputType.isPhoneNumber();
	}

	public boolean isTextEditingActive() {
		return mainView != null && mainView.isTextEditingPaletteShown();
	}


	public boolean isTouchExplorationEnabled() {
		return touchExplorationEnabled;
	}


	public boolean isVoiceInputActive() {
		return voiceInputOps.isListening();
	}


	public boolean isVoiceInputMissing() {
		return !voiceInputOps.isAvailable();
	}


	public String getABCString() {
		return mLanguage == null ? "ABC" : mLanguage.getAbcString().toUpperCase(mLanguage.getLocale());
	}


	@NonNull
	public ArrayList<Integer> getAllowedInputModes() {
		return new ArrayList<>(allowedInputModes);
	}


	@NonNull
	public AppHacks getAppHacks() {
		return appHacks;
	}


	public int getDisplayTextCase() {
		return getDisplayTextCase(mLanguage, mInputMode.getTextCase());
	}


	public InputMode getInputMode() {
		return mInputMode;
	}


	@NonNull
	public String getInputModeName() {
		if (InputModeKind.isHiragana(mInputMode)) {
			return "あ";
		} else if (InputModeKind.isKatakana(mInputMode)) {
			return "ア";
		}  else if (InputModeKind.isPredictive(mInputMode)) {
			return mLanguage != null ? mLanguage.getCode().toUpperCase(mLanguage.getLocale()) : "T9";
		} else if (InputModeKind.isNumeric(mInputMode)){
			return "123";
		} else {
			return getABCString();
		}
	}


	@NonNull
	public InputType getInputType() {
		return inputType;
	}


	@Nullable
	public Language getLanguage() {
		return mLanguage;
	}


	@Nullable
	public MainView getMainView() {
		return mainView;
	}


	@NonNull
	public MindReader getMindReader() {
		return mindReader;
	}


	public SettingsStore getSettings() {
		return settings;
	}

	@Nullable
	public StatusBar getStatusBar() {
		return statusBar;
	}


	public int getTextCase() {
		return mInputMode.getTextCase();
	}


	@Nullable
	public TextSelection getTextSelection() {
		return textSelection;
	}


	@NonNull
	public TextField getTextField() {
		return textField;
	}


	@NonNull
	public VoiceInputOps getVoiceInputOps() {
		return voiceInputOps;
	}


	public int getWidth() {
		if (width == 0 && mainView != null && mainView.getView() != null) {
			width = mainView.getView().getWidth();
		}

		return width;
	}


	public float getNormalizedWidth() {
		if (normalizedWidth < 0) {
			normalizedWidth = settings.getWidthPercent(!DeviceInfo.isLandscapeOrientation(this), null) / 100f;
		}
		return normalizedWidth;
	}


	public float getNormalizedHeight() {
		if (normalizedHeight < 0) {
			normalizedHeight = (float) settings.getNumpadKeyHeight(null) / (float) settings.getNumpadKeyDefaultHeight();
		}
		return normalizedHeight;
	}


	private void resetNormalizedDimensions() {
		normalizedWidth = -1;
		normalizedHeight = -1;
	}
}
