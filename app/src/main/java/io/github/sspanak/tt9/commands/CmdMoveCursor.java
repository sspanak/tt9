package io.github.sspanak.tt9.commands;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;

public class CmdMoveCursor implements Command {
	public static final int CURSOR_MOVE_UP = 0;
	public static final int CURSOR_MOVE_DOWN = 1;
	public static final int CURSOR_MOVE_LEFT = 2;
	public static final int CURSOR_MOVE_RIGHT = 3;

	public static final String ID = "move_cursor";
	public String getId() { return ID; }
	public int getIcon() { return -1; }
	public int getName() { return 0; }


	public boolean run(@Nullable TraditionalT9 tt9, int direction) {
		if (tt9 == null || tt9.getTextSelection() == null) {
			return false;
		}

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.getInputMode().onAcceptSuggestion(tt9.getSuggestionOps().acceptIncomplete());
		tt9.getMindReader().clearContext();
		tt9.resetKeyRepeat();

		final boolean backward = direction == CmdMoveCursor.CURSOR_MOVE_LEFT;

		if (tt9.getTextSelection().isEmpty()) {
			return
				tt9.getAppHacks().onMoveCursor(direction)
				|| (backward && tt9.onTrimTrailingSpace(false))
				|| tt9.getTextField().moveCursor(direction);
		} else {
			tt9.getTextSelection().clear(backward);
			return true;
		}
	}
}
