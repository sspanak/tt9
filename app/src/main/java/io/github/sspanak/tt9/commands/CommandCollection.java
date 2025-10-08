package io.github.sspanak.tt9.commands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.github.sspanak.tt9.R;

public class CommandCollection {
	public static final int COLLECTION_HOTKEYS = 1;
	public static final int COLLECTION_PALETTE = 2;
	public static final int COLLECTION_TEXT_EDITING = 3;

	private static final HashMap<String, Command> searchCache = new HashMap<>();

	private static final ArrayList<Command> hotkeys = new ArrayList<>();
	private static final LinkedHashMap<Integer, Command> palette = new LinkedHashMap<>();
	private static final HashMap<String, Integer> reversePalette = new HashMap<>();
	private static final LinkedHashMap<Integer, Command> textEditing = new LinkedHashMap<>();
	private static final HashMap<String, Integer> reverseTextEditing = new HashMap<>();


	@Nullable
	public static Command getById(int collectionType, @Nullable String commandId) {
		Collection<Command> commands = collectionType == COLLECTION_HOTKEYS ? getHotkeyCommands() : getAll(collectionType).values();

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
	public static Command getByKeyId(int collectionType, int keyId) {
		Command cmd = getAll(collectionType).get(keyId);
		return cmd != null ? cmd : new NullCommand();
	}


	@NonNull
	public static LinkedHashMap<Integer, Command> getAll(int collectionType) {
		return switch(collectionType) {
			case COLLECTION_PALETTE -> getPaletteCommands();
			case COLLECTION_TEXT_EDITING -> getTextEditingCommands();
			default -> new LinkedHashMap<>();
		};
	}


	public static int indexOf(int collectionType, @Nullable String commandId) {
		if (collectionType == COLLECTION_HOTKEYS) {
			return indexOfHotkeyCommand(commandId);
		}

		HashMap<String, Integer> reverse = switch(collectionType) {
			case COLLECTION_PALETTE -> reversePalette;
			case COLLECTION_TEXT_EDITING -> reverseTextEditing;
			default -> new HashMap<>();
		};

		Integer key = reverse.get(commandId);
		return key != null ? key : -1;
	}


	private static void generateReverseMap(@NonNull LinkedHashMap<Integer, Command> commands, @NonNull HashMap<String, Integer> target) {
		for (Integer keyId : commands.keySet()) {
			Command cmd = commands.get(keyId);
			if (cmd != null) {
				target.put(cmd.getId(), keyId);
			}
		}
	}


	@NonNull
	public static ArrayList<Command> getHotkeyCommands() {
		if (hotkeys.isEmpty()) {
			hotkeys.add(new CmdAddWord());
			hotkeys.add(new CmdBackspace());
			hotkeys.add(new CmdCommandPalette());
			hotkeys.add(new CmdEditText());
			hotkeys.add(new CmdFilterClear());
			hotkeys.add(new CmdFilterSuggestions());
			hotkeys.add(new CmdSuggestionPrevious());
			hotkeys.add(new CmdSuggestionNext());
			hotkeys.add(new CmdNextInputMode());
			hotkeys.add(new CmdNextLanguage());
			hotkeys.add(new CmdSelectKeyboard());
			hotkeys.add(new CmdShift());
			hotkeys.add(new CmdSpaceKorean());
			hotkeys.add(new CmdShowSettings());
			hotkeys.add(new CmdUndo());
			hotkeys.add(new CmdRedo());
			hotkeys.add(new CmdVoiceInput());
		}

		return hotkeys;
	}


	private static int indexOfHotkeyCommand(@Nullable String commandId) {
		ArrayList<Command> commands = getHotkeyCommands();
		for (int i = 0; i < commands.size(); i++) {
			if (commands.get(i).getId().equals(commandId)) {
				return i;
			}
		}
		return -1;
	}


	@NonNull
	private static LinkedHashMap<Integer, Command> getPaletteCommands() {
		if (palette.isEmpty()) {
			palette.put(R.id.soft_key_1, new CmdShowSettings());
			palette.put(R.id.soft_key_2, new CmdAddWord());
			palette.put(R.id.soft_key_3, new CmdVoiceInput());
			palette.put(R.id.soft_key_4, new CmdUndo());
			palette.put(R.id.soft_key_5, new CmdEditText());
			palette.put(R.id.soft_key_6, new CmdRedo());
			palette.put(R.id.soft_key_8, new CmdSelectKeyboard());
			generateReverseMap(palette, reversePalette);
		}

		return palette;
	}


	private static LinkedHashMap<Integer, Command> getTextEditingCommands() {
		if (textEditing.isEmpty()) {
			textEditing.put(R.id.soft_key_1, new CmdTxtSelectPreviousChar());
			textEditing.put(R.id.soft_key_2, new CmdTxtSelectNone());
			textEditing.put(R.id.soft_key_3, new CmdTxtSelectNextChar());
			textEditing.put(R.id.soft_key_4, new CmdTxtSelectPreviousWord());
			textEditing.put(R.id.soft_key_5, new CmdTxtSelectAll());
			textEditing.put(R.id.soft_key_6, new CmdTxtSelectNextWord());
			textEditing.put(R.id.soft_key_7, new CmdTxtCut());
			textEditing.put(R.id.soft_key_8, new CmdTxtCopy());
			textEditing.put(R.id.soft_key_9, new CmdTxtPaste());
			generateReverseMap(textEditing, reverseTextEditing);
		}

		return textEditing;
	}
}
