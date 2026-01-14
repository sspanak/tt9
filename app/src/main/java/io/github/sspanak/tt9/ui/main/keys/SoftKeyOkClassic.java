package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdEditDuplicateLetter;
import io.github.sspanak.tt9.commands.CmdFilterClear;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.commands.CmdMoveCursor;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SoftKeyOkClassic extends SoftKeyOk {
	@NonNull private final CmdFilterClear clear = new CmdFilterClear();
	@NonNull private final CmdFilterSuggestions filter = new CmdFilterSuggestions();
	@NonNull private final CmdEditDuplicateLetter duplicateLetter = new CmdEditDuplicateLetter();
	@NonNull private final CmdMoveCursor moveCursor = new CmdMoveCursor();

	public SoftKeyOkClassic(Context context) { super(context); }
	public SoftKeyOkClassic(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyOkClassic(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected void initColors(@NonNull SettingsStore settings) {
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
		if (isSwipeable) {
			if (delta < 0) onUp();
			else onDown();
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


	private void onUp() {
		if (duplicateLetter.run(tt9) || filter.run(tt9, getLastPressedKey() == getId())) {
			return;
		}

		moveCursor.run(tt9, CmdMoveCursor.CURSOR_MOVE_UP);
	}


	private void onDown() {
		if (!clear.run(tt9)) {
			moveCursor.run(tt9, CmdMoveCursor.CURSOR_MOVE_DOWN);
		}
	}
}
