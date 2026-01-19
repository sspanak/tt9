package io.github.sspanak.tt9.ime;

import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.AppHacks;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.StatusIcon;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.sys.DeviceInfo;
import io.github.sspanak.tt9.util.sys.SystemSettings;

abstract class UiHandler extends AbstractHandler {
	private final static String LOG_TAG = "UiHandler";

	@NonNull protected final AppHacks appHacks = new AppHacks();
	protected SettingsStore settings;

	protected int displayTextCase = InputMode.CASE_UNDEFINED;
	protected boolean isMainViewShown = false;
	protected MainView mainView = null;
	protected StatusBar statusBar = null;


	@Override
	public boolean onEvaluateInputViewShown() {
		super.onEvaluateInputViewShown();
		if (!SystemSettings.isTT9Selected(this)) {
			isMainViewShown = false;
			return false;
		}

		setInputField(getCurrentInputEditorInfo());
		return isMainViewShown = shouldBeVisible();
	}


	@Override
	public boolean onEvaluateFullscreenMode() {
		return false;
	}


	@Override
	protected void onInit() {
		if (mainView == null) {
			mainView = new MainView(getFinalContext());
			initTray();
		} else {
			mainView.destroy();
			mainView.getView();
		}
	}


	protected void initTray() {
		mainView.getView();
		statusBar = new StatusBar(this, settings, mainView.getView(), this::resetStatus).setColorScheme();
		createSuggestionBar();
		getSuggestionOps().setColorScheme();
	}


	public void initUi(InputMode inputMode) {
		if (mainView.create()) {
			initTray();
			setCurrentView();
		} else {
			getSuggestionOps().setColorScheme();
		}
		setStatusIcon(inputMode, getFinalContext().getLanguage());
		statusBar.setColorScheme().setText(inputMode);
		mainView.showKeyboard();
		mainView.render();

		SystemSettings.setNavigationBarBackground(getWindow().getWindow(), settings, mainView.isBackgroundBlendingEnabled());

		if (appHacks.isBrutalForceShowNeeded()) {
			brutalForceShowWindow();
		} else if (!isInputViewShown()) {
			updateInputViewShown();
		}
	}


	public void setCurrentView() {
		setInputView(onCreateInputView());
	}


	protected int getDisplayTextCase(@Nullable Language language, int modeTextCase) {
		boolean hasUpperCase = language != null && language.hasUpperCase();
		if (!hasUpperCase) {
			return displayTextCase = InputMode.CASE_UNDEFINED;
		}

		if (modeTextCase == InputMode.CASE_UPPER) {
			return displayTextCase = InputMode.CASE_UPPER;
		}

		Text currentWord = new Text(language, getSuggestionOps().getCurrent());
		if (currentWord.isEmpty() || !currentWord.isAlphabetic()) {
			return displayTextCase = modeTextCase;
		}

		final int wordTextCase = currentWord.getTextCase();
		return displayTextCase = wordTextCase == InputMode.CASE_UPPER ? InputMode.CASE_CAPITALIZE : wordTextCase;
	}


	protected void setStatusIcon(@Nullable InputMode mode, @Nullable Language language) {
		if (!settings.isStatusIconEnabled()) {
			return;
		}

		final int resId = new StatusIcon(settings.isStatusIconEnabled() ? mode : null, language, displayTextCase).resourceId;
		if (resId == 0) {
			hideStatusIcon();
		} else {
			showStatusIcon(resId);
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


	/**
	 * Shows the IME window using brutal force, ignoring IME flags and state, and any (invalid) app
	 * requests for passthrough mode. Note that this should not be randomly used, because it will
	 * cause the UI to appear in calculators, banking apps or others where it is not desired.
	 * Reported problems (in chronological order):
	 *	- <a href="https://github.com/sspanak/tt9/issues/920">Google search field in Firefox on Android 16</a>
	 *	- <a href="https://github.com/sspanak/tt9/issues/963">Gmail reply/forward on Android 16</a>
	 */
	private void brutalForceShowWindow() {
		if (!isShowInputRequested() || !isMainViewShown) {
			forceShowWindow();
		}

		if (!isShowInputRequested() || !isMainViewShown) {
			Logger.d(LOG_TAG, "InputMethodManager refused show request. Forcing visibility with showWindow().");
			showWindow(true);
		}
	}
}
