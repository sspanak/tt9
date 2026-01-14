package io.github.sspanak.tt9.ime.helpers;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.commands.CmdMoveCursor;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

abstract class InputField {
	public static final int IME_ACTION_ENTER = EditorInfo.IME_MASK_ACTION + 1;

	@NonNull public final String id;
	@Nullable protected final InputMethodService ims;
	@Nullable protected final EditorInfo field;


	protected InputField(@Nullable InputMethodService ims, @Nullable EditorInfo inputField) {
		id = generateId(inputField);
		this.ims = ims;
		field = inputField;
	}


	@NonNull
	private static String generateId(@Nullable EditorInfo inputField) {
		if (inputField == null) {
			return "";
		}

		return inputField.packageName + ":" + inputField.inputType + ":" + inputField.imeOptions;
	}


	@Nullable
	protected InputConnection getConnection() {
		return ims != null ? ims.getCurrentInputConnection() : null;
	}


	public boolean equals(InputConnection inputConnection, EditorInfo inputField) {
		return
			inputConnection != null && inputConnection == getConnection()
			&& id.equals(generateId(inputField));
	}


	/**
	 * getAction
	 * Returns the most appropriate action for the "OK" key. It could be "send", "act as ENTER key", "go (to URL)" and so on.
	 */
	public int getAction() {
		if (field == null) {
			return EditorInfo.IME_ACTION_NONE;
		}

		// custom actions handling as in LatinIME. See the example in OpenBoard repo:
		// https://github.com/openboard-team/openboard/blob/master/app/src/main/java/org/dslul/openboard/inputmethod/latin/utils/InputTypeUtils.java#L107
		if (field.actionId == EditorInfo.IME_ACTION_DONE || field.actionLabel != null) {
			return IME_ACTION_ENTER;
		} else if (field.actionId > 0) {
			return field.actionId;
		}

		// As in LatinIME, we want to perform an editor action, including in the case of "IME_ACTION_UNSPECIFIED".
		// Otherwise, we pass through the ENTER or DPAD_CENTER key press and let the app or the system decide what to do.
		// See the example below:
		// https://github.com/openboard-team/openboard/blob/c3772cd56e770975ea5570db903f93b199de8b32/app/src/main/java/org/dslul/openboard/inputmethod/latin/inputlogic/InputLogic.java#L756
		int standardAction = field.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
		return switch (standardAction) {
			case EditorInfo.IME_ACTION_DONE,
					 EditorInfo.IME_ACTION_GO,
					 EditorInfo.IME_ACTION_NEXT,
					 EditorInfo.IME_ACTION_PREVIOUS,
					 EditorInfo.IME_ACTION_SEARCH,
					 EditorInfo.IME_ACTION_SEND,
					 EditorInfo.IME_ACTION_UNSPECIFIED
				-> standardAction;
			default -> IME_ACTION_ENTER;
		};
	}


	/**
	 * performAction
	 * Sends an action ID to the connected application. Usually, the action is determined with "this.getAction()".
	 * Note that it is up to the app to decide what to do or ignore the action ID.
	 */
	public boolean performAction(int actionId) {
		InputConnection connection = getConnection();
		return connection != null && actionId != EditorInfo.IME_ACTION_NONE && connection.performEditorAction(actionId);
	}


	/**
	 * getLanguage
	 * Detects the language hint of the current field and returns a TT9-friendly Language object.
	 * If the language is not supported, or the field has no hint, for example it's a numeric field or
	 * it's a text field where the language doesn't matter, the function returns null.
	 */
	@Nullable
	public Language getLanguage(ArrayList<Integer> allowedLanguageIds) {
		if (!DeviceInfo.AT_LEAST_ANDROID_7 || field == null || field.hintLocales == null) {
			return null;
		}

		for (int i = 0; i < field.hintLocales.size(); i++) {
			Language lang = LanguageCollection.getByLanguageCode(field.hintLocales.get(i).getLanguage());
			if (lang != null && allowedLanguageIds.contains(lang.getId())) {
				return lang;
			}
		}

		return null;
	}


	public boolean moveCursor(int direction) {
		final boolean backward = direction == CmdMoveCursor.CURSOR_MOVE_LEFT;
		final boolean forward = direction == CmdMoveCursor.CURSOR_MOVE_RIGHT;

		if (
			(backward && getTextBeforeCursor(null, 1).isEmpty())
			|| (forward && getTextAfterCursor(null, 1).isEmpty())
		) {
			return false;
		}

		final int keyCode = switch (direction) {
			case CmdMoveCursor.CURSOR_MOVE_UP -> KeyEvent.KEYCODE_DPAD_UP;
			case CmdMoveCursor.CURSOR_MOVE_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN;
			case CmdMoveCursor.CURSOR_MOVE_LEFT -> KeyEvent.KEYCODE_DPAD_LEFT;
			case CmdMoveCursor.CURSOR_MOVE_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT;
			default -> KeyEvent.KEYCODE_UNKNOWN;
		};


		sendDownUpKeyEvents(keyCode);

		return true;
	}


	public boolean sendDownUpKeyEvents(int keyCode) {
		return sendDownUpKeyEvents(keyCode, false, false);
	}


	public boolean sendDownUpKeyEvents(int keyCode, boolean shift, boolean ctrl) {
		InputConnection connection = getConnection();
		if (connection != null) {
			int metaState = shift ? KeyEvent.META_SHIFT_ON : 0;
			metaState |= ctrl ? KeyEvent.META_CTRL_ON : 0;
			KeyEvent downEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, metaState);
			KeyEvent upEvent = new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, metaState);
			return connection.sendKeyEvent(downEvent) && connection.sendKeyEvent(upEvent);
		}

		return false;
	}


	@NonNull protected abstract Text getTextAfterCursor(@Nullable Language language, int numberOfChars);
	@NonNull protected abstract Text getTextBeforeCursor(@Nullable Language language, int numberOfChars);
}
