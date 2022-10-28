package io.github.sspanak.tt9.ime;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.UI;

class SoftKeyHandler implements View.OnTouchListener {
	private static final int[] buttons = { R.id.main_left, R.id.main_mid, R.id.main_right };
	private final TraditionalT9 tt9;
	private View view = null;

	public SoftKeyHandler(LayoutInflater layoutInflater, TraditionalT9 tt9) {
		this.tt9 = tt9;

		createView(layoutInflater);
	}


	View createView(LayoutInflater layoutInflater) {
		if (view == null) {
			view = layoutInflater.inflate(R.layout.mainview, null);

			for (int buttonId : buttons) {
				view.findViewById(buttonId).setOnTouchListener(this);
			}
		}

		return view;
	}

	View getView() {
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


	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		int buttonId = view.getId();

		if (buttonId == R.id.main_left && action == MotionEvent.ACTION_UP) {
			UI.showPreferencesScreen(tt9);
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
