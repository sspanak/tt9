package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftCommandKey extends SoftNumberKey {
	public SoftCommandKey(Context context) {
		super(context);
	}

	public SoftCommandKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftCommandKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected String getTitle() {
		return getNumber(getId()) <= 3 ? super.getTitle() : null;
	}

	@Override
	protected String getSubTitle() {
		if (tt9 == null) {
			return null;
		}

		int number = getNumber(getId());

		switch (number) {
			case 0:
				return "Back";
			case 1:
				return getContext().getString(R.string.function_add_word);
			case 2:
				return getContext().getString(R.string.function_show_settings);
			case 3:
				return getContext().getString(R.string.function_change_keyboard);
		}

		return null;
	}
}
