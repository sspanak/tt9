package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdMoveCursor;
import io.github.sspanak.tt9.commands.CmdNextInputMode;
import io.github.sspanak.tt9.commands.Command;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SoftKeyNumberSwipeable extends SoftKeyNumber {
	private float previousX;
	private final Command[] swipeCommand = { null, null };
	private boolean swipeCommandRan = false;

	public SoftKeyNumberSwipeable(Context context) { super(context); }
	public SoftKeyNumberSwipeable(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumberSwipeable(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	public void setTT9(TraditionalT9 tt9) {
		super.setTT9(tt9);
		isSwipeable = false;

		if (tt9 != null) {
			final int number = getNumber();
			final String actionKey = ActionKey.numberToActionKey(number);

			isSwipeable =
				(number == 0 && tt9.getSettings().getMoveCursorWithSpace())
				|| (getNumber() > 0 && !(tt9.getSettings().getSwipeLeftCommand(actionKey).isEmpty() && tt9.getSettings().getSwipeRightCommand(actionKey).isEmpty()));
		}
	}


	@Override
	protected String getRightText() { return is0Swipeable() && getNumber() == 0 ? getContext().getString(R.string.key_dpad_right) : super.getRightText(); }


	@Override
	protected String getLeftText() { return is0Swipeable() && getNumber() == 0 ? getContext().getString(R.string.key_dpad_left) : super.getLeftText(); }


	@Override
	protected int getCornerIcon(int position) {
		return switch (position) {
			case ICON_POSITION_BOTTOM_LEFT -> {
				Command cmd = getSwipeCommand(true);
				yield cmd != null ? cmd.getIcon() : -1;
			}
			case ICON_POSITION_BOTTOM_RIGHT -> {
				Command cmd = getSwipeCommand(false);
				yield cmd != null ? cmd.getIcon() : -1;
			}
			default -> -1;
		};
	}


	@Override
	protected float getCornerElementScale(int position) {
		if ((position == ICON_POSITION_BOTTOM_LEFT || position == ICON_POSITION_BOTTOM_RIGHT) && !(getSwipeCommand(position == ICON_POSITION_BOTTOM_LEFT) instanceof CmdNextInputMode)) {
			return super.getCornerElementScale(position) * 0.75f;
		}

		return super.getCornerElementScale(position);
	}


	@Override
	protected float getHoldDurationThreshold() {
		return SettingsStore.SOFT_KEY_REPEAT_DELAY * 10;
	}


	@Override
	protected boolean handlePress() {
		swipeCommandRan = false;
		return super.handlePress();
	}


	@Override
	protected void handleStartSwipeX(float screenX, float d) {
		previousX = screenX;
	}


	/**
	 * 0-key swipe function. Move the text cursor left or right.
	 */
	@Override
	protected void handleSwipeX(float screenX, float delta) {
		if (is0Swipeable()) {
			moveCursor(screenX);
		}
	}


	/**
	 * All other number keys swipe function. Execute the assigned command, and set the swipeCommandRan
	 * flag to avoid triggering the default press-release method. Or keep the flag off, if no action.
	 */
	@Override
	protected void handleEndSwipeX(float position, float delta) {
		Command cmd = getSwipeCommand(delta < 0);
		if (cmd != null) {
			swipeCommandRan = cmd.run(tt9);
		}
	}


	@Override
	protected boolean handleRelease() {
		return swipeCommandRan || super.handleRelease();
	}


	@Nullable
	private Command getSwipeCommand(boolean left) {
		if (tt9 == null || getNumber() == 0 || isFnPanelOn()) {
			return null;
		}

		final String aKey = ActionKey.numberToActionKey(getNumber());

		String currentCommandId = left ? tt9.getSettings().getSwipeLeftCommand(aKey) : tt9.getSettings().getSwipeRightCommand(aKey);
		Command cachedCommand = left ? swipeCommand[0] : swipeCommand[1];

		if (cachedCommand == null || !cachedCommand.getId().equals(currentCommandId)) {
			cachedCommand = CommandCollection.getById(CommandCollection.COLLECTION_HOTKEYS, currentCommandId);
			swipeCommand[left ? 0 : 1] = cachedCommand;
			resetIconCache();
		}

		return cachedCommand;
	}


	@Override
	public boolean isDynamic() {
		return getSwipeCommand(false) instanceof CmdNextInputMode || getSwipeCommand(true) instanceof CmdNextInputMode;
	}


	private boolean is0Swipeable() {
		boolean fnPanelOn = isFnPanelOn();

		return
			isSwipeable
			&& getNumber() == 0
			&& !LanguageKind.isKorean(tt9 != null ? tt9.getLanguage() : null)
			&& (!fnPanelOn || (tt9 != null && tt9.isTextEditingActive()));
	}


	/**
	 * Swipe function for the 0-key. Move the text cursor left or right, and set the swipeCommandRan
	 * flag to avoid triggering the default press-release method. Or keep the flag off, if no action.
	 */
	private void moveCursor(float screenX) {
		float delta = previousX - screenX;
		if (tt9 == null || Math.abs(delta) <= tt9.getSettings().MOVE_CURSOR_WITH_SPACE_THRESHOLD) {
			return;
		}

		previousX = screenX;
		new CmdMoveCursor().run(tt9, delta > 0 ? CmdMoveCursor.CURSOR_MOVE_LEFT : CmdMoveCursor.CURSOR_MOVE_RIGHT);
		swipeCommandRan = true;
	}
}
