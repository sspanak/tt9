package io.github.sspanak.tt9.ui;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;

public class NumpadButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {
	int number;
	TraditionalT9 ims;

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
	 * call this after shift is pressed or similar, to change the button text from abc to ABC
	 */
	public void invalidateText(Language language) {
		ArrayList<String> chars = language.getKeyCharacters(number, false);
		SpannableStringBuilder sb = new SpannableStringBuilder(String.valueOf(number));
		sb.append('\n');
		for (String c: chars) {
			sb.append(c);
		}
		sb.setSpan(new RelativeSizeSpan(0.5f), 1, sb.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

		setText(sb);
	}


	public void setIMS(TraditionalT9 ims) {
		this.ims = ims;
		super.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
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
		ims.onKeyDown(keycode, new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
		ims.onKeyUp(keycode, new KeyEvent(KeyEvent.ACTION_UP, keycode));
	}
}
