package io.github.sspanak.tt9.ime.helpers;

import android.content.Context;
import android.os.Build;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;

public class InputField {
	public static final int IME_ACTION_ENTER = EditorInfo.IME_MASK_ACTION + 1;

	protected final InputConnection connection;
	protected final EditorInfo field;


	protected InputField(InputConnection inputConnection, EditorInfo inputField) {
		connection = inputConnection;
		field = inputField;
	}


	public boolean equals(InputConnection inputConnection, EditorInfo inputField) {
		return
			connection != null && connection == inputConnection
			&& field != null && field == inputField;
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
		switch (standardAction) {
			case EditorInfo.IME_ACTION_DONE:
			case EditorInfo.IME_ACTION_GO:
			case EditorInfo.IME_ACTION_NEXT:
			case EditorInfo.IME_ACTION_PREVIOUS:
			case EditorInfo.IME_ACTION_SEARCH:
			case EditorInfo.IME_ACTION_SEND:
			case EditorInfo.IME_ACTION_UNSPECIFIED:
				return standardAction;
			default:
				return IME_ACTION_ENTER;
		}
	}


	/**
	 * performAction
	 * Sends an action ID to the connected application. Usually, the action is determined with "this.getAction()".
	 * Note that it is up to the app to decide what to do or ignore the action ID.
	 */
	public boolean performAction(int actionId) {
		return connection != null && actionId != EditorInfo.IME_ACTION_NONE && connection.performEditorAction(actionId);
	}


	/**
	 * getLanguage
	 * Detects the language hint of the current field and returns a TT9-friendly Language object.
	 * If the language is not supported, or the field has no hint, for example it's a numeric field or
	 * it's a text field where the language doesn't matter, the function returns null.
	 */
	@Nullable
	public Language getLanguage(Context context, ArrayList<Integer> allowedLanguageIds) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			return null;
		}

		for (int i = 0; field.hintLocales != null && i < field.hintLocales.size(); i++) {
			Language lang = LanguageCollection.getByLanguageCode(context, field.hintLocales.get(i).getLanguage());
			if (lang != null && allowedLanguageIds.contains(lang.getId())) {
				return lang;
			}
		}

		return null;
	}
}
