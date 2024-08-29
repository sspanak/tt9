package io.github.sspanak.tt9.ime;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.modes.ModePredictive;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.dialogs.PopupDialog;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.SystemSettings;

public class TraditionalT9 extends MainViewHandler {
	@NonNull
	private final Handler normalizationHandler = new Handler(Looper.getMainLooper());
	private final Handler deathDetector = new Handler(Looper.getMainLooper());


	@Override
	public boolean onEvaluateInputViewShown() {
		super.onEvaluateInputViewShown();
		setInputField(getCurrentInputConnection(), getCurrentInputEditorInfo());

		if (!shouldBeVisible()) {
			return false;
		}

		if (SystemSettings.isTT9Enabled(this)) {
			return true;
		} else {
			onDeath();
			return false;
		}
	}


	@Override
	public boolean onEvaluateFullscreenMode() {
		return false;
	}


	@Override
	public View onCreateInputView() {
		mainView.forceCreateInputView();
		initTray();
		setDarkTheme();
		statusBar.setText(mInputMode);
		suggestionOps.set(mInputMode.getSuggestions(), mInputMode.containsGeneratedSuggestions());

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
		settings.setDemoMode(false);
		Logger.setLevel(settings.getLogLevel());
		WordStoreAsync.init(this);
		super.onInit();
	}


	@Override
	protected boolean onStart(InputConnection connection, EditorInfo field) {
		deathDetector.removeCallbacksAndMessages(null);

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
		stopVoiceInput();
		onFinishTyping();
		suggestionOps.clear();
		setStatusIcon(mInputMode);
		statusBar.setText(mInputMode);

		if (isInputViewShown()) {
			updateInputViewShown();
		}

		normalizationHandler.removeCallbacksAndMessages(null);
		normalizationHandler.postDelayed(
			() -> { if (!DictionaryLoader.getInstance(this).isRunning()) WordStoreAsync.normalizeNext(); },
			SettingsStore.WORD_NORMALIZATION_DELAY
		);
	}


	/**
	 * On Android 11 the IME is sometimes not killed when the user switches to a different one.
	 * Here we attempt to detect if we are disabled, then hide and kill ourselves.
	 */
	private void onDeath() {
		if (SystemSettings.isTT9Active(this)) {
			Logger.w("onDeath", "===> Still active, rescheduling");
			deathDetector.postDelayed(this::onDeath, SettingsStore.ZOMBIE_CHECK_INTERVAL);
			return;
		} else {
			deathDetector.removeCallbacksAndMessages(null);
		}

		Logger.w("onDeath", "===> Killing self");
		requestHideSelf(0);
		onStop();
		normalizationHandler.removeCallbacksAndMessages(null);
		stopSelf();
	}


	@Override
	public void onDestroy() {
		onDeath();
		super.onDestroy();
	}


	@Override
	protected boolean onNumber(int key, boolean hold, int repeat) {
		if (mInputMode instanceof ModePredictive && DictionaryLoader.autoLoad(this, mLanguage)) {
			return true;
		}
		return super.onNumber(key, hold, repeat);
	}


	@Override
	protected TraditionalT9 getFinalContext() {
		return this;
	}
}
