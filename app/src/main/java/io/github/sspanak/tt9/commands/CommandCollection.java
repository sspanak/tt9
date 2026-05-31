package io.github.sspanak.tt9.commands;

import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class CommandCollection {
	public static final int COLLECTION_HOTKEYS = 1;
	public static final int COLLECTION_PALETTE = 2;
	public static final int COLLECTION_SWIPE = 3;
	public static final int COLLECTION_TEXT_EDITING = 4;

	private static final HashMap<String, Command> searchCache = new HashMap<>();

	private static final ArrayList<Command> hotkeys = new ArrayList<>();
	private static final LinkedHashMap<Integer, Command> palette = new LinkedHashMap<>();
	private static final ArrayList<Command> swipe = new ArrayList<>();
	private static final LinkedHashMap<Integer, Command> textEditing = new LinkedHashMap<>();


	@Nullable
	public static Command getById(int collectionType, @Nullable String commandId) {
		if (NullCommand.ID.equals(commandId)) {
			return NullCommand.self;
		}

		Collection<Command> commands = switch (collectionType) {
			case COLLECTION_HOTKEYS -> getHotkeyCommands();
			case COLLECTION_SWIPE -> getSwipeCommands();
			default -> getAll(collectionType).values();
		};

		final String cacheKey = collectionType + "_" + commandId;
		Command cached = searchCache.get(cacheKey);
		if (cached != null) {
			return cached;
		}

		for (Command cmd : commands) {
			if (cmd.getId().equals(commandId)) {
				searchCache.put(cacheKey, cmd);
				return cmd;
			}
		}

		return null;
	}


	@NonNull
	public static Command getBySoftKey(int collectionType, int keyId) {
		Command cmd = getAll(collectionType).get(keyId);
		return cmd != null ? cmd : NullCommand.self;
	}


	@NonNull
	public static Command getByHardKey(int collectionType, int keyCode) {
		for (Command cmd : getAll(collectionType).values()) {
			if (cmd.getHardKey() == keyCode) {
				return cmd;
			}
		}

		return NullCommand.self;
	}


	@NonNull public static Command getByHotkey(@NonNull SettingsStore settings, int keyCode) {
		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			return NullCommand.self;
		}

		for (Command cmd : getHotkeyCommands()) {
			if (keyCode == settings.getFunctionKey(cmd.getId())) {
				return cmd;
			}
		}

		return NullCommand.self;
	}


	@NonNull
	public static LinkedHashMap<Integer, Command> getAll(int collectionType) {
		return switch(collectionType) {
			case COLLECTION_PALETTE -> getPaletteCommands();
			case COLLECTION_TEXT_EDITING -> getTextEditingCommands();
			default -> new LinkedHashMap<>();
		};
	}


	@NonNull
	public static ArrayList<Command> getHotkeyCommands() {
		if (hotkeys.isEmpty()) {
			hotkeys.add(new CmdAddWord());
			hotkeys.add(new CmdEditWord());
			hotkeys.add(new CmdBackspace());
			hotkeys.add(new CmdCommandPalette());
			hotkeys.add(new CmdEditText());
			hotkeys.add(new CmdFilterClear());
			hotkeys.add(new CmdFilterSuggestions());
			hotkeys.add(new CmdHideKeyboard());
			hotkeys.add(new CmdSuggestionPrevious());
			hotkeys.add(new CmdSuggestionNext());
			hotkeys.add(new CmdNextInputMode());
			hotkeys.add(new CmdNextLanguage());
			hotkeys.add(new CmdSelectKeyboard());
			hotkeys.add(new CmdShift());
			hotkeys.add(new CmdSpaceKorean());
			hotkeys.add(new CmdShowEmojis());
			hotkeys.add(new CmdShowSettings());
			hotkeys.add(new CmdTogglePredictiveMode());
			hotkeys.add(new CmdUndo());
			hotkeys.add(new CmdRedo());
			hotkeys.add(new CmdVoiceInput());
		}

		return hotkeys;
	}


	public static ArrayList<Command> getSwipeCommands() {
		if (swipe.isEmpty()) {
			swipe.addAll(getHotkeyCommands());
			swipe.add(new CmdTxtCut());
			swipe.add(new CmdTxtCopy());
			swipe.add(new CmdTxtPaste());
		}

		return swipe;
	}


	private static void addPaletteCommand(@NonNull Command cmd) {
		palette.put(cmd.getPaletteKey(), cmd);
	}


	@NonNull
	private static LinkedHashMap<Integer, Command> getPaletteCommands() {
		if (palette.isEmpty()) {
			addPaletteCommand(new CmdAddWord());
			addPaletteCommand(new CmdEditWord());
			addPaletteCommand(new CmdVoiceInput());
			addPaletteCommand(new CmdUndo());
			addPaletteCommand(new CmdEditText());
			addPaletteCommand(new CmdRedo());
			addPaletteCommand(new CmdSelectKeyboard());
			addPaletteCommand(new CmdShowSettings());
		}

		return palette;
	}


	private static void addTextEditingCommand(@NonNull Command cmd) {
		textEditing.put(cmd.getPaletteKey(), cmd);
	}


	private static LinkedHashMap<Integer, Command> getTextEditingCommands() {
		if (textEditing.isEmpty()) {
			addTextEditingCommand(new CmdTxtSelectPreviousChar());
			addTextEditingCommand(new CmdTxtSelectNone());
			addTextEditingCommand(new CmdTxtSelectNextChar());
			addTextEditingCommand(new CmdTxtSelectPreviousWord());
			addTextEditingCommand(new CmdTxtSelectAll());
			addTextEditingCommand(new CmdTxtSelectNextWord());
			addTextEditingCommand(new CmdTxtCut());
			addTextEditingCommand(new CmdTxtCopy());
			addTextEditingCommand(new CmdTxtPaste());
		}

		return textEditing;
	}
}
