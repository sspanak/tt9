package io.github.sspanak.tt9.db;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAbortedException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAlreadyRunningException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.db.room.Word;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
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

				dropIndexes();

				// SQLite does not support parallel queries, so let's import them one by one
				for (Language lang : languages) {
					if (isInterrupted()) {
						break;
					}
					importAll(lang);
					currentFile++;
				}

				createIndexes();
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


	private void dropIndexes() {
		long start = System.currentTimeMillis();
		DictionaryDb.dropLongWordIndexSync();
		Logger.d("dropIndexes", "Index 1: " + (System.currentTimeMillis() - start) + " ms");

		start = System.currentTimeMillis();
		DictionaryDb.dropShortWordIndexSync();
		Logger.d("dropIndexes", "Index 2: " + (System.currentTimeMillis() - start) + " ms");
	}


	private void createIndexes() {
		long start = System.currentTimeMillis();
		DictionaryDb.createLongWordIndexSync();
		Logger.d("createIndexes", "Index 1: " + (System.currentTimeMillis() - start) + " ms");

		start = System.currentTimeMillis();
		DictionaryDb.createShortWordIndexSync();
		Logger.d("createIndexes", "Index 2: " + (System.currentTimeMillis() - start) + " ms");
	}


	private void importLetters(Language language) {
		ArrayList<Word> letters = new ArrayList<>();

		for (int key = 2; key <= 9; key++) {
			for (String langChar : language.getKeyCharacters(key)) {
				if (langChar.length() == 1 && langChar.charAt(0) >= '0' && langChar.charAt(0) <= '9') {
					// We do not want 0-9 as "word suggestions" in Predictive mode. It looks confusing
					// when trying to type a word and also, one can type them by holding the respective
					// key.
					continue;
				}

//				if (DictionaryDb.doesWordExistSync(language, langChar.toUpperCase(language.getLocale()))) {
//					continue;
//				}

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


	private void importWords(Language language, String dictionaryFile) throws Exception {
		long totalWords = countWords(dictionaryFile);

		BufferedReader br = new BufferedReader(new InputStreamReader(assets.open(dictionaryFile), StandardCharsets.UTF_8));

		ArrayList<Word> dbWords = new ArrayList<>();
		long lineCount = 0;

		sendProgressMessage(language, 0, 0);

		for (String line; (line = br.readLine()) != null; lineCount++) {
			if (loadThread.isInterrupted()) {
				br.close();
				sendProgressMessage(language, -1, 0);
				throw new DictionaryImportAbortedException();
			}

			String[] parts = splitLine(line);
			String word = parts[0];
			int frequency = getFrequency(parts);

			try {
				dbWords.add(stringToWord(language, word, frequency));
			} catch (InvalidLanguageCharactersException e) {
				throw new DictionaryImportException(word, lineCount);
			}

			if (lineCount % settings.getDictionaryImportWordChunkSize() == 0 || lineCount == totalWords - 1) {
				DictionaryDb.upsertWordsSync(dbWords);
				dbWords.clear();
			}

			if (totalWords > 0) {
				int progress = (int) Math.floor(100.0 * lineCount / totalWords);
				sendProgressMessage(language, progress, settings.getDictionaryImportProgressUpdateInterval());
			}
		}

		br.close();
		sendProgressMessage(language, 100, 0);
	}


	private String[] splitLine(String line) {
		String[] parts = { line, "" };

		// This is faster than String.split() by around 10%, so it's worth having it.
		// It runs very often, so any other optimizations are welcome.
		for (int i = 0 ; i < line.length(); i++) {
			if (line.charAt(i) == '	') { // the delimiter is TAB
				parts[0] = line.substring(0, i);
				parts[1] = i < line.length() - 1 ? line.substring(i + 1) : "";
				break;
			}
		}

		return parts;
	}


	private long countWords(String filename) {
		try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(assets.open(filename), StandardCharsets.UTF_8))) {
			//noinspection ResultOfMethodCallIgnored
			reader.skip(Long.MAX_VALUE);
			long lines = reader.getLineNumber();
			reader.close();

			return lines;
		} catch (Exception e) {
			Logger.w("DictionaryLoader.countWords", "Could not count the lines of file: " + filename + ". " + e.getMessage());
			return 0;
		}
	}


	private int getFrequency(String[] lineParts) {
		try {
			return Integer.parseInt(lineParts[1]);
		} catch (Exception e) {
			return 0;
		}
	}


	private Word stringToWord(Language language, String word, int frequency) throws InvalidLanguageCharactersException {
		Word dbWord = new Word();
		dbWord.langId = language.getId();
		dbWord.frequency = frequency;
		dbWord.length = word.length();
		dbWord.sequence = language.getDigitSequenceForWord(word);
		dbWord.word = word;

		return dbWord;
	}


	private void sendProgressMessage(Language language, int progress, int progressUpdateInterval) {
		if (onStatusChange == null) {
			Logger.w(
				"tt9/DictionaryLoader.sendProgressMessage",
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
			Logger.w("tt9/DictionaryLoader.sendError", "Cannot send an error without a status Handler. Ignoring message.");
			return;
		}

		Bundle errorMsg = new Bundle();
		errorMsg.putString("error", message);
		errorMsg.putInt("languageId", langId);
		asyncHandler.post(() -> onStatusChange.accept(errorMsg));
	}


	private void sendImportError(String message, int langId, long fileLine, String word) {
		if (onStatusChange == null) {
			Logger.w("tt9/DictionaryLoader.sendError", "Cannot send an import error without a status Handler. Ignoring message.");
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
