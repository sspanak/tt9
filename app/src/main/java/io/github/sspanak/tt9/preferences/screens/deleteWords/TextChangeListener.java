package io.github.sspanak.tt9.preferences.screens.deleteWords;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.preferences.SettingsStore;

class TextChangeListener implements TextWatcher {
	private static TextChangeListener self;

	@NonNull private ConsumerCompat<String> onChange;
	@NonNull private final Handler debouncer = new Handler(Looper.getMainLooper());

	private TextChangeListener(@NonNull ConsumerCompat<String> onChange) {
		this.onChange = onChange;
	}

	static TextChangeListener getInstance(@NonNull ConsumerCompat<String> onChange) {
		if (self == null) {
			self = new TextChangeListener(onChange);
		}

		self.onChange = onChange;
		return self;
	}

	@Override public void afterTextChanged(Editable s) {
		debouncer.removeCallbacksAndMessages(null);
		debouncer.postDelayed(() -> onChange.accept(s.toString()), SettingsStore.DELETE_WORDS_SEARCH_DELAY);
	}

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
