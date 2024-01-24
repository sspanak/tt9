package io.github.sspanak.tt9.db;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAbortedException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAlreadyRunningException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.db.sqlite.InsertOperations;
import io.github.sspanak.tt9.db.sqlite.DeleteOperations;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class DictionaryLoader {
	private static final String LOG_TAG = "DictionaryLoader";
	private static DictionaryLoader self;

	private final AssetManager assets;
	private final SettingsStore settings;
	private final SQLiteOpener sqlite;

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
		sqlite = SQLiteOpener.getInstance(context);
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
			Logger.d(LOG_TAG, "Nothing to do");
			return;
		}

		loadThread = new Thread() {
			@Override
			public void run() {
				currentFile = 0;
				importStartTime = System.currentTimeMillis();

				sendStartMessage(languages.size());

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
		if (language == null) {
			Logger.e(LOG_TAG, "Failed loading a dictionary for NULL language.");
			sendError(InvalidLanguageException.class.getSimpleName(), -1);
			return;
		}

		sqlite.runInTransaction(() -> {
			try {
				long start = System.currentTimeMillis();

				DeleteOperations.delete(sqlite, language.getId());
				Logger.i(
					LOG_TAG,
					"Storage for language '" + language.getName() + "' cleared in: " + (System.currentTimeMillis() - start) + " ms"
				);

				start = System.currentTimeMillis();
				int lettersCount = importLetters(language);
				Logger.i(
					LOG_TAG,
					"Added letters for '" + language.getName() + "' in: " + (System.currentTimeMillis() - start) + " ms"
				);

				start = System.currentTimeMillis();
				InsertOperations.restoreCustomWords(sqlite.getDb(), language);
				Logger.i(
					LOG_TAG,
					"Restored custom words for '" + language.getName() + "' in: " + (System.currentTimeMillis() - start) + " ms"
				);

				start = System.currentTimeMillis();
				importWords(language, language.getDictionaryFile(), lettersCount);
				Logger.i(
					LOG_TAG,
					"Dictionary: '" + language.getDictionaryFile() + "'" +
						" processing time: " + (System.currentTimeMillis() - start) + " ms"
				);

			} catch (DictionaryImportAbortedException e) {
				stop();

				Logger.i(
					LOG_TAG,
					e.getMessage() + ". File '" + language.getDictionaryFile() + "' not imported."
				);
			} catch (DictionaryImportException e) {
				stop();
				sendImportError(DictionaryImportException.class.getSimpleName(), language.getId(), e.line, e.word);

				Logger.e(
					LOG_TAG,
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
					LOG_TAG,
					"Failed loading dictionary: " + language.getDictionaryFile()
					+ " for language '" + language.getName() + "'. "
					+ e.getMessage()
				);
			}
		});
	}


	private int importLetters(Language language) throws InvalidLanguageCharactersException {
		InsertOperations letters = new InsertOperations(language, settings);
		int lettersCount = 0;
		boolean isEnglish = language.getLocale().equals(Locale.ENGLISH);

		for (int key = 2; key <= 9; key++) {
			for (String langChar : language.getKeyCharacters(key, false)) {
				langChar = (isEnglish && langChar.equals("i")) ? langChar.toUpperCase(Locale.ENGLISH) : langChar;
				letters.addWordToBatch(langChar, (short) 0, key);
				lettersCount++;
			}
		}

		letters.saveBatch(sqlite.getDb());

		return lettersCount;
	}


	private void importWords(Language language, String dictionaryFile, int positionShift) throws Exception {
		sendProgressMessage(language, 1, 0);

		int currentLine = 1;
		int totalLines = (int) getFileSize(dictionaryFile); // @todo: add a maximum word validation up to 2^31 - 1

		BufferedReader br = new BufferedReader(new InputStreamReader(assets.open(dictionaryFile), StandardCharsets.UTF_8));

		// @todo: instead of accumulating two ArrayLists, build the insert Strings in a WordOperation.
		InsertOperations wordBatch = new InsertOperations(language, settings);

		for (String line; (line = br.readLine()) != null; currentLine++) {
			if (loadThread.isInterrupted()) {
				br.close();
				sendProgressMessage(language, 0, 0);
				throw new DictionaryImportAbortedException();
			}

			String[] parts = splitLine(line);
			String word = parts[0];
			short frequency = getFrequency(parts);

			try {
				if (!wordBatch.addWordToBatch(word, frequency, currentLine + positionShift)) {
					wordBatch.saveBatch(sqlite.getDb());
					wordBatch.addWordToBatch(word, frequency, currentLine + positionShift);
				}
			} catch (InvalidLanguageCharactersException e) {
				br.close();
				throw new DictionaryImportException(word, currentLine);
			}

			if (totalLines > 0) {
				int progress = (int) Math.floor(100.0 * currentLine / totalLines);
				progress = Math.max(1, progress);
				sendProgressMessage(language, progress, settings.getDictionaryImportProgressUpdateInterval());
			}
		}

		wordBatch.saveBatch(sqlite.getDb());
		wordBatch.saveLongestPositionRange(sqlite.getDb());
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


	private long getFileSize(String filename) {
		String sizeFilename = filename + ".size";

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(sizeFilename), StandardCharsets.UTF_8))) {
			return Integer.parseInt(reader.readLine());
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Could not read the size of: " + filename + " from:  " + sizeFilename + ". " + e.getMessage());
			return 0;
		}
	}


	private short getFrequency(String[] lineParts) {
		try {
			return Short.parseShort(lineParts[1]);
		} catch (Exception e) {
			return 0;
		}
	}


	private void sendStartMessage(int fileCount) {
		if (onStatusChange == null) {
			Logger.w(LOG_TAG, "Cannot send file count without a status Handler. Ignoring message.");
			return;
		}

		Bundle progressMsg = new Bundle();
		progressMsg.putInt("fileCount", fileCount);
		progressMsg.putInt("progress", 1);
		asyncHandler.post(() -> onStatusChange.accept(progressMsg));
	}


	private void sendProgressMessage(Language language, int progress, int progressUpdateInterval) {
		if (onStatusChange == null) {
			Logger.w(LOG_TAG, "Cannot send progress without a status Handler. Ignoring message.");
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
			Logger.w(LOG_TAG, "Cannot send an error without a status Handler. Ignoring message.");
			return;
		}

		Bundle errorMsg = new Bundle();
		errorMsg.putString("error", message);
		errorMsg.putInt("languageId", langId);
		asyncHandler.post(() -> onStatusChange.accept(errorMsg));
	}


	private void sendImportError(String message, int langId, long fileLine, String word) {
		if (onStatusChange == null) {
			Logger.w(LOG_TAG, "Cannot send an import error without a status Handler. Ignoring message.");
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
