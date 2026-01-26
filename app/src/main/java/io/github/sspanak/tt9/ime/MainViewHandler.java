package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.helpers.OrientationListener;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.helpers.TextSelection;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

/**
 * Informational methods for the on-screen keyboard
 **/
abstract public class MainViewHandler extends HotkeyHandler {
	OrientationListener orientationListener;

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
	protected boolean onStart(EditorInfo field, boolean restarting) {
		resetNormalizedDimensions();
		return super.onStart(field, restarting);
	}


	@Override
	public boolean onBackspace(int repeat) {
		mainView.renderClickFn(KeyEvent.KEYCODE_DEL);
		return super.onBackspace(repeat);
	}


	@Override
	protected boolean onNumber(int key, boolean hold, int repeat) {
		mainView.renderClickNumber(key);
		return super.onNumber(key, hold, repeat);
	}


	@Override
	public boolean onOK() {
		mainView.renderClickFn(KeyEvent.KEYCODE_ENTER);
		return super.onOK();
	}


	@Override
	public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly) {
		if (!validateOnly) { // on release
			mainView.renderClickFn(keyCode);
		}

		return super.onHotkey(keyCode, repeat, validateOnly);
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


	public boolean isVoiceInputActive() {
		return voiceInputOps != null && voiceInputOps.isListening();
	}


	public boolean isVoiceInputMissing() {
		return !(new VoiceInputOps(this, null, null, null, null)).isAvailable();
	}


	public String getABCString() {
		return mLanguage == null ? "ABC" : mLanguage.getAbcString().toUpperCase(mLanguage.getLocale());
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


	public int getTextCase() {
		return mInputMode.getTextCase();
	}


	@Nullable
	public Language getLanguage() {
		return mLanguage;
	}


	public MainView getMainView() {
		return mainView;
	}


	public SettingsStore getSettings() {
		return settings;
	}


	@Nullable
	public TextSelection getTextSelection() {
		return textSelection;
	}


	public int getWidth() {
		if (width == 0 && mainView != null && mainView.getView() != null) {
			width = mainView.getView().getWidth();
		}

		return width;
	}


	public float getNormalizedWidth() {
		if (normalizedWidth < 0) {
			normalizedWidth = settings.getWidthPercent(!DeviceInfo.isLandscapeOrientation(this)) / 100f;
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
