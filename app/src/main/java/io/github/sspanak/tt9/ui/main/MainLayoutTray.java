package io.github.sspanak.tt9.ui.main;

import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

class MainLayoutTray extends BaseMainLayout {
	MainLayoutTray(TraditionalT9 tt9) {
		super(tt9, R.layout.main_small);
	}

	protected void setSoftKeysVisibility() {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.GONE);
		}
	}

	@Override
	public void render() {
		getView();
		enableClickHandlers();
		setSoftKeysVisibility();
	}

	@Override
	public void setDarkTheme(boolean darkEnabled) {
		if (view == null) {
			return;
		}

		// background
		view.findViewById(R.id.main_soft_keys).setBackground(ContextCompat.getDrawable(
			view.getContext(),
			darkEnabled ? R.drawable.button_background_dark : R.drawable.button_background
		));
	}

	@Override
	protected ArrayList<SoftKey> getKeys() {
		return keys;
	}
}
