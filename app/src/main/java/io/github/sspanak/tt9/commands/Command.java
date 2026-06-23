package io.github.sspanak.tt9.commands;

import android.content.Context;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;

public interface Command {
	/**
	 * The unique command identifier. It could be used for saving user preferences, for example "key_add_word".
	 */
	String getId();


	/**
	 * The drawable resource id for the command icon.
	 */
	int getIcon();


	/**
	 * The string resource id for the command name, displayed in the Settings.
	 */
	int getName();


	/**
	 * Same as getName(), but returns the actual string.
	 */
	@Nullable
	default String getName(@Nullable Context context) { return context == null ? null : context.getString(getName()); }


	/**
	 * The hard key number (0-9) that triggers this command, or -1 if none.
	 */
	default int getHardKey() { return -1; }


	/**
	 * The id of the command-palette key (R.id.soft_key_*) that triggers this command, or 0 if none.
	 */
	default int getPaletteKey() { return 0; }


	/**
	 * Returns true if the command is available. Useful for enabling/disabling SoftKeys.
	 */
	default boolean isAvailable(@Nullable TraditionalT9 tt9) { return isAvailableStd(tt9); }


	/**
	 * A standard availability check for commands that just require the keyboard to be active.
	 */
	default boolean isAvailableStd(@Nullable TraditionalT9 tt9) {
		return
			tt9 != null
			&& !tt9.shouldBeOff()
			&& (tt9.getSettings().isMainLayoutLarge() || tt9.isInputViewShown());
	}


	/**
	 * Executes the command action.
	 * @return true if the command was executed successfully, false otherwise.
	 */
	default boolean run(@Nullable TraditionalT9 tt9) { return false; }


	/**
	 * Executes the command action when triggered from a hotkey, replacing the former HotkeyHandler code.
	 */
	default boolean runFromHotkey(@Nullable TraditionalT9 tt9, boolean validateOnly) {
		return isAvailable(tt9) && (validateOnly || run(tt9));
	}
}
