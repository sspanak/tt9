package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import org.jspecify.annotations.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdFilterClear;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.commands.CmdMoveCursor;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SoftKeyOkWithWithArrows extends SoftKeyOk {
	@NonNull private final CmdFilterClear clear = new CmdFilterClear();
	@NonNull private final CmdFilterSuggestions filter = new CmdFilterSuggestions();
	@NonNull private final CmdMoveCursor moveCursor = new CmdMoveCursor();

	public SoftKeyOkWithWithArrows(Context context) { super(context); }
	public SoftKeyOkWithWithArrows(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyOkWithWithArrows(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected void initColors(@androidx.annotation.NonNull SettingsStore settings) {
		super.initColors(settings);
		cornerElementColor = textColor;
	}


	@Override
	public void setTT9(TraditionalT9 tt9) {
		super.setTT9(tt9);
		isSwipeable = tt9 != null && tt9.getSettings().getArrowsUpDown();
	}


	@Override
	protected void handleEndSwipeY(float position, float delta) {
		if (!isSwipeable) {
			return;
		}

		if (delta < 0) {
			if (!filter.run(tt9, getLastPressedKey() == getId())) {
				moveCursor.run(tt9, CmdMoveCursor.CURSOR_MOVE_UP);
			}
		} else {
			if (!clear.run(tt9)) {
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
		return isSwipeable ? getContext().getString(R.string.key_dpad_up) : "";
	}


	@Override
	protected String getBottomText() {
		return isSwipeable ? getContext().getString(R.string.key_dpad_down) : "";
	}
}
