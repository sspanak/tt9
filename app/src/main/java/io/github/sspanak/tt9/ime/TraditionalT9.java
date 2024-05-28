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

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.ModePredictive;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.dialogs.PopupDialog;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;
import io.github.sspanak.tt9.util.Logger;

public class TraditionalT9 extends MainViewOps {
	@NonNull
	private final Handler normalizationHandler = new Handler(Looper.getMainLooper());
	private MainView mainView = null;
	private StatusBar statusBar = null;


	@Override
	public boolean onEvaluateInputViewShown() {
		super.onEvaluateInputViewShown();
		setInputField(getCurrentInputConnection(), getCurrentInputEditorInfo());
		return shouldBeVisible();
	}


	@Override
	public boolean onEvaluateFullscreenMode() {
		return false;
	}


	// Some devices have their own super.onCreateXxxView(), which does some magic allowing our MainView to remain visible.
	// This is why we must call the super method, instead of returning null, as in the AOSP code.
	@Override
	public View onCreateInputView() {
		mainView.forceCreateInputView();
		initTray();
		setDarkTheme();
		setStatusText(mInputMode.toString());
		suggestionOps.set(mInputMode.getSuggestions());

		return mainView.getView();
	}


	@Override
	public void onComputeInsets(Insets outInsets) {
		super.onComputeInsets(outInsets);
		if (shouldBeVisible() && settings.clearInsets()) {
			// otherwise the MainView wouldn't show up on Sonim XP3900
			// or it expands the application window past the edge of the screen
			outInsets.contentTopInsets = 0;
		}
	}


	@Override
	public void onStartInput(EditorInfo inputField, boolean restarting) {
		Logger.i(
			"KeyPadHandler",
			"===> Start Up; packageName: " + inputField.packageName + " inputType: " + inputField.inputType + " actionId: " + inputField.actionId + " imeOptions: " + inputField.imeOptions + " privateImeOptions: " + inputField.privateImeOptions + " extras: " + inputField.extras
		);
		onStart(getCurrentInputConnection(), inputField);
	}


	@Override
	public void onStartInputView(EditorInfo inputField, boolean restarting) {
		onStart(getCurrentInputConnection(), inputField);
	}


	@Override
	public void onFinishInputView(boolean finishingInput) {
		super.onFinishInputView(finishingInput);
		onFinishTyping();
	}


	@Override
	public void onFinishInput() {
		super.onFinishInput();
		onStop();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int result = super.onStartCommand(intent, flags, startId);

		String message = intent != null ? intent.getStringExtra(PopupDialog.INTENT_CLOSE) : null;
		if (message != null) {
			forceShowWindow();
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
		displayMainView();
		if (!isInputViewShown()) {
			updateInputViewShown();
		}
	}


	@Override
	protected boolean onStart(InputConnection connection, EditorInfo field) {
		if (!super.onStart(connection, field)) {
			return false;
		}

		Logger.setLevel(settings.getLogLevel());

		if (mInputMode.isPassthrough()) {
			onStop();
		}	else {
			normalizationHandler.removeCallbacksAndMessages(null);
			initUi();
		}

		if (new InputType(connection, field).isNotUs(this)) {
			DictionaryLoader.autoLoad(this, mLanguage);
		}

		return true;
	}


	@Override
	protected void onStop() {
		onFinishTyping();
		suggestionOps.clear();
		setStatusIcon(mInputMode);
		setStatusText(mInputMode.toString());

		if (isInputViewShown()) {
			updateInputViewShown();
		}

		normalizationHandler.removeCallbacksAndMessages(null);
		normalizationHandler.postDelayed(
			() -> { if (!DictionaryLoader.getInstance(this).isRunning()) WordStoreAsync.normalizeNext(); },
			SettingsStore.WORD_NORMALIZATION_DELAY
		);
	}


	@Override
	protected boolean onNumber(int key, boolean hold, int repeat) {
		if (mInputMode instanceof ModePredictive && DictionaryLoader.autoLoad(this, mLanguage)) {
			return true;
		}
		return super.onNumber(key, hold, repeat);
	}


	protected boolean isMainViewCommandPalette() {
		return mainView.isCommandPalette();
	}


	@Override
	protected void displayCommandsView() {
		mainView.createCommandsView();
		initTray();
		setDarkTheme();
		setStatusText("Select a Command");
		mainView.render();
		setInputView(mainView.getView());
	}


	protected void displayMainView() {
		if (mainView.createInputView()) {
			initTray();
		}
		setDarkTheme();
		setStatusIcon(mInputMode);
		setStatusText(mInputMode.toString());
		mainView.render();
		setInputView(mainView.getView());
	}


	/**
	 * Populates the UI elements with strings and icons
	 */
	@Override
	protected void renderMainView() {
		mainView.render();
	}


	/**
	 * forceShowWindow
	 * Some applications may hide our window and it remains invisible until the screen is touched or OK is pressed.
	 * This is fine for touchscreen keyboards, but the hardware keyboard allows typing even when the window and the suggestions
	 * are invisible. This function forces the InputMethodManager to show our window.
	 * WARNING! Calling this may cause a restart, which will cause InputMode to be recreated. Depending
	 * on how much time the restart takes, this may erase the current user input.
	 */
	@Override
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


	@Override
	protected void setStatusIcon(InputMode mode) {
		if (!mode.isPassthrough() && settings.isStatusIconEnabled()) {
			showStatusIcon(R.drawable.ic_status);
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
		return getInputModeId() != InputMode.MODE_PASSTHROUGH && !settings.isMainLayoutStealth();
	}


	@Override
	protected boolean shouldBeOff() {
		return currentInputConnection == null || mInputMode.isPassthrough();
	}
}
