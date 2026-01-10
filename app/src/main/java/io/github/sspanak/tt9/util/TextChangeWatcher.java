package io.github.sspanak.tt9.util;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;

public class TextChangeWatcher implements TextWatcher {
	private boolean ignoreNextChange = false;
	private final ConsumerCompat<Editable> onChange;

	public TextChangeWatcher(@Nullable ConsumerCompat<Editable> onChange) {
		this.onChange = onChange;
	}

	public void ignoreNextChange() {
		ignoreNextChange = true;
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (onChange != null && !ignoreNextChange) onChange.accept(s);
		ignoreNextChange = false;
	}

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
