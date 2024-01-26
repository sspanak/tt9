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
import io.github.sspanak.tt9.db.entities.WordBatch;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAbortedException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAlreadyRunningException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.db.sqlite.DeleteOps;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.db.sqlite.Tables;
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

		try {
			long start = System.currentTimeMillis();
			float progress = 1;
			final float dictionaryMaxProgress = 90f;

			sqlite.beginTransaction();

			Tables.dropIndexes(sqlite.getDb(), language);
			sendProgressMessage(language, ++progress, 0);
			logLoadingStep("Indexes dropped", language, start);

			start = System.currentTimeMillis();
			DeleteOps.delete(sqlite, language.getId());
			sendProgressMessage(language, ++progress, 0);
			logLoadingStep("Storage cleared", language, start);

			start = System.currentTimeMillis();
			int lettersCount = importLetters(language);
			sendProgressMessage(language, ++progress, 0);
			logLoadingStep("Letters imported", language, start);

			start = System.currentTimeMillis();
			InsertOps.restoreCustomWords(sqlite.getDb(), language);
			sendProgressMessage(language, ++progress, 0);
			logLoadingStep("Custom words restored", language, start);

			start = System.currentTimeMillis();
			WordBatch words = readWordsFile(language, lettersCount, progress, progress + 25f);
			progress += 25;
			sendProgressMessage(language, progress, 0);
			logLoadingStep("Dictionary file loaded in memory", language, start);

			start = System.currentTimeMillis();
			saveWordBatch(words, progress, dictionaryMaxProgress, settings.getDictionaryImportProgressUpdateBatchSize());
			progress = dictionaryMaxProgress;
			sendProgressMessage(language, progress, 0);
			logLoadingStep("Dictionary words saved in database", language, start);

			start = System.currentTimeMillis();
			Tables.createPositionIndex(sqlite.getDb(), language);
			sendProgressMessage(language, progress + (100f - progress) / 2f, 0);
			Tables.createWordIndex(sqlite.getDb(), language);
			sendProgressMessage(language, 100, 0);
			logLoadingStep("Indexes restored", language, start);

			sqlite.finishTransaction();
		} catch (DictionaryImportAbortedException e) {
			sqlite.failTransaction();
			stop();
			Logger.i(LOG_TAG, e.getMessage() + ". File '" + language.getDictionaryFile() + "' not imported.");
		} catch (DictionaryImportException e) {
			sqlite.failTransaction();
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
			sqlite.failTransaction();
			stop();
			sendError(e.getClass().getSimpleName(), language.getId());

			Logger.e(
				LOG_TAG,
				"Failed loading dictionary: " + language.getDictionaryFile()
				+ " for language '" + language.getName() + "'. "
				+ e.getMessage()
			);
		}
	}


	private int importLetters(Language language) throws InvalidLanguageCharactersException, DictionaryImportAbortedException {
		int lettersCount = 0;
		boolean isEnglish = language.getLocale().equals(Locale.ENGLISH);
		WordBatch letters = new WordBatch(language);

		for (int key = 2; key <= 9; key++) {
			for (String langChar : language.getKeyCharacters(key, false)) {
				langChar = (isEnglish && langChar.equals("i")) ? langChar.toUpperCase(Locale.ENGLISH) : langChar;
				letters.add(langChar, 0, key);
				lettersCount++;
			}
		}

		saveWordBatch(letters, -1, -1, -1);

		return lettersCount;
	}


	private WordBatch readWordsFile(Language language, int positionShift, float minProgress, float maxProgress) throws Exception {
		int currentLine = 1;
		int totalLines = getFileSize(language.getDictionaryFile());
		float progressRatio = (maxProgress - minProgress) / totalLines;

		WordBatch batch = new WordBatch(language, totalLines);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(assets.open(language.getDictionaryFile()), StandardCharsets.UTF_8))) {
			for (String line; (line = br.readLine()) != null; currentLine++) {
				if (loadThread.isInterrupted()) {
					sendProgressMessage(language, 0, 0);
					throw new DictionaryImportAbortedException();
				}

				String[] parts = splitLine(line);
				String word = parts[0];
				short frequency = getFrequency(parts);

				try {
					batch.add(word, frequency, currentLine + positionShift);
				} catch (InvalidLanguageCharactersException e) {
					throw new DictionaryImportException(word, currentLine);
				}

				if (totalLines > 0 && currentLine % settings.getDictionaryImportProgressUpdateBatchSize() == 0) {
					sendProgressMessage(language, minProgress + progressRatio * currentLine, settings.getDictionaryImportProgressUpdateTime());
				}
			}
		}

		return batch;
	}


	public void saveWordBatch(WordBatch batch, float minProgress, float maxProgress, int updateInterval) throws DictionaryImportAbortedException {
		float middleProgress = minProgress + (maxProgress - minProgress) / 2;

		InsertOps insertOps = new InsertOps(sqlite.getDb(), batch.getLanguage());

		insertWordsBatch(insertOps, batch, minProgress, middleProgress - 2, updateInterval);
		insertWordPositionsBatch(insertOps, batch, middleProgress - 2, maxProgress - 2, updateInterval);
		InsertOps.insertMaxPositionRange(sqlite.getDb(), batch);

		if (updateInterval > 0) {
			sendProgressMessage(batch.getLanguage(), maxProgress, settings.getDictionaryImportProgressUpdateBatchSize());
		}
	}


	private void insertWordsBatch(InsertOps insertOps, WordBatch batch, float minProgress, float maxProgress, int sizeUpdateInterval) throws DictionaryImportAbortedException {
		if (batch.getWords().size() == 0) {
			return;
		}

		float progressRatio = (maxProgress - minProgress) / batch.getWords().size();

		for (int progress = 0, end = batch.getWords().size(); progress < end; progress++) {
			if (loadThread.isInterrupted()) {
				sendProgressMessage(batch.getLanguage(), 0, 0);
				throw new DictionaryImportAbortedException();
			}

			insertOps.insertWord(batch.getWords().get(progress));
			if (sizeUpdateInterval > 0 && progress % sizeUpdateInterval == 0) {
				sendProgressMessage(batch.getLanguage(), minProgress + progress * progressRatio, settings.getDictionaryImportProgressUpdateTime());
			}
		}
	}


	private void insertWordPositionsBatch(InsertOps insertOps, WordBatch batch, float minProgress, float maxProgress, int sizeUpdateInterval) throws DictionaryImportAbortedException {
		if (batch.getPositions().size() == 0) {
			return;
		}

		float progressRatio = (maxProgress - minProgress) / batch.getPositions().size();

		for (int progress = 0, end = batch.getPositions().size(); progress < end; progress++) {
			if (loadThread.isInterrupted()) {
				sendProgressMessage(batch.getLanguage(), 0, 0);
				throw new DictionaryImportAbortedException();
			}

			insertOps.insertWordPosition(batch.getPositions().get(progress));
			if (sizeUpdateInterval > 0 && progress % sizeUpdateInterval == 0) {
				sendProgressMessage(batch.getLanguage(), minProgress + progress * progressRatio, settings.getDictionaryImportProgressUpdateTime());
			}
		}
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


	private int getFileSize(String filename) {
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


	private void sendProgressMessage(Language language, float progress, int progressUpdateInterval) {
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
		progressMsg.putInt("progress", (int) Math.round(progress));
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


	private void logLoadingStep(String message, Language language, long time) {
		if (Logger.isDebugLevel()) {
			Logger.d(LOG_TAG, message + " for language '" + language.getName() + "' in: " + (System.currentTimeMillis() - time) + " ms");
		}
	}
}
