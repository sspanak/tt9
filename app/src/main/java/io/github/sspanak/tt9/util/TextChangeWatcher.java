package io.github.sspanak.tt9.util;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;

import java.util.function.Consumer;

public record TextChangeWatcher(Consumer<Editable> onChange) implements TextWatcher {
	public TextChangeWatcher(@Nullable Consumer<Editable> onChange) {
		this.onChange = onChange;
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (onChange != null) onChange.accept(s);
	}

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
