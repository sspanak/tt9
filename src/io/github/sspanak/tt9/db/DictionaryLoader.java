package io.github.sspanak.tt9.db;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.InvalidLanguageException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class DictionaryLoader {
	private static DictionaryLoader self;

	private final AssetManager assets;
	private final SettingsStore settings;

	private final Pattern containsPunctuation = Pattern.compile("\\p{Punct}(?<!-)");
	private Handler statusHandler = null;
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


	public void setStatusHandler(Handler handler) {
		statusHandler = handler;
	}


	private long getImportTime() {
		return System.currentTimeMillis() - importStartTime;
	}


	public void load(ArrayList<Language> languages) throws DictionaryImportAlreadyRunningException {
		if (isRunning()) {
			throw new DictionaryImportAlreadyRunningException();
		}

		loadThread = new Thread() {
			@Override
			public void run() {
				currentFile = 0;
				importStartTime = System.currentTimeMillis();
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

		for (int key = 2; key <= 9; key++) {
			for (String langChar : language.getKeyCharacters(key)) {
				if (langChar.length() == 1 && langChar.charAt(0) >= '0' && langChar.charAt(0) <= '9') {
					// We do not want 0-9 as "word suggestions" in Predictive mode. It looks confusing
					// when trying to type a word and also, one can type them by holding the respective
					// key.
					continue;
				}

				if (DictionaryDb.doesWordExistSync(language, langChar.toUpperCase(language.getLocale()))) {
					continue;
				}

				Word word = new Word();
				word.langId = language.getId();
				word.frequency = 0;
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
			String word = validateWord(language, parts, lineCount);
			int frequency = validateFrequency(parts);

			try {
				dbWords.add(stringToWord(language, word, frequency));
			} catch (InvalidLanguageCharactersException e) {
				throw new DictionaryImportException(dictionaryFile, word, lineCount);
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


	private String validateWord(Language language, String[] lineParts, long line) throws DictionaryImportException {
		String word = lineParts[0];

		if (!language.isPunctuationPartOfWords() && containsPunctuation.matcher(word).find()) {
			throw new DictionaryImportException(language.getDictionaryFile(), word, line);
		}

		return word;
	}


	private int validateFrequency(String[] lineParts) {
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
		dbWord.sequence = language.getDigitSequenceForWord(word);
		dbWord.word = word;

		return dbWord;
	}


	private void sendProgressMessage(Language language, int progress, int progressUpdateInterval) {
		if (statusHandler == null) {
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

		Bundle bundle = new Bundle();
		bundle.putInt("languageId", language.getId());
		bundle.putLong("time", getImportTime());
		bundle.putInt("progress", progress);
		bundle.putInt("currentFile", currentFile);
		Message msg = new Message();
		msg.setData(bundle);
		statusHandler.sendMessage(msg);
	}


	private void sendError(String message, int langId) {
		if (statusHandler == null) {
			Logger.w("tt9/DictionaryLoader.sendError", "Cannot send an error without a status Handler. Ignoring message.");
			return;
		}

		Bundle bundle = new Bundle();
		bundle.putString("error", message);
		bundle.putInt("languageId", langId);
		Message msg = new Message();
		msg.setData(bundle);
		statusHandler.sendMessage(msg);
	}


	private void sendImportError(String message, int langId, long fileLine, String word) {
		if (statusHandler == null) {
			Logger.w("tt9/DictionaryLoader.sendError", "Cannot send an import error without a status Handler. Ignoring message.");
			return;
		}

		Bundle bundle = new Bundle();
		bundle.putString("error", message);
		bundle.putLong("fileLine", fileLine + 1);
		bundle.putInt("languageId", langId);
		bundle.putString("word", word);
		Message msg = new Message();
		msg.setData(bundle);
		statusHandler.sendMessage(msg);
	}
}
