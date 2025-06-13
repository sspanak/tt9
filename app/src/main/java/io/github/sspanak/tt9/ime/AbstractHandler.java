package io.github.sspanak.tt9.ime;

import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.EditorInfo;

import io.github.sspanak.tt9.ime.helpers.SuggestionOps;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.util.Ternary;

abstract public class AbstractHandler extends InputMethodService {
	// hardware key handlers
	abstract protected Ternary onBack();
	abstract public boolean onBackspace(int repeat);
	abstract public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly);
	abstract protected boolean onNumber(int key, boolean hold, int repeat);
	abstract public boolean onOK();
	abstract public boolean onText(String text, boolean validateOnly); // used for "#", "*" and whatnot

	// lifecycle
	abstract protected void onInit();
	abstract protected boolean onStart(EditorInfo inputField);
	abstract protected void onFinishTyping();
	abstract protected void onStop();
	abstract protected void setInputField(EditorInfo inputField);

	// UI
	abstract protected void createSuggestionBar();
	abstract protected void resetStatus();

	// informational
	abstract protected InputMode determineInputMode();
	abstract protected int determineInputModeId();
	abstract protected SuggestionOps getSuggestionOps();
	abstract protected boolean shouldBeOff();
	abstract protected TraditionalT9 getFinalContext();
}
