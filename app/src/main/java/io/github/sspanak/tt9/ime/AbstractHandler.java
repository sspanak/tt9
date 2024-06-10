package io.github.sspanak.tt9.ime;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import io.github.sspanak.tt9.ime.modes.InputMode;

abstract public class AbstractHandler extends InputMethodService {
	// hardware key handlers
	abstract protected boolean onBack();
	abstract public boolean onBackspace();
	abstract public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly);
	abstract protected boolean onNumber(int key, boolean hold, int repeat);
	abstract public boolean onOK();
	abstract public boolean onText(String text, boolean validateOnly); // used for "#", "*" and whatnot

	abstract protected void onInit();
	abstract protected boolean onStart(InputConnection inputConnection, EditorInfo inputField);
	abstract protected void onFinishTyping();
	abstract protected void onStop();
	abstract protected void setInputField(InputConnection inputConnection, EditorInfo inputField);

	// UI
	abstract protected void createSuggestionBar(View mainView);
	abstract protected void resetStatus();


	abstract protected InputMode getInputMode();
	abstract protected int getInputModeId();
	abstract protected SuggestionOps getSuggestionOps();
	abstract protected boolean shouldBeOff();
	abstract protected TraditionalT9 getFinalContext();
}
