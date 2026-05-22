package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdEditAdjacentLetter;
import io.github.sspanak.tt9.commands.CmdMoveCursor;
import io.github.sspanak.tt9.commands.CmdSuggestionNext;
import io.github.sspanak.tt9.commands.CmdSuggestionPrevious;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyArrow extends BaseSoftKeyCustomizable {
	private final CmdEditAdjacentLetter editNextLetter = new CmdEditAdjacentLetter();
	private final CmdMoveCursor moveCursor = new CmdMoveCursor();

	private boolean held;
	private boolean editedNext;
	private int direction = 0;

	public SoftKeyArrow(Context context) { super(context); }
	public SoftKeyArrow(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyArrow(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private void determineDirection() {
		if (getId() == R.id.soft_key_left_arrow || getId() == R.id.soft_key_extra_left_arrow) {
			direction = -1;
		} else if (getId() == R.id.soft_key_right_arrow || getId() == R.id.soft_key_extra_right_arrow) {
			direction = 1;
		}
	}


	public boolean isLeft() {
		if (direction == 0) {
			determineDirection();
		}
		return direction < 0;
	}


	public boolean isRight() {
		if (direction == 0) {
			determineDirection();
		}
		return direction > 0;
	}


	public boolean isSmall() {
		return getId() == R.id.soft_key_left_arrow || getId() == R.id.soft_key_right_arrow;
	}


	@Override
	protected boolean handlePress() {
		editedNext = false;
		held = false;
		return super.handlePress();
	}

	@Override
	protected void handleHold() {
		if (editNextLetter.isAvailable(tt9)) {
			preventRepeat();
			editedNext = editNextLetter.run(tt9, isLeft());
		} else {
			held = true;
			moveCursor();
		}
	}

	@Override
	protected boolean handleRelease() {
		if (editedNext || held) {
			vibrate(Vibration.getReleaseVibration());
			return true;
		} else {
			return moveCursor();
		}
	}

	private boolean moveCursor() {
		if (!validateTT9Handler()) {
			return false;
		}

		if (isLeft()) return onLeft();
		if (isRight()) return onRight();

		return false;
	}

	private boolean onLeft() {
		return new CmdSuggestionPrevious().run(tt9) || moveCursor.run(tt9, CmdMoveCursor.CURSOR_MOVE_LEFT);
	}

	private boolean onRight() {
		return new CmdSuggestionNext().run(tt9) || moveCursor.run(tt9, CmdMoveCursor.CURSOR_MOVE_RIGHT);
	}


	@Override
	protected String getAccessibilityText() {
		if (isLeft()) return getContext().getString(R.string.accessibility_key_left);
		if (isRight()) return getContext().getString(R.string.accessibility_key_right);

		return super.getAccessibilityText();
	}


	private void setSmallVisibility() {
		if (tt9 == null || !tt9.getSettings().isMainLayoutNumpad() || !isSmall()) {
			return;
		}

		final int visibility = tt9 != null && tt9.getSettings().getArrowsLeftRight() ? VISIBLE : GONE;
		setVisibility(visibility);
		getOverlayWrapper();
		if (overlay != null) {
			overlay.setVisibility(visibility);
		}
	}


	@Override
	public void render() {
		setSmallVisibility();
		super.render();
	}
}
