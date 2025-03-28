package io.github.sspanak.tt9.ime;

import android.view.inputmethod.InputMethodManager;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.ResizableMainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;
import io.github.sspanak.tt9.util.sys.DeviceInfo;
import io.github.sspanak.tt9.util.sys.SystemSettings;

abstract class UiHandler extends AbstractHandler {
	protected SettingsStore settings;
	protected ResizableMainView mainView = null;
	protected StatusBar statusBar = null;


	@Override
	protected void onInit() {
		if (mainView == null) {
			mainView = new ResizableMainView(getFinalContext());
			initTray();
		} else {
			mainView.destroy();
			setInputView(mainView.getView());
		}
	}


	protected void initTray() {
		setInputView(mainView.getView());
		createSuggestionBar();
		getSuggestionOps().setDarkTheme();
		statusBar = new StatusBar(mainView.getView());
	}


	public void initUi(InputMode inputMode) {
		if (mainView.create()) {
			initTray();
		} else {
			getSuggestionOps().setDarkTheme();
		}
		setStatusIcon(inputMode);
		statusBar.setText(inputMode);
		mainView.hideCommandPalette();
		mainView.render();
		SystemSettings.setNavigationBarDarkTheme(getWindow().getWindow(), settings.getDarkTheme());

		if (!isInputViewShown()) {
			updateInputViewShown();
		}
	}


	protected void setStatusIcon(InputMode mode) {
		if (!InputModeKind.isPassthrough(mode) && settings.isStatusIconEnabled()) {
			showStatusIcon(R.drawable.ic_keyboard);
		} else {
			hideStatusIcon();
		}
	}


	protected boolean shouldBeVisible() {
		return determineInputModeId() != InputMode.MODE_PASSTHROUGH && !settings.isMainLayoutStealth();
	}


	/**
	 * forceShowWindow
	 * Some applications may hide our window and it remains invisible until the screen is touched or OK is pressed.
	 * This is fine for touchscreen keyboards, but the hardware keyboard allows typing even when the window and the suggestions
	 * are invisible. This function forces the InputMethodManager to show our window.
	 * WARNING! Calling this may cause a restart, which will cause InputMode to be recreated. Depending
	 * on how much time the restart takes, this may erase the current user input.
	 */
	protected void forceShowWindow() {
		if (isInputViewShown() || !shouldBeVisible()) {
			return;
		}

		if (DeviceInfo.AT_LEAST_ANDROID_9) {
			requestShowSelf(DeviceInfo.isSonimGen2(getApplicationContext()) ? 0 : InputMethodManager.SHOW_IMPLICIT);
		} else {
			showWindow(true);
		}
	}
}
