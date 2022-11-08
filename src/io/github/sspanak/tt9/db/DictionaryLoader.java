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
	private Thread loadThread;

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
		settings = SettingsStore.getInstance();
	}


	public void load(Handler handler, ArrayList<Language> languages) throws DictionaryImportAlreadyRunningException {
		if (isRunning()) {
			throw new DictionaryImportAlreadyRunningException();
		}

		loadThread = new Thread() {
			@Override
			public void run() {
				currentFile = 0;
				// SQLite does not support parallel queries, so let's import them one by one
				for (Language lang : languages) {
					if (isInterrupted()) {
						break;
					}
					importAll(handler, lang);
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


	private void importAll(Handler handler, Language language) {
		final String logTag = "tt9.DictionaryLoader.importAll";

		if (language == null) {
			Logger.e(logTag, "Failed loading a dictionary for NULL language.");
			sendError(handler, InvalidLanguageException.class.getSimpleName(), -1);
			return;
		}

		DictionaryDb.runInTransaction(() -> {
			long start = System.currentTimeMillis();
			importLetters(language);
			Logger.i(
				logTag,
				"Loaded letters for '" + language.getName() + "' language in: " + (System.currentTimeMillis() - start) + " ms"
			);

			try {
				start = System.currentTimeMillis();
				importWords(handler, language);
				Logger.i(
					logTag,
					"Dictionary: '" + language.getDictionaryFile() + "'" +
						" processing time: " + (System.currentTimeMillis() - start) + " ms"
				);
			} catch (DictionaryImportAbortedException e) {
				stop();

				Logger.i(
					logTag,
					e.getMessage() + ". File '" + language.getDictionaryFile() + "' not imported."
				);
			} catch (DictionaryImportException e) {
				stop();
				sendImportError(handler, DictionaryImportException.class.getSimpleName(), language.getId(), e.line, e.word);

				Logger.e(
					logTag,
					" Invalid word: '" + e.word
					+ "' in dictionary: '" + language.getDictionaryFile() + "'"
					+ " on line " + e.line
					+ " of language '" + language.getName() + "'. "
					+ e.getMessage()
				);
			} catch (Exception e) {
				stop();
				sendError(handler, e.getClass().getSimpleName(), language.getId());

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

		for (int key = 0; key <= 9; key++) {
			for (String langChar : language.getKeyCharacters(key)) {
				if (langChar.length() == 1 && langChar.charAt(0) >= '0' && langChar.charAt(0) <= '9') {
					// We do not want 0-9 as "word suggestions" in Predictive mode. It looks confusing
					// when trying to type a word and also, one can type them by holding the respective
					// key.
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

		DictionaryDb.insertWordsSync(letters);
	}


	private void importWords(Handler handler, Language language) throws Exception {
		importWords(handler, language, language.getDictionaryFile());
	}


	private void importWords(Handler handler, Language language, String dictionaryFile) throws Exception {
		long totalWords = countWords(dictionaryFile);

		BufferedReader br = new BufferedReader(new InputStreamReader(assets.open(dictionaryFile), StandardCharsets.UTF_8));

		ArrayList<Word> dbWords = new ArrayList<>();
		long line = 0;

		sendProgressMessage(handler, language, 0, 0);

		for (String word; (word = br.readLine()) != null; line++) {
			if (loadThread.isInterrupted()) {
				br.close();
				sendProgressMessage(handler, language, -1, 0);
				throw new DictionaryImportAbortedException();
			}

			validateWord(language, word, line);
			dbWords.add(stringToWord(language, word));

			if (line % settings.getDictionaryImportWordChunkSize() == 0) {
				DictionaryDb.insertWordsSync(dbWords);
				dbWords.clear();
			}

			if (totalWords > 0) {
				int progress = (int) Math.floor(100.0 * line / totalWords);
				sendProgressMessage(handler, language, progress, settings.getDictionaryImportProgressUpdateInterval());
			}
		}

		br.close();
		sendProgressMessage(handler, language, 100, 0);
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


	private void validateWord(Language language, String word, long line) throws DictionaryImportException {
		if (!language.isPunctuationPartOfWords() && containsPunctuation.matcher(word).find()) {
			throw new DictionaryImportException(language.getDictionaryFile(), word, line);
		}
	}


	private Word stringToWord(Language language, String word) throws InvalidLanguageCharactersException {
		Word dbWord = new Word();
		dbWord.langId = language.getId();
		dbWord.frequency = 0;
		dbWord.sequence = language.getDigitSequenceForWord(word);
		dbWord.word = word;

		return dbWord;
	}


	private void sendProgressMessage(Handler handler, Language language, int progress, int progressUpdateInterval) {
		long now = System.currentTimeMillis();
		if (now - lastProgressUpdate < progressUpdateInterval) {
			return;
		}

		lastProgressUpdate = now;

		Bundle bundle = new Bundle();
		bundle.putInt("languageId", language.getId());
		bundle.putInt("progress", progress);
		bundle.putInt("currentFile", currentFile);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}


	private void sendError(Handler handler, String message, int langId) {
		Bundle bundle = new Bundle();
		bundle.putString("error", message);
		bundle.putInt("languageId", langId);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}


	private void sendImportError(Handler handler, String message, int langId, long fileLine, String word) {
		Bundle bundle = new Bundle();
		bundle.putString("error", message);
		bundle.putLong("fileLine", fileLine);
		bundle.putInt("languageId", langId);
		bundle.putString("word", word);
		Message msg = new Message();
		msg.setData(bundle);
		handler.sendMessage(msg);
	}
}
