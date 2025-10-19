package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdFilterClear;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyFilter extends BaseSoftKeyWithIcons {
	public SoftKeyFilter(Context context) { super(context); }
	public SoftKeyFilter(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyFilter(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected void handleHold() {
		preventRepeat();
		if (new CmdFilterClear().run(tt9)) {
			vibrate(Vibration.getHoldVibration());
			ignoreLastPressedKey();
		}
	}

	@Override
	protected boolean handleRelease() {
		return new CmdFilterSuggestions().run(tt9, getLastPressedKey() == getId());
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override protected int getCentralIcon() {
		if (tt9 != null) {
			if (tt9.isFilteringFuzzy()) return new CmdFilterSuggestions().getIconFuzzy();
			if (tt9.isFilteringOn()) return new CmdFilterSuggestions().getIconExact();
		}
		return new CmdFilterSuggestions().getIcon();
	}

	@Override protected int getCornerIcon(int position) {
		return position == ICON_POSITION_TOP_RIGHT ? new CmdFilterClear().getIcon() : super.getCornerIcon(position);
	}

	@Override
	public void render() {
		resetIconCache();
		if (tt9 != null) {
			setEnabled(
				tt9.isFilteringSupported()
				&& !tt9.isInputModeABC()
				&& !tt9.isInputModeNumeric()
				&& !tt9.isVoiceInputActive()
				&& !tt9.isFnPanelVisible()
			);
		}

		super.render();
	}
}
