package io.github.sspanak.tt9.ui;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;

public class NumpadButton extends androidx.appcompat.widget.AppCompatButton implements View.OnTouchListener {
	int number;
	TraditionalT9 ims;
	int textCase;
	Language language;

	public NumpadButton(Context context) {
		super(context);
		setDefaults();
	}

	public NumpadButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDefaults();
	}

	public NumpadButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setDefaults();

	}

	protected void setDefaults(){
		number = Integer.parseInt(getText().toString());
	}


	/**
	 * call this if anything might have changed which causes a change in shown letters on the buttons
	 * (e.g. after language is changed, to update the button texts e.g. from Latin to Kyrillic chars)
	 * No additional pre-checks whether it really changed are necessary.
	 *
	 * @return returns whether it was necessary to invalidate the texts or whether not (e.g. neither language nor TextCase changed)
	 */
	public boolean invalidateText(Language language) {
		if (this.language == language && textCase == ims.getInputMode().getTextCase()){
			//no change in visualization necessary
			return false;
		}

		//update in case it changed
		textCase = ims.getInputMode().getTextCase();
		this.language = language;

		ArrayList<String> chars = language.getKeyCharacters(number, false);
		SpannableStringBuilder sb = new SpannableStringBuilder(String.valueOf(number));
		sb.append('\n');
		for (String c: chars) {
			if (textCase == InputMode.CASE_UPPER) {
				c = c.toUpperCase(language.getLocale());
			}
			sb.append(c);
		}
		sb.setSpan(new RelativeSizeSpan(0.5f), 1, sb.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

		setText(sb);
		return true;
	}




	public void setIMS(TraditionalT9 ims) {
		this.ims = ims;
		super.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		int keycode;
		switch (number) {
			case 0: keycode = KeyEvent.KEYCODE_0; break;
			case 1: keycode = KeyEvent.KEYCODE_1; break;
			case 2: keycode = KeyEvent.KEYCODE_2; break;
			case 3: keycode = KeyEvent.KEYCODE_3; break;
			case 4: keycode = KeyEvent.KEYCODE_4; break;
			case 5: keycode = KeyEvent.KEYCODE_5; break;
			case 6: keycode = KeyEvent.KEYCODE_6; break;
			case 7: keycode = KeyEvent.KEYCODE_7; break;
			case 8: keycode = KeyEvent.KEYCODE_8; break;
			case 9: keycode = KeyEvent.KEYCODE_9; break;
			default: throw new RuntimeException("NumpadButton - unsupported number key code");
		}

		//since physical key events can not reliable simulated, directly send it to the TT9 implementation
		boolean consumedByTT9;
		if (motionEvent.getAction() == KeyEvent.ACTION_DOWN){
			consumedByTT9 = ims.onKeyDown(keycode, new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
		}else if (motionEvent.getAction() == KeyEvent.ACTION_UP){
			consumedByTT9 = ims.onKeyUp(keycode, new KeyEvent(KeyEvent.ACTION_UP, keycode));
		}else {
			return false;
		}
		if (!consumedByTT9){
			//means ims is in number mode, send the number key event directly
			ims.getCurrentInputConnection().sendKeyEvent(new KeyEvent(motionEvent.getAction(), keycode));
		}
		return false;
	}
}
