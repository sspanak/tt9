package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import org.jspecify.annotations.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdFilterClear;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.commands.CmdMoveCursor;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class SoftKeyOkWithFilter extends SoftKeyOk {
	@NonNull private final CmdFilterClear clear = new CmdFilterClear();
	@NonNull private final CmdFilterSuggestions filter = new CmdFilterSuggestions();
	@NonNull private final CmdMoveCursor moveCursor = new CmdMoveCursor();

	public SoftKeyOkWithFilter(Context context) { super(context); }
	public SoftKeyOkWithFilter(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyOkWithFilter(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	public void setTT9(TraditionalT9 tt9) {
		super.setTT9(tt9);
		isSwipeable = tt9 != null && tt9.getSettings().getArrowsUpDown();
	}


	@Override
	protected void handleEndSwipeY(float position, float delta) {
		if (delta < 0) {
			if (!isSwipeable || !filter.run(tt9, getLastPressedKey() == getId())) {
				moveCursor.run(tt9, CmdMoveCursor.CURSOR_MOVE_UP);
			}
		} else {
			if (!isSwipeable || !clear.run(tt9)) {
				moveCursor.run(tt9, CmdMoveCursor.CURSOR_MOVE_DOWN);
			}
		}
	}


	@Override
	protected boolean handleRelease() {
		return notSwiped() && super.handleRelease();
	}


	@Override
	protected String getTopText() {
		return getContext().getString(R.string.key_dpad_up);
	}


	@Override
	protected String getBottomText() {
		return getContext().getString(R.string.key_dpad_down);
	}
}
