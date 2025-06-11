package io.github.sspanak.tt9.ime;

import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.helpers.OrientationListener;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.ResizableMainView;

/**
 * Informational methods for the on-screen keyboard
 **/
abstract public class MainViewHandler extends HotkeyHandler {
	OrientationListener orientationListener;

	private boolean dragResize = true;
	private float normalizedWidth = -1;
	private float normalizedHeight = -1;
	private int width = 0;


	@Override
	protected void onInit() {
		super.onInit();

		if (orientationListener == null) {
			orientationListener = new OrientationListener(this, this::onOrientationChanged);
			orientationListener.start();
		}
	}


	@Override
	protected boolean onStart(EditorInfo field) {
		resetNormalizedDimensions();
		dragResize = settings.getDragResize();
		return super.onStart(field);
	}


	private void onOrientationChanged() {
		width = 0;
		resetNormalizedDimensions();
		if (mainView != null) {
			mainView.onOrientationChanged();
		}
	}


	protected void cleanUp() {
		if (orientationListener != null) {
			orientationListener.stop();
			orientationListener = null;
		}
		if (mainView != null) {
			mainView.destroy();
		}
	}


	public boolean isAddingWordsSupported() {
		return mLanguage == null || !mLanguage.isTranscribed();
	}


	public boolean isDragResizeOn() {
		return dragResize;
	}


	public boolean isFilteringSupported() {
		return mInputMode.supportsFiltering();
	}

	public boolean isFilteringFuzzy() {
		return mInputMode.isStemFilterFuzzy();
	}

	public boolean isFilteringOn() {
		String stem = mInputMode.getWordStem();
		return stem != null && !stem.isEmpty();
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


	public boolean isNumericModeStrict() {
		return InputModeKind.is123(mInputMode) && inputType.isNumeric() && !inputType.isPhoneNumber();
	}


	public boolean isNumericModeSigned() {
		return InputModeKind.is123(mInputMode) && inputType.isSignedNumber();
	}


	public boolean isInputModePhone() {
		return InputModeKind.is123(mInputMode) && inputType.isPhoneNumber();
	}


	public boolean isTextEditingActive() {
		return mainView != null && mainView.isTextEditingPaletteShown();
	}


	public boolean isVoiceInputActive() {
		return voiceInputOps != null && voiceInputOps.isListening();
	}


	public boolean isVoiceInputMissing() {
		return !(new VoiceInputOps(this, null, null, null)).isAvailable();
	}


	public String getABCString() {
		return mLanguage == null ? "ABC" : mLanguage.getAbcString().toUpperCase(mLanguage.getLocale());
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


	public int getTextCase() {
		return mInputMode.getTextCase();
	}


	@Nullable
	public Language getLanguage() {
		return mLanguage;
	}


	public ResizableMainView getMainView() {
		return mainView;
	}


	public SettingsStore getSettings() {
		return settings;
	}


	public int getWidth() {
		if (width == 0 && mainView != null && mainView.getView() != null) {
			width = mainView.getView().getWidth();
		}

		return width;
	}


	public float getNormalizedWidth() {
		if (normalizedWidth < 0) {
			normalizedWidth = settings.getWidthPercent() / 100f;
		}
		return normalizedWidth;
	}


	public float getNormalizedHeight() {
		if (normalizedHeight < 0) {
			normalizedHeight = (float) settings.getNumpadKeyHeight() / (float) settings.getNumpadKeyDefaultHeight();
		}
		return normalizedHeight;
	}


	private void resetNormalizedDimensions() {
		normalizedWidth = -1;
		normalizedHeight = -1;
	}
}
