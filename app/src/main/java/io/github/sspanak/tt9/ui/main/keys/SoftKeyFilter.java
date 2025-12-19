package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.commands.CmdFilterClear;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyFilter extends BaseSoftKeyWithIcons {
	@NonNull private final CmdFilterClear clear = new CmdFilterClear();
	@NonNull private final CmdFilterSuggestions filter = new CmdFilterSuggestions();

	public SoftKeyFilter(Context context) { super(context); }
	public SoftKeyFilter(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyFilter(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected void handleHold() {
		preventRepeat();
		if (clear.run(tt9)) {
			vibrate(Vibration.getHoldVibration());
			ignoreLastPressedKey();
		}
	}

	@Override
	protected boolean handleRelease() {
		return filter.run(tt9, getLastPressedKey() == getId());
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override protected int getCentralIcon() {
		if (tt9 != null) {
			if (tt9.isFilteringFuzzy()) return filter.getIconFuzzy();
			if (tt9.isFilteringOn()) return filter.getIconExact();
		}
		return filter.getIcon();
	}

	@Override protected int getCornerIcon(int position) {
		return position == ICON_POSITION_TOP_RIGHT ? clear.getIcon() : super.getCornerIcon(position);
	}

	@Override
	public void render() {
		resetIconCache();
		setEnabled(filter.isAvailable(tt9));
		super.render();
	}
}
