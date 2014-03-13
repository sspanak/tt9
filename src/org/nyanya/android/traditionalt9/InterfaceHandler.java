package org.nyanya.android.traditionalt9;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class InterfaceHandler implements View.OnClickListener, View.OnLongClickListener {

	private static final int[] buttons = { R.id.main_left, R.id.main_right, R.id.main_mid };
	private TraditionalT9 parent;
	private View mainview;

	public InterfaceHandler(View mainview, TraditionalT9 iparent) {
		this.parent = iparent;
		changeView(mainview);
	}

	protected View getMainview() {
		return mainview;
	}


	protected void changeView(View v) {
		this.mainview = v;
		View button;
		for (int buttid : buttons) {
			button = v.findViewById(buttid);
			button.setOnClickListener(this);
			if (!parent.mAddingWord) {
				button.setOnLongClickListener(this);
			}
		}
	}

	protected void setPressed(int keyCode, boolean pressed) {
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

	protected void showNotFound(boolean notfound) {
		if (notfound) {
			((TextView) mainview.findViewById(R.id.left_hold_upper))
				.setText(R.string.main_left_notfound);
			((TextView) mainview.findViewById(R.id.left_hold_lower))
				.setText(R.string.main_left_insert);
		} else {
			((TextView) mainview.findViewById(R.id.left_hold_upper))
				.setText(R.string.main_left_insert);
			((TextView) mainview.findViewById(R.id.left_hold_lower))
				.setText(R.string.main_left_addword);
		}
	}

	protected void emulateMiddleButton() {
		((Button) mainview.findViewById(R.id.main_mid)).performClick();
	}

	protected void midButtonUpdate(boolean composing) {
		if (composing) {
			((TextView) mainview.findViewById(R.id.main_mid)).setText(R.string.main_mid_commit);
		} else {
			((TextView) mainview.findViewById(R.id.main_mid)).setText(R.string.main_mid);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_left:
			if (parent.mAddingWord) {
				parent.showSymbolPage();
			} else {
				if (parent.mWordFound) {
					parent.showSymbolPage();
				} else {
					parent.showAddWord();
				}
			}
			break;
		case R.id.main_mid:
			parent.handleMidButton();
			break;
		case R.id.main_right:
			parent.nextKeyMode();
			break;
		}
	}

	protected void showHold(boolean show) {
		ViewSwitcher vs = (ViewSwitcher) mainview.findViewById(R.id.main_left);
		if (show) {
			vs.setDisplayedChild(1);
		} else {
			vs.setDisplayedChild(0);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.main_left:
			parent.showAddWord();
			break;
		case R.id.main_right:
			parent.launchOptions();
			break;
		default:
			return false;
		}
		return true;
	}

	protected void hideView() {
		mainview.setVisibility(View.GONE);
	}

	protected void showView() {
		mainview.setVisibility(View.VISIBLE);
	}
}
