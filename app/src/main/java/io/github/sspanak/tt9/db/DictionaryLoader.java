package io.github.sspanak.tt9.db;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.github.sspanak.tt9.ConsumerCompat;
import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.entities.WordBatch;
import io.github.sspanak.tt9.db.entities.WordFile;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAbortedException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.db.sqlite.DeleteOps;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.db.sqlite.Tables;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.ui.DictionaryLoadingBar;
import io.github.sspanak.tt9.ui.UI;

public class DictionaryLoader {
	private static final String LOG_TAG = "DictionaryLoader";
	private static DictionaryLoader self;

	private final AssetManager assets;
	private final SQLiteOpener sqlite;

	private static final Handler asyncHandler = new Handler();
	private ConsumerCompat<Bundle> onStatusChange;
	private Thread loadThread;

	private final HashMap<Integer, Long> lastAutoLoadAttemptTime = new HashMap<>();
	private int currentFile = 0;
	private long importStartTime = 0;
	private long lastProgressUpdate = 0;



	public static DictionaryLoader getInstance(Context context) {
		if (self == null) {
			self = new DictionaryLoader(context);
		}

		return self;
	}


	private DictionaryLoader(Context context) {
		assets = context.getAssets();
		sqlite = SQLiteOpener.getInstance(context);
	}


	public void setOnStatusChange(ConsumerCompat<Bundle> callback) {
		onStatusChange = callback;
	}


	private long getImportTime() {
		return System.currentTimeMillis() - importStartTime;
	}


	public boolean load(ArrayList<Language> languages) {
		if (isRunning()) {
			return false;
		}

		if (languages == null || languages.size() == 0) {
			Logger.d(LOG_TAG, "Nothing to do");
			return true;
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
		return true;
	}


	public static void load(Context context, Language language) {
		DictionaryLoadingBar progressBar = DictionaryLoadingBar.getInstance(context);
		getInstance(context).setOnStatusChange(status -> progressBar.show(context, status));
		self.load(new ArrayList<Language>() {{ add(language); }});
	}


	public static void autoLoad(TraditionalT9 context, Language language) {
		if (getInstance(context).isRunning()) {
			return;
		}

		Long lastUpdateTime = self.lastAutoLoadAttemptTime.get(language.getId());
		boolean isItTooSoon = lastUpdateTime != null && System.currentTimeMillis() - lastUpdateTime < SettingsStore.DICTIONARY_AUTO_LOAD_COOLDOWN_TIME;
		if (isItTooSoon) {
			return;
		}

		WordStoreAsync.getLastLanguageUpdateTime(
			(timestamp) -> {
				self.lastAutoLoadAttemptTime.put(language.getId(), System.currentTimeMillis());

				// no words at all, load without confirmation
				if (timestamp == 0) {
					load(context, language);
				}
				// or if the database is outdated, compared to the dictionary file, ask for confirmation and load
				else if (timestamp > 0 && timestamp < new WordFile(language.getDictionaryFile(), self.assets).getTimestamp()) {
					UI.showConfirmDictionaryUpdateDialog(context, language.getId());
				}
			},
			language
		);
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

			sqlite.beginTransaction();

			Tables.dropIndexes(sqlite.getDb(), language);
			sendProgressMessage(language, ++progress, SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME);
			logLoadingStep("Indexes dropped", language, start);

			start = System.currentTimeMillis();
			DeleteOps.delete(sqlite, language.getId());
			sendProgressMessage(language, ++progress, SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME);
			logLoadingStep("Storage cleared", language, start);

			start = System.currentTimeMillis();
			int lettersCount = importLetters(language);
			sendProgressMessage(language, ++progress, SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME);
			logLoadingStep("Letters imported", language, start);

			start = System.currentTimeMillis();
			InsertOps.restoreCustomWords(sqlite.getDb(), language);
			sendProgressMessage(language, ++progress, SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME);
			logLoadingStep("Custom words restored", language, start);

			start = System.currentTimeMillis();
			importWordFile(language, lettersCount, progress, 90);
			progress = 90;
			sendProgressMessage(language, progress, SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME);
			logLoadingStep("Dictionary file imported", language, start);

			start = System.currentTimeMillis();
			Tables.createPositionIndex(sqlite.getDb(), language);
			sendProgressMessage(language, progress + (100f - progress) / 2f, 0);
			Tables.createWordIndex(sqlite.getDb(), language);
			sendProgressMessage(language, 100, 0);
			logLoadingStep("Indexes restored", language, start);

			sqlite.finishTransaction();
			SlowQueryStats.clear();
		} catch (DictionaryImportAbortedException e) {
			sqlite.failTransaction();
			stop();
			Logger.i(LOG_TAG, e.getMessage() + ". File '" + language.getDictionaryFile() + "' not imported.");
		} catch (DictionaryImportException e) {
			stop();
			sqlite.failTransaction();
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
			sqlite.failTransaction();
			sendError(e.getClass().getSimpleName(), language.getId());

			Logger.e(
				LOG_TAG,
				"Failed loading dictionary: " + language.getDictionaryFile()
				+ " for language '" + language.getName() + "'. "
				+ e.getMessage()
			);
		}
	}


	private int importLetters(Language language) throws InvalidLanguageCharactersException {
		int lettersCount = 0;
		boolean isEnglish = language.getLocale().equals(Locale.ENGLISH);
		WordBatch letters = new WordBatch(language);

		for (int key = 2; key <= 9; key++) {
			for (String langChar : language.getKeyCharacters(key)) {
				langChar = (isEnglish && langChar.equals("i")) ? langChar.toUpperCase(Locale.ENGLISH) : langChar;
				letters.add(langChar, 0, key);
				lettersCount++;
			}
		}

		saveWordBatch(letters);

		return lettersCount;
	}


	private void importWordFile(Language language, int positionShift, float minProgress, float maxProgress) throws Exception {
		WordFile wordFile = new WordFile(language.getDictionaryFile(), assets);
		WordBatch batch = new WordBatch(language, wordFile.getTotalLines());
		int currentLine = 1;
		float progressRatio = (maxProgress - minProgress) / wordFile.getTotalLines();

		try (BufferedReader br = wordFile.getReader()) {
			for (String line; (line = br.readLine()) != null; currentLine++) {
				if (loadThread.isInterrupted()) {
					sendProgressMessage(language, 0, 0);
					throw new DictionaryImportAbortedException();
				}

				String[] parts = WordFile.splitLine(line);
				String word = parts[0];
				short frequency = WordFile.getFrequencyFromLineParts(parts);

				try {
					boolean isFinalized = batch.add(word, frequency, currentLine + positionShift);
					if (isFinalized && batch.getWords().size() > SettingsStore.DICTIONARY_IMPORT_BATCH_SIZE) {
						saveWordBatch(batch);
						batch.clear();
					}
				} catch (InvalidLanguageCharactersException e) {
					throw new DictionaryImportException(word, currentLine);
				}

				if (wordFile.getTotalLines() > 0) {
					sendProgressMessage(language, minProgress + progressRatio * currentLine, SettingsStore.DICTIONARY_IMPORT_PROGRESS_UPDATE_TIME);
				}
			}
		}

		saveWordBatch(batch);
		InsertOps.replaceLanguageMeta(sqlite.getDb(), language.getId(), wordFile.getTimestamp());
	}


	public void saveWordBatch(WordBatch batch) {
		InsertOps insertOps = new InsertOps(sqlite.getDb(), batch.getLanguage());

		for (int i = 0, end = batch.getWords().size(); i < end; i++) {
			insertOps.insertWord(batch.getWords().get(i));
		}

		for (int i = 0, end = batch.getPositions().size(); i < end; i++) {
			insertOps.insertWordPosition(batch.getPositions().get(i));
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
		progressMsg.putInt("progress", Math.round(progress));
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
