package io.github.sspanak.tt9.ime;

import android.os.Build;
import android.view.inputmethod.InputMethodManager;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;

abstract class UiHandler extends AbstractHandler {
	protected SettingsStore settings;
	protected MainView mainView = null;
	private StatusBar statusBar = null;


	@Override
	protected void onInit() {
		if (mainView == null) {
			mainView = new MainView(getFinalContext());
			initTray();
		}
	}


	protected void initTray() {
		setInputView(mainView.getView());
		createSuggestionBar(mainView.getView());
		statusBar = new StatusBar(mainView.getView());
	}


	protected void initUi() {
		if (mainView.createInputView()) {
			initTray();
		}
		setDarkTheme();
		setStatusIcon(getInputMode());
		setStatusText(getInputMode().toString());
		mainView.hideCommandPalette();
		mainView.render();

		if (!isInputViewShown()) {
			updateInputViewShown();
		}
	}


	protected void setDarkTheme() {
		mainView.setDarkTheme(settings.getDarkTheme());
		statusBar.setDarkTheme(settings.getDarkTheme());
		getSuggestionOps().setDarkTheme(settings.getDarkTheme());
	}


	protected void setStatusIcon(InputMode mode) {
		if (!mode.isPassthrough() && settings.isStatusIconEnabled()) {
			showStatusIcon(R.drawable.ic_status);
		} else {
			hideStatusIcon();
		}
	}


	protected void setStatusText(String status) {
		statusBar.setText(status);
	}


	protected boolean shouldBeVisible() {
		return getInputModeId() != InputMode.MODE_PASSTHROUGH && !settings.isMainLayoutStealth();
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			requestShowSelf(DeviceInfo.isSonimGen2(getApplicationContext()) ? 0 : InputMethodManager.SHOW_IMPLICIT);
		} else {
			showWindow(true);
		}
	}
}
