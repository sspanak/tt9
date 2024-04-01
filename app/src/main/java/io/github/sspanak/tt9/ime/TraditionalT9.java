package io.github.sspanak.tt9.ime;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.ime.modes.ModePassthrough;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.dialogs.PopupDialog;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;
import io.github.sspanak.tt9.util.Logger;

public class TraditionalT9 extends HotkeyHandler {
	@NonNull
	private final Handler normalizationHandler = new Handler(Looper.getMainLooper());
	private MainView mainView = null;
	private StatusBar statusBar = null;


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int result = super.onStartCommand(intent, flags, startId);

		String message = intent != null ? intent.getStringExtra(PopupDialog.INTENT_CLOSE) : null;
		if (message != null) {
			forceShowWindowIfHidden();
			if (!message.isEmpty()) {
				UI.toastLong(this, message);
			}
		}

		return result;
	}


	@Override
	protected void onInit() {
		Logger.setLevel(settings.getLogLevel());

		WordStoreAsync.init(this);

		if (mainView == null) {
			mainView = new MainView(this);
			initTray();
		}

		super.onInit();
	}


	private void initTray() {
		setInputView(mainView.getView());
		createSuggestionBar(mainView.getView());
		statusBar = new StatusBar(mainView.getView());
	}


	private void setDarkTheme() {
		mainView.setDarkTheme(settings.getDarkTheme());
		statusBar.setDarkTheme(settings.getDarkTheme());
		suggestionOps.setDarkTheme(settings.getDarkTheme());
	}


	private void initUi() {
		if (mainView.createView()) {
			initTray();
		}
		setStatusIcon(mInputMode.getIcon());
		setStatusText(mInputMode.toString());
		setDarkTheme();
		mainView.render();
	}


	@Override
	protected void onStart(InputConnection connection, EditorInfo field) {
		Logger.setLevel(settings.getLogLevel());

		super.onStart(connection, field);

		if (mInputMode.isPassthrough()) {
			// When the input is invalid or simple, let Android handle it.
			onStop();
			updateInputViewShown();
			return;
		}

		normalizationHandler.removeCallbacksAndMessages(null);

		initUi();
		updateInputViewShown();
	}


	@Override
	protected void onFinishTyping() {
		if (!(mInputMode instanceof ModePassthrough)) {
			DictionaryLoader.autoLoad(this, mLanguage);
		}
		super.onFinishTyping();
	}


	@Override
	protected void onStop() {
		onFinishTyping();
		suggestionOps.clear();
		setStatusIcon(0);
		setStatusText("--");

		normalizationHandler.removeCallbacksAndMessages(null);
		normalizationHandler.postDelayed(
			() -> { if (!DictionaryLoader.getInstance(this).isRunning()) WordStoreAsync.normalizeNext(); },
			SettingsStore.WORD_NORMALIZATION_DELAY
		);
	}


	/**
	 * createMainView
	 * Generates the actual UI of TT9.
	 */
	protected View createMainView() {
		mainView.forceCreateView();
		initTray();
		setDarkTheme();
		return mainView.getView();
	}


	/**
	 * Populates the UI elements with strings and icons
	 */
	@Override
	protected void renderMainView() {
		mainView.render();
	}


	/**
	 * forceShowWindowIfHidden
	 * Some applications may hide our window and it remains invisible until the screen is touched or OK is pressed.
	 * This is fine for touchscreen keyboards, but the hardware keyboard allows typing even when the window and the suggestions
	 * are invisible. This function forces the InputMethodManager to show our window.
	 * WARNING! While this is running, it is not possible to load or display suggestions,
	 * or change the composing text. Use with care, after all processing is done.
	 */
	protected void forceShowWindowIfHidden() {
		if (getInputMode().isPassthrough() || isInputViewShown() || settings.isMainLayoutStealth()) {
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			requestShowSelf(InputMethodManager.SHOW_IMPLICIT);
		} else {
			showWindow(true);
		}
	}


	@Override
	protected void setStatusIcon(int iconResource) {
		if (iconResource > 0 && settings.isStatusIconEnabled()) {
			showStatusIcon(iconResource);
		} else {
			hideStatusIcon();
		}
	}


	@Override
	protected void setStatusText(String status) {
		statusBar.setText(status);
	}


	@Override
	protected boolean shouldBeVisible() {
		return !getInputMode().isPassthrough();
	}


	@Override
	protected boolean shouldBeOff() {
		return currentInputConnection == null || mInputMode.isPassthrough();
	}


	/**** Informational methods for the on-screen keyboard ****/

	public int getTextCase() {
		return mInputMode.getTextCase();
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

	public SettingsStore getSettings() {
		return settings;
	}
}
