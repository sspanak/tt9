package io.github.sspanak.tt9.util;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;

public class TextChangeWatcher implements TextWatcher {
	private final ConsumerCompat<Editable> onChange;

	public TextChangeWatcher(@Nullable ConsumerCompat<Editable> onChange) {
		this.onChange = onChange;
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (onChange != null) onChange.accept(s);
	}

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
