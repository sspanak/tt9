package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyFilter extends BaseSoftKeyWithIcons {
	public SoftKeyFilter(Context context) { super(context); }
	public SoftKeyFilter(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyFilter(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	private boolean isKorean() {
		return tt9 != null && LanguageKind.isKorean(tt9.getLanguage());
	}

	@Override
	protected void handleHold() {
		if (isKorean()) {
			handleRelease();
			return;
		}

		preventRepeat();
		if (validateTT9Handler() && tt9.onKeyFilterClear(false)) {
			vibrate(Vibration.getHoldVibration());
			ignoreLastPressedKey();
		}
	}


	@Override
	protected boolean handleRelease() {
		if (!validateTT9Handler()) {
			return false;
		}

		if (isKorean()) {
			return tt9.onKeySpaceKorean(false);
		} else {
			return tt9.onKeyFilterSuggestions(false, getLastPressedKey() == getId());
		}
	}


	@Override protected String getTitle() { return isKorean() ? "␣" : ""; }
	@Override protected int getCentralIcon() { return isKorean() ? 0 : R.drawable.ic_fn_filter; }
	@Override protected int getHoldIcon() { return isKorean() ? 0 : R.drawable.ic_fn_filter_off; }

	@Override protected float getTitleScale() { return isKorean() ? 1.3f * Math.min(1, getTT9Height()) * getScreenScaleY() : super.getTitleScale(); }

	@Override
	public void render() {
		if (tt9 != null) {
			setEnabled(
				!tt9.isInputModeNumeric()
				&& !tt9.isInputModeABC()
				&& !tt9.isVoiceInputActive()
				&& (
					LanguageKind.isKorean(tt9.getLanguage())
					|| (tt9.isFilteringSupported() && !tt9.isTextEditingActive())
				)
			);
		}

		super.render();
	}
}
