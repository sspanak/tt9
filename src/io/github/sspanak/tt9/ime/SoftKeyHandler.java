package io.github.sspanak.tt9.ime;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.UI;

class SoftKeyHandler implements View.OnTouchListener {
	private static final int[] buttons = { R.id.main_left, R.id.main_mid, R.id.main_right };
	private final TraditionalT9 tt9;
	private View view = null;

	public SoftKeyHandler(TraditionalT9 tt9) {
		this.tt9 = tt9;

		getView();
	}


	View getView() {
		if (view == null) {
			view = LayoutInflater.from(tt9.getApplicationContext()).inflate(R.layout.mainview, null);

			for (int buttonId : buttons) {
				view.findViewById(buttonId).setOnTouchListener(this);
			}
		}

		return view;
	}


	void show() {
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
	}


	void hide() {
		if (view != null) {
			view.setVisibility(View.GONE);
		}
	}


	void setSoftKeysVisibility(boolean visible) {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(visible ? LinearLayout.VISIBLE : LinearLayout.GONE);
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

		view.findViewById(R.id.main_separator_left).setBackground(separatorColor);
		view.findViewById(R.id.main_separator_right).setBackground(separatorColor);
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

		if (buttonId == R.id.main_right && action == MotionEvent.AXIS_PRESSURE) {
			return tt9.handleBackspaceHold();
		}

		return false;
	}
}
