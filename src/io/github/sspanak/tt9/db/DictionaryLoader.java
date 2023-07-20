package io.github.sspanak.tt9.db;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAbortedException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAlreadyRunningException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.db.room.Word;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class DictionaryLoader {
	private static DictionaryLoader self;

	private final AssetManager assets;
	private final SettingsStore settings;

	private static final Handler asyncHandler = new Handler();
	private ConsumerCompat<Bundle> onStatusChange;
	private Thread loadThread;

	private long importStartTime = 0;
	private int currentFile = 0;
	private long lastProgressUpdate = 0;



	public static DictionaryLoader getInstance(Context context) {
		if (self == null) {
			self = new DictionaryLoader(context);
		}

		return self;
	}


	public DictionaryLoader(Context context) {
		assets = context.getAssets();
		settings = new SettingsStore(context);
	}


	public void setOnStatusChange(ConsumerCompat<Bundle> callback) {
		onStatusChange = callback;
	}


	private long getImportTime() {
		return System.currentTimeMillis() - importStartTime;
	}


	public void load(ArrayList<Language> languages) throws DictionaryImportAlreadyRunningException {
		if (isRunning()) {
			throw new DictionaryImportAlreadyRunningException();
		}

		if (languages.size() == 0) {
			Logger.d("DictionaryLoader", "Nothing to do");
			return;
		}

		loadThread = new Thread() {
			@Override
			public void run() {
				currentFile = 0;
				importStartTime = System.currentTimeMillis();

				sendFileCount(languages.size());

				// SQLite does not support parallel queries, so let's import them one by one
				for (Language lang : languages) {
					if (isInterrupted()) {
						break;
					}
					importAll(lang);
					currentFile++;
				}
			}
		};

		loadThread.start();
	}


	public void stop() {
		loadThread.interrupt();
	}


	public boolean isRunning() {
		return loadThread != null && loadThread.isAlive();
	}


	private void importAll(Language language) {
		final String logTag = "tt9.DictionaryLoader.importAll";

		if (language == null) {
			Logger.e(logTag, "Failed loading a dictionary for NULL language.");
			sendError(InvalidLanguageException.class.getSimpleName(), -1);
			return;
		}

		DictionaryDb.runInTransaction(() -> {
			try {
				long start = System.currentTimeMillis();
				importWords(language);
				Logger.i(
					logTag,
					"Dictionary: '" + language.getDictionaryFile() + "'" +
						" processing time: " + (System.currentTimeMillis() - start) + " ms"
				);

				start = System.currentTimeMillis();
				importLetters(language);
				Logger.i(
					logTag,
					"Loaded letters for '" + language.getName() + "' language in: " + (System.currentTimeMillis() - start) + " ms"
				);
			} catch (DictionaryImportAbortedException e) {
				stop();

				Logger.i(
					logTag,
					e.getMessage() + ". File '" + language.getDictionaryFile() + "' not imported."
				);
			} catch (DictionaryImportException e) {
				stop();
				sendImportError(DictionaryImportException.class.getSimpleName(), language.getId(), e.line, e.word);

				Logger.e(
					logTag,
					" Invalid word: '" + e.word
					+ "' in dictionary: '" + language.getDictionaryFile() + "'"
					+ " on line " + e.line
					+ " of language '" + language.getName() + "'. "
					+ e.getMessage()
				);
			} catch (Exception | Error e) {
				stop();
				sendError(e.getClass().getSimpleName(), language.getId());

				Logger.e(
					logTag,
					"Failed loading dictionary: " + language.getDictionaryFile() +
					" for language '" + language.getName() + "'. "
					+ e.getMessage()
				);
			}
		});
	}


	private void importLetters(Language language) {
		ArrayList<Word> letters = new ArrayList<>();

		boolean isEnglish = language.getLocale().equals(Locale.ENGLISH);

		for (int key = 2; key <= 9; key++) {
			for (String langChar : language.getKeyCharacters(key, false)) {
				langChar = (isEnglish && langChar.equals("i")) ? langChar.toUpperCase(Locale.ENGLISH) : langChar;

				Word word = new Word();
				word.langId = language.getId();
				word.frequency = 0;
				word.length = 1;
				word.sequence = String.valueOf(key);
				word.word = langChar;

				letters.add(word);
			}
		}

		DictionaryDb.upsertWordsSync(letters);
	}


	private void importWords(Language language) throws Exception {
		importWords(language, language.getDictionaryFile());
	}


	private void importWords(Language language, String dictionaryPath) throws Exception {
		sendProgressMessage(language, 0, 0);

		final byte WORD_END = (byte) 0b10001111; // word end is the equivalent of uppercase punctuation

		final long fileSize = getDictionarySize(dictionaryPath);


		int frequency = 0;
		char[] sequence = new char[256];
		char[] word = new char[256];
		int sequenceLength = 0;
		int lastByte = 0;

		ArrayList<Word> dbWords = new ArrayList<>();
		DataInputStream fileStream = new DataInputStream(new BufferedInputStream(assets.open(dictionaryPath)));

		for (long filePosition = 0; filePosition < fileSize; filePosition++) {
			if (loadThread.isInterrupted()) {
				fileStream.close();
				sendProgressMessage(language, -1, 0);
				throw new DictionaryImportAbortedException();
			}

			// analyze and process the next file byte
			byte currentByte = fileStream.readByte();

			// end of word
			if (currentByte == WORD_END && sequenceLength > 0) {
				dbWords.add(stringToWord(
					language,
					new String(Arrays.copyOfRange(word, 0, sequenceLength)),
					new String(Arrays.copyOfRange(sequence, 0, sequenceLength)),
					frequency
				));
				sequenceLength = 0;
			}
			// frequency
			else if (lastByte == WORD_END || filePosition == 0) {
				frequency = currentByte;
			}
			// word characters
			else {
				try {
					char[] decompressed = decompressChar(language, currentByte);
					sequence[sequenceLength] = decompressed[0];
					word[sequenceLength] = decompressed[1];
					sequenceLength++;
				} catch (Exception e) {
					throw new Exception("Could not decompress character at position: " + filePosition + ". " + e.getMessage());
				}
			}

			lastByte = currentByte;

			// save the word list to the database when it is long enough or when he have reached the end of the file
			if (dbWords.size() >= settings.getDictionaryImportWordChunkSize() || filePosition == fileSize - 1) {
				DictionaryDb.upsertWordsSync(dbWords);
				dbWords.clear();
			}

			// send progress status
			int progress = (int) Math.floor(100.0 * filePosition / fileSize);
			sendProgressMessage(language, progress, settings.getDictionaryImportProgressUpdateInterval());
		}

		fileStream.close();

		sendProgressMessage(language, 100, 0);
	}


	private char[] decompressChar(Language language, byte compressedChar) throws Exception {
		int key = 2 + ((compressedChar & 0b01110000) >> 4);
		int letterPosition = compressedChar & 0b00001111;
		boolean isUpperCase = (compressedChar & 0b10000000) == 0b10000000;

		return new char[] {
			(char)(key + '0'),
			letterPosition == 0b00001111 ? getPunctuationChar(key) : getLetter(language, key, letterPosition, isUpperCase)
		};
	}

	private char getPunctuationChar(int key) throws Exception {
		switch (key) {
			case 2:
				return '-';
			case 3:
				return '\'';
			case 4:
				return '"';
			default:
				throw new Exception("Unrecognized punctuation character with ID: " + key);
		}
	}


	private char getLetter(Language language, int key, int letterPosition, boolean isUpperCase) throws Exception {
		try {
			String letter = language.getKeyCharacters(key).get(letterPosition);
			letter = isUpperCase ? letter.toUpperCase(language.getLocale()) : letter;
			return letter.charAt(0);
		} catch (Exception e) {
			throw new Exception("No character on " + key + "-key at position: " + (letterPosition + 1) + " (human numbers)");
		}
	}


	private long getDictionarySize(String dictionaryPath) {
		try {
			DataInputStream fileStream = new DataInputStream(new BufferedInputStream(assets.open(dictionaryPath)));
			long fileSize = fileStream.skip(Long.MAX_VALUE);
			fileStream.close();
			return fileSize;
		} catch (IOException e) {
			Logger.w("DictionaryLoader.getDictionarySize", "Could not count the lines of file: " + dictionaryPath + ". " + e.getMessage());
			return 0;
		}
	}


	private Word stringToWord(Language language, String word, String sequence, int frequency) {
		Word dbWord = new Word();
		dbWord.langId = language.getId();
		dbWord.frequency = frequency;
		dbWord.length = word.length();
		dbWord.sequence = sequence;
		dbWord.word = word;

		return dbWord;
	}


	private void sendFileCount(int fileCount) {
		if (onStatusChange == null) {
			Logger.w(
				"DictionaryLoader.sendFileCount",
				"Cannot send file count without a status Handler. Ignoring message.");
			return;
		}

		Bundle progressMsg = new Bundle();
		progressMsg.putInt("fileCount", fileCount);
		asyncHandler.post(() -> onStatusChange.accept(progressMsg));
	}


	private void sendProgressMessage(Language language, int progress, int progressUpdateInterval) {
		if (onStatusChange == null) {
			Logger.w(
				"DictionaryLoader.sendProgressMessage",
				"Cannot send progress without a status Handler. Ignoring message.");
			return;
		}

		long now = System.currentTimeMillis();
		if (now - lastProgressUpdate < progressUpdateInterval) {
			return;
		}

		lastProgressUpdate = now;

		Bundle progressMsg = new Bundle();
		progressMsg.putInt("languageId", language.getId());
		progressMsg.putLong("time", getImportTime());
		progressMsg.putInt("progress", progress);
		progressMsg.putInt("currentFile", currentFile);
		asyncHandler.post(() -> onStatusChange.accept(progressMsg));
	}


	private void sendError(String message, int langId) {
		if (onStatusChange == null) {
			Logger.w("DictionaryLoader.sendError", "Cannot send an error without a status Handler. Ignoring message.");
			return;
		}

		Bundle errorMsg = new Bundle();
		errorMsg.putString("error", message);
		errorMsg.putInt("languageId", langId);
		asyncHandler.post(() -> onStatusChange.accept(errorMsg));
	}


	private void sendImportError(String message, int langId, long fileLine, String word) {
		if (onStatusChange == null) {
			Logger.w("DictionaryLoader.sendError", "Cannot send an import error without a status Handler. Ignoring message.");
			return;
		}

		Bundle errorMsg = new Bundle();
		errorMsg.putString("error", message);
		errorMsg.putLong("fileLine", fileLine + 1);
		errorMsg.putInt("languageId", langId);
		errorMsg.putString("word", word);
		asyncHandler.post(() -> onStatusChange.accept(errorMsg));
	}
}
