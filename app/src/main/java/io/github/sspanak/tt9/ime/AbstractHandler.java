package io.github.sspanak.tt9.ime;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract public class AbstractHandler extends InputMethodService {
	// hardware key handlers
	abstract protected boolean onBack();
	abstract public boolean onBackspace();
	abstract public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly);
	abstract protected boolean onNumber(int key, boolean hold, int repeat);
	abstract public boolean onOK();
	abstract public boolean onText(String text, boolean validateOnly); // used for "#", "*" and whatnot

	// helpers
	abstract public SettingsStore getSettings();
	abstract protected void onInit();
	abstract protected void onStart(InputConnection inputConnection, EditorInfo inputField);
	abstract protected void onFinishTyping();
	abstract protected void onStop();
	abstract protected void setInputField(InputConnection inputConnection, EditorInfo inputField);

	// UI
	abstract protected View createMainView();
	abstract protected void createSuggestionBar(View mainView);
	abstract protected void forceShowWindowIfHidden();
	abstract protected void renderMainView();
	abstract protected void setStatusIcon(int iconResource);
	abstract protected void setStatusText(String status);
	abstract protected boolean shouldBeVisible();
	abstract protected boolean shouldBeOff();
}
