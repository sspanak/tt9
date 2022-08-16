package io.github.sspanak.tt9.ime;

import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.UI;

public class SoftKeyHandler implements View.OnTouchListener {
	private static final int[] buttons = { R.id.main_left, R.id.main_right, R.id.main_mid };
	private final TraditionalT9 parent;

	public SoftKeyHandler(View mainView, TraditionalT9 tt9) {
		this.parent = tt9;
		changeView(mainView);
	}


	public void changeView(View v) {
		for (int buttonId : buttons) {
			View button = v.findViewById(buttonId);
			button.setOnTouchListener(this);
		}
	}


	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		int buttonId = view.getId();

		if (buttonId == R.id.main_left && action == MotionEvent.ACTION_UP) {
			UI.showPreferencesScreen(parent);
			return view.performClick();
		}

		if (buttonId == R.id.main_mid && action == MotionEvent.ACTION_UP) {
			parent.onOK();
			return view.performClick();
		}

		if (buttonId == R.id.main_right && action == MotionEvent.AXIS_PRESSURE) {
			return parent.handleBackspaceHold();
		}

		return false;
	}
}
