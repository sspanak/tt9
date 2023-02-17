package io.github.sspanak.tt9.ime;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;

import java.util.HashSet;

import androidx.core.content.ContextCompat;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.NumpadButton;
import io.github.sspanak.tt9.ui.UI;

class SoftKeyHandler implements View.OnTouchListener {
	private static final int[] buttons = { R.id.main_left, R.id.main_mid, R.id.main_right, R.id.soft_mode, R.id.soft_language, R.id.soft_addWord };
	private HashSet<NumpadButton> buttons_number = new HashSet<>();
	private final TraditionalT9 tt9;
	private View view = null;
	protected SettingsStore settings;
	private long lastBackspaceCall = 0;

	public SoftKeyHandler(TraditionalT9 tt9) {
		this.tt9 = tt9;

		getView();
		settings = new SettingsStore(tt9.getApplicationContext());
	}


	View getView() {
		if (view == null) {
			view = View.inflate(tt9.getApplicationContext(), R.layout.mainview, null);

			for (int buttonId : buttons) {
				view.findViewById(buttonId).setOnTouchListener(this);
			}

			ViewGroup softNumpad = view.findViewById(R.id.main_soft_keys);
			for (int r = 0; r < softNumpad.getChildCount(); r++) {
				Log.d("test", softNumpad.getClass().toString());
				ViewGroup row = (ViewGroup) softNumpad.getChildAt(r);
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

		return view;
	}


	void show() {
		if (view != null) {
			view.setVisibility(View.VISIBLE);
			for (NumpadButton nb: buttons_number){
				nb.invalidateText(tt9.mLanguage);
			}
		}
	}


	void hide() {
		if (view != null) {
			view.setVisibility(View.GONE);
		}
	}


	void setSoftKeysVisibility(boolean visible) {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(visible ? TableLayout.VISIBLE : TableLayout.GONE);
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
		return true;
	}


	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		int buttonId = view.getId();

		if (buttonId == R.id.main_left && action == MotionEvent.ACTION_UP) {
			UI.showSettingsScreen(tt9);
			return view.performClick();
		}

		if (buttonId == R.id.main_mid && action == MotionEvent.ACTION_UP) {
			tt9.onOK();
			return view.performClick();
		}
		if (buttonId == R.id.soft_mode && action == MotionEvent.ACTION_UP) {
			tt9.onKeyNextInputMode();
			return view.performClick();
		}
		if (buttonId == R.id.soft_addWord && action == MotionEvent.ACTION_UP) {
			tt9.onKeyAddWord();
			return view.performClick();
		}
		if (buttonId == R.id.soft_language && action == MotionEvent.ACTION_UP) {
			tt9.onKeyNextLanguage();
			return view.performClick();
		}

		if (buttonId == R.id.main_right) {
			if (action == MotionEvent.AXIS_PRESSURE) {
				return handleBackspaceHold();
			} else if (action == MotionEvent.ACTION_UP) {
				return handleBackspaceUp();
			}
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


		return false;
	}
}
