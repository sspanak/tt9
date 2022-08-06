package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.TraditionalT9;

public class InterfaceHandler implements View.OnClickListener, View.OnLongClickListener {

	private static final int[] buttons = { R.id.main_left, R.id.main_right, R.id.main_mid };
	private TraditionalT9 parent;
	private View mainview;

	public InterfaceHandler(View mainview, TraditionalT9 iparent) {
		this.parent = iparent;
		changeView(mainview);
	}

	public View getMainview() {
		return mainview;
	}


	public void changeView(View v) {
		this.mainview = v;
		for (int buttid : buttons) {
			View button = v.findViewById(buttid);
			button.setOnClickListener(this);
		}
	}

	public void setPressedInUI(int keyCode, boolean pressed) {
		int id = 0;
		switch (keyCode) {
		case KeyEvent.KEYCODE_SOFT_LEFT:
			id = R.id.main_left;
			break;
		case KeyEvent.KEYCODE_SOFT_RIGHT:
			id = R.id.main_right;
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			id = R.id.main_mid;
			break;
		}
		if (id != 0) {
			((View) mainview.findViewById(id)).setPressed(pressed);
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.main_left:
				parent.showPreferencesScreen();
				break;

			case R.id.main_right:
				parent.handleBackspace();
				break;
		}
	}


	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == R.id.main_right) {
			parent.handleBackspace();
			return true;
		}

		return false;
	}

	public void hideView() {
		mainview.setVisibility(View.GONE);
	}

	public void showView() {
		mainview.setVisibility(View.VISIBLE);
	}
}
