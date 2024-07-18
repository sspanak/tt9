package io.github.sspanak.tt9.ime;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.helpers.OrientationListener;
import io.github.sspanak.tt9.ime.modes.ModeABC;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.ResizableMainView;

/**
 * Informational methods for the on-screen keyboard
 **/
abstract public class MainViewHandler extends HotkeyHandler {
	OrientationListener orientationListener;

	@Override
	protected void onInit() {
		super.onInit();

		if (orientationListener == null) {
			orientationListener = new OrientationListener(this, mainView::onOrientationChanged);
			orientationListener.start();
		}
	}

	public int getTextCase() {
		return mInputMode.getTextCase();
	}

	public boolean isInputLimited() {
		return inputType.isLimited();
	}

	public boolean isInputModeABC() {
		return mInputMode.getClass().equals(ModeABC.class);
	}

	public boolean isInputModeNumeric() {
		return mInputMode.is123();
	}

	public boolean isNumericModeStrict() {
		return mInputMode.is123() && inputType.isNumeric() && !inputType.isPhoneNumber();
	}

	public boolean isNumericModeSigned() {
		return mInputMode.is123() && inputType.isSignedNumber();
	}

	public boolean isInputModePhone() {
		return mInputMode.is123() && inputType.isPhoneNumber();
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
}
