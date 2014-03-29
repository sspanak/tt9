package org.nyanya.android.traditionalt9;

import android.app.Dialog;
import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Arrays;

public abstract class AbsSymDialog extends Dialog implements
		View.OnClickListener {

	protected Context context;
	private View mainview;
	private int pagenum = 1;
	private int pageoffset = (pagenum - 1) * 10;

	private int MAX_PAGE;
	private boolean started;

	private static final int[] buttons = {
		R.id.text_keyone,   R.id.text_keytwo,
		R.id.text_keythree, R.id.text_keyfour,  R.id.text_keyfive,
		R.id.text_keysix,   R.id.text_keyseven, R.id.text_keyeight,
		R.id.text_keynine,  R.id.text_keyzero
	};
	private static final int[] buttons2 = {
		R.id.text_keystar,
		R.id.text_keypound
	};

	public AbsSymDialog(Context c, View mv) {
		super(c);
		context = c;
		mainview = mv;
		started = true;
		setContentView(mv);

		View button;
		for (int butt : buttons) {
			button = mv.findViewById(butt);
			button.setOnClickListener(this);
		}
		for (int butt : buttons2) {
			button = mv.findViewById(butt);
			button.setOnClickListener(this);
		}
		MAX_PAGE = getMaxPage();
	}

	// must return a string array the same size as the length of the button string array.
	abstract String[] getContentDescription();

	@Override
	public void onClick(View v) {
		// Log.d("SymbolPopup - onClick", "click happen: " + v);
		switch (v.getId()) {
		case R.id.text_keyone:
			sendChar(pageoffset);
			break;
		case R.id.text_keytwo:
			sendChar(pageoffset + 1);
			break;
		case R.id.text_keythree:
			sendChar(pageoffset + 2);
			break;
		case R.id.text_keyfour:
			sendChar(pageoffset + 3);
			break;
		case R.id.text_keyfive:
			sendChar(pageoffset + 4);
			break;
		case R.id.text_keysix:
			sendChar(pageoffset + 5);
			break;
		case R.id.text_keyseven:
			sendChar(pageoffset + 6);
			break;
		case R.id.text_keyeight:
			sendChar(pageoffset + 7);
			break;
		case R.id.text_keynine:
			sendChar(pageoffset + 8);
			break;
		case R.id.text_keyzero:
			sendChar(pageoffset + 9);
			break;

		case R.id.text_keypound:
			pageChange(1);
			break;
		case R.id.text_keystar:
			pageChange(-1);
			break;
		}
	}

	protected abstract String getSymbol(int index);
	protected abstract String getTitleText();
	protected abstract int getSymbolSize();
	protected abstract int getMaxPage();

	private void sendChar(int index) {
		// Log.d("SymbolDialog - sendChar", "Sending index: " + index);

		if (index < getSymbolSize()) {
			((KeyboardView.OnKeyboardActionListener) context).onText(getSymbol(index));
			// then close
			pagenum = 1;
			pageoffset = (pagenum - 1) * 10;
			this.dismiss();
		}
	}

	private void pageChange(int amt) {
		pagenum = pagenum + amt;
		if (pagenum > MAX_PAGE) {
			pagenum = 1;
		} else if (pagenum < 1) {
			pagenum = MAX_PAGE;
		}
		pageoffset = (pagenum - 1) * 10;
		updateButtons();
	}

	private void updateButtons() {
		// set page number text
		setTitle(String.format("%s\t\t%s", getTitleText(),
				context.getResources().getString(R.string.symbol_page, pagenum, MAX_PAGE)));
		// update button labels
		int symbx = pageoffset;
		int stop = symbx + 9;
		int nomore = stop;
		int symsize = getSymbolSize();
		if (nomore >= symsize - 1) {
			nomore = symsize - 1;
		}
		TextView tv;
		String[] cd = getContentDescription();

		for (int buttx = 0; symbx <= stop; symbx++) {
			// Log.d("SymbolDialog - updateButtons", "buttx: " + buttx +
			// " symbx: " + symbx);
			if (symbx > nomore) {
				((TextView) mainview.findViewById(buttons[buttx])).setText("");
			} else {
				tv = (TextView) mainview.findViewById(buttons[buttx]);
				tv.setText(String.valueOf(getSymbol(symbx)));
				if (cd != null) {
					tv.setContentDescription(cd[symbx]);
				}
			}
			buttx++;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() != 0) {
			return true;
		}
		if (started) {
			started = false;
		}
		// TODO: remove emulator special keys
		switch (keyCode) {
		case 75:
			keyCode = KeyEvent.KEYCODE_POUND;
			break;
		case 74:
			keyCode = KeyEvent.KEYCODE_STAR;
			break;
		}
		// Log.d("AbsSymDialog.onKeyDown", "bootan pres: " + keyCode);
		switch (keyCode) {

		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
		case KeyEvent.KEYCODE_POUND:
		case KeyEvent.KEYCODE_STAR:
			event.startTracking();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Log.d("AbsSymDialog.onKeyUp", "Key: " + keyCode);
		if (started) {
			started = false;
			return true;
		}
		// TODO: remove emulator special keys
		switch (keyCode) {
		case 75:
			keyCode = KeyEvent.KEYCODE_POUND;
			break;
		case 74:
			keyCode = KeyEvent.KEYCODE_STAR;
			break;
		}
		switch (keyCode) {
		// pass-through
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
		case KeyEvent.KEYCODE_POUND:
		case KeyEvent.KEYCODE_STAR:
			onKey(keyCode);
			return true;
		default:
			// KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD).getNumber(keyCode)
			// Log.w("onKeyUp", "Unhandled Key: " + keyCode + "(" +
			// event.toString() + ")");
		}
		return super.onKeyUp(keyCode, event);
	}

	private void onKey(int keyCode) {
		// Log.d("OnKey", "pri: " + keyCode);
		// Log.d("onKey", "START Cm: " + mCapsMode);
		// HANDLE SPECIAL KEYS
		switch (keyCode) {
		case KeyEvent.KEYCODE_1:
			sendChar(pageoffset);
			break;
		case KeyEvent.KEYCODE_2:
			sendChar(pageoffset + 1);
			break;
		case KeyEvent.KEYCODE_3:
			sendChar(pageoffset + 2);
			break;
		case KeyEvent.KEYCODE_4:
			sendChar(pageoffset + 3);
			break;
		case KeyEvent.KEYCODE_5:
			sendChar(pageoffset + 4);
			break;
		case KeyEvent.KEYCODE_6:
			sendChar(pageoffset + 5);
			break;
		case KeyEvent.KEYCODE_7:
			sendChar(pageoffset + 6);
			break;
		case KeyEvent.KEYCODE_8:
			sendChar(pageoffset + 7);
			break;
		case KeyEvent.KEYCODE_9:
			sendChar(pageoffset + 8);
			break;
		case KeyEvent.KEYCODE_0:
			sendChar(pageoffset + 9);
			break;
		case KeyEvent.KEYCODE_STAR:
			pageChange(-1);
			break;
		case KeyEvent.KEYCODE_POUND:
			pageChange(1);
			break;
		}
	}

	protected void doShow(View v) {
		// based on http://stackoverflow.com/a/13962770
		started = true;
		Window win = getWindow();
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.token = v.getWindowToken();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		win.setAttributes(lp);
		win.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		updateButtons();
		try {
			show();
		} catch (Exception e) {
			Log.e("AbsSymDialog", "Cannot create Dialog:");
			Log.e("AbsSymDialog", Arrays.toString(e.getStackTrace()));
		}
	}
}
