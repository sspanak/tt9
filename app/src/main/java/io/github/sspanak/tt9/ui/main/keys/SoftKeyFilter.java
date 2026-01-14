package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.commands.CmdEditDuplicateLetter;
import io.github.sspanak.tt9.commands.CmdFilterClear;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyFilter extends BaseSoftKeyWithIcons {
	@NonNull private final CmdFilterClear clear = new CmdFilterClear();
	@NonNull private final CmdFilterSuggestions filter = new CmdFilterSuggestions();
	@NonNull private final CmdEditDuplicateLetter duplicateLetter = new CmdEditDuplicateLetter();

	public SoftKeyFilter(Context context) { super(context); }
	public SoftKeyFilter(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyFilter(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected void handleHold() {
		preventRepeat();
		if (!duplicateLetter.isAvailable(tt9) && clear.run(tt9)) {
			vibrate(Vibration.getHoldVibration());
			ignoreLastPressedKey();
		}
	}

	@Override
	protected boolean handleRelease() {
		return duplicateLetter.run(tt9) || filter.run(tt9, getLastPressedKey() == getId());
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	protected int getCentralIcon() {
		return duplicateLetter.isAvailable(tt9) ? duplicateLetter.getIcon() : filter.getDynamicIcon(tt9);
	}

	@Override
	protected int getCornerIcon(int position) {
		return position == ICON_POSITION_TOP_RIGHT && !duplicateLetter.isAvailable(tt9) ? clear.getIcon() : super.getCornerIcon(position);
	}

	@Override
	public void render() {
		resetIconCache();
		setEnabled(filter.isAvailable(tt9) || duplicateLetter.isAvailable(tt9));
		super.render();
	}
}
