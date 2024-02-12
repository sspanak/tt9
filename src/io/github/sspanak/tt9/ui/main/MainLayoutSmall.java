package io.github.sspanak.tt9.ui.main;

import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

class MainLayoutSmall extends BaseMainLayout {
	MainLayoutSmall(TraditionalT9 tt9) {
		super(tt9, R.layout.main_small);
	}

	private void setSoftKeysVisibility() {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(tt9.getSettings().getShowSoftKeys() ? LinearLayout.VISIBLE : LinearLayout.GONE);
		}
	}

	@Override
	public void render() {
		getView();
		enableClickHandlers();
		setSoftKeysVisibility();
	}

	@Override
	final public void setDarkTheme(boolean darkEnabled) {
		if (view == null) {
			return;
		}

		// background
		view.findViewById(R.id.main_soft_keys).setBackground(ContextCompat.getDrawable(
			view.getContext(),
			darkEnabled ? R.drawable.button_background_dark : R.drawable.button_background
		));

		// text
		for (SoftKey key : getKeys()) {
			key.setDarkTheme(darkEnabled);
		}

		// separators
		Drawable separatorColor = ContextCompat.getDrawable(
			view.getContext(),
			darkEnabled ? R.drawable.button_separator_dark : R.drawable.button_separator
		);

		view.findViewById(R.id.main_separator_left).setBackground(separatorColor);
		view.findViewById(R.id.main_separator_right).setBackground(separatorColor);
	}


	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (view != null && (keys == null || keys.size() == 0)) {
			keys = getKeysFromContainer(view.findViewById(R.id.main_soft_keys));
		}
		return keys;
	}
}
