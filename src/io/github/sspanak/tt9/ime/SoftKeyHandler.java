package io.github.sspanak.tt9.ime;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashSet;

import androidx.core.content.ContextCompat;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.NumpadButton;
import io.github.sspanak.tt9.ui.UI;

class SoftKeyHandler implements View.OnClickListener {
	private static final int[] buttons = { R.id.main_left, R.id.main_mid, R.id.main_right, R.id.soft_mode, R.id.soft_language, R.id.soft_addWord };
	private HashSet<NumpadButton> buttons_number = new HashSet<>();
	private final TraditionalT9 tt9;
	private View view = null;
	protected SettingsStore settings;
	private long lastBackspaceCall = 0;

	public SoftKeyHandler(TraditionalT9 tt9) {
		this.tt9 = tt9;

		settings = new SettingsStore(tt9.getApplicationContext());
		getView();
	}


	View getView() {
		if (view != null){
			//check whether mainview setting has changed
			boolean prefShowNumpad = settings.getShowSoftNumpad();
			if ((view.getId() == R.id.mainview_small && prefShowNumpad) ||
					(view.getId() == R.id.mainview_with_numpad && !prefShowNumpad)){
				view = null;
			}
		}

		if (view == null) {
			if (settings.getShowSoftNumpad()){
				view = View.inflate(tt9.getApplicationContext(), R.layout.mainview_with_numpad, null);
			}else{
				view = View.inflate(tt9.getApplicationContext(), R.layout.mainview_small, null);
			}

			for (int buttonId : buttons) {
				View button = view.findViewById(buttonId);
				if (button == null){
					continue; //as this button is only in numpad mainview included
				}
				button.setOnClickListener(this);

				if (buttonId == R.id.main_right){
					//implement holding / moving with finger on back button for deleting letters
					button.setOnTouchListener(new View.OnTouchListener() {
						@Override
						public boolean onTouch(View view, MotionEvent motionEvent) {
							if (motionEvent.getAction() == MotionEvent.AXIS_PRESSURE) {
								return handleBackspaceHold();
							}
							return false;
						}
					});
				}
			}


			ViewGroup softNumpad = view.findViewById(R.id.main_soft_keys);
			for (int r = 0; r < softNumpad.getChildCount(); r++) {
				Log.d("test", softNumpad.getClass().toString());
				View child = softNumpad.getChildAt(r);
				if (child instanceof ViewGroup) {
					//this happens only if R.layout.mainview_with_numpad is inflated
					ViewGroup row = (ViewGroup) child;
					for (int b = 0; b < row.getChildCount(); b++) {
						Log.d("test", row.getClass().toString());
						View button = row.getChildAt(b);
						if (button instanceof NumpadButton) {
							NumpadButton nb = (NumpadButton) button;
							buttons_number.add(nb);
							nb.setIMS(tt9);
						}
					}
				}
			}
			tt9.setInputView(view);
		}

		return view;
	}


	void show() {
		//First call getView to force an update of the view in case settings have changed.
		//Does nothing, if nothing has changed.
		getView();

		if (view != null) {
			view.setVisibility(View.VISIBLE);
			invalidateNumpadButtonsText();
		}
	}

	/**
	 * calls {@link NumpadButton#invalidateText} for all Numpad Buttons.
	 */
	void invalidateNumpadButtonsText(){
		for (NumpadButton nb: buttons_number){
			nb.invalidateText(tt9.mLanguage);
		}
	}


	void hide() {
		if (view != null) {
			view.setVisibility(View.GONE);
		}
	}


	void setSoftKeysVisibility(boolean visible) {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}


	/** setDarkTheme
	 * Changes the main view colors according to the theme.
	 *
	 * We need to do this manually, instead of relying on the Context to resolve the appropriate colors,
	 * because this View is part of the main service View. And service Views are always locked to the
	 * system context and theme.
	 *
	 * More info:
	 * https://stackoverflow.com/questions/72382886/system-applies-night-mode-to-views-added-in-service-type-application-overlay
	 */
	void setDarkTheme(boolean darkEnabled) {
		if (view == null) {
			return;
		}

		// background
		view.findViewById(R.id.main_soft_keys).setBackground(ContextCompat.getDrawable(
			view.getContext(),
			darkEnabled ? R.drawable.button_background_dark : R.drawable.button_background
		));

		// text
		int textColor = ContextCompat.getColor(
			view.getContext(),
			darkEnabled ? R.color.dark_button_text : R.color.button_text
		);

		for (int buttonId : buttons) {
			Button button = view.findViewById(buttonId);
			if (button == null){
				continue; //as this button is only in numpad mainview included
			}
			button.setTextColor(textColor);
		}

		// separators
		Drawable separatorColor = ContextCompat.getDrawable(
			view.getContext(),
			darkEnabled ? R.drawable.button_separator_dark : R.drawable.button_separator
		);

//		view.findViewById(R.id.main_separator_left).setBackground(separatorColor);
//		view.findViewById(R.id.main_separator_right).setBackground(separatorColor);
	}


	private boolean handleBackspaceHold() {
		if (System.currentTimeMillis() - lastBackspaceCall < tt9.settings.getSoftKeyRepeatDelay()) {
			return true;
		}

		boolean handled = tt9.onBackspace();

		long now = System.currentTimeMillis();
		lastBackspaceCall = lastBackspaceCall == 0 ? tt9.settings.getSoftKeyInitialDelay() + now : now;

		return handled;
	}


	private boolean handleBackspaceUp() {
		lastBackspaceCall = 0;
		invalidateNumpadButtonsText();
		return true;
	}

	public boolean handleDpadNavigation(int keyCode){
		if (!settings.getDpadSoftkeysNavigation()){
			return false;
		}
		View focusedView = view.findFocus();
		if ((focusedView == null || focusedView.getId() == R.id.main_suggestions_list)
				&& keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			Button settingsButton = view.findViewById(R.id.main_left);
//			settingsButton.setFocusableInTouchMode(true); // only for debugging (also in NumpadButton.java)
			return settingsButton.requestFocus();
		}else if(focusedView != null && focusedView.getId() != R.id.main_suggestions_list){
			int direction;
			switch (keyCode){
				case KeyEvent.KEYCODE_DPAD_UP: direction = View.FOCUS_UP; break;
				case KeyEvent.KEYCODE_DPAD_DOWN: direction = View.FOCUS_DOWN; break;
				case KeyEvent.KEYCODE_DPAD_LEFT: direction = View.FOCUS_LEFT; break;
				case KeyEvent.KEYCODE_DPAD_RIGHT: direction = View.FOCUS_RIGHT; break;
				case KeyEvent.KEYCODE_DPAD_CENTER: return focusedView.performClick();
				default: throw new RuntimeException("Unsupported dpad key code");
			}
			View nextFocused = focusedView.focusSearch(direction);
			if (nextFocused != null){
				nextFocused.requestFocus();
			}
			return true;
		}

		return false;
	}


	@Override
	public void onClick(View view) {
		int buttonId = view.getId();

		if (buttonId == R.id.main_left) {
			UI.showSettingsScreen(tt9);
		}

		if (buttonId == R.id.main_mid) {
			tt9.onOK();
			invalidateNumpadButtonsText();
		}
		if (buttonId == R.id.soft_mode) {
			tt9.onKeyNextInputMode();
			invalidateNumpadButtonsText();
		}
		if (buttonId == R.id.soft_addWord) {
			tt9.onKeyAddWord();
		}
		if (buttonId == R.id.soft_language) {
			tt9.onKeyNextLanguage();
			invalidateNumpadButtonsText();
		}
		if (buttonId == R.id.main_right) {
			handleBackspaceUp();
		}

//		int keycodeToForward = -1;
//		if (buttonId == R.id.soft_pound){
//			keycodeToForward = KeyEvent.KEYCODE_POUND;
//		} else if (buttonId == R.id.soft_star){
//			keycodeToForward = KeyEvent.KEYCODE_STAR;
//		}
//		if (keycodeToForward != -1){
//			if (action == MotionEvent.ACTION_DOWN){
//				return tt9.onKeyDown(keycodeToForward,
//					new KeyEvent(KeyEvent.ACTION_DOWN, keycodeToForward));
//			} else if (action == MotionEvent.ACTION_UP){
//				return tt9.onKeyUp(keycodeToForward,
//					new KeyEvent(KeyEvent.ACTION_UP, keycodeToForward));
//			}
//		}
	}
}
