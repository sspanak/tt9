package io.github.sspanak.tt9.db.words;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Consumer;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.db.entities.WordBatch;
import io.github.sspanak.tt9.db.entities.WordFile;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportAbortedException;
import io.github.sspanak.tt9.db.exceptions.DictionaryImportException;
import io.github.sspanak.tt9.db.sqlite.DeleteOps;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.db.sqlite.Tables;
import io.github.sspanak.tt9.db.sqlite.WordDbOpener;
import io.github.sspanak.tt9.ime.helpers.InputModePolicy;
import io.github.sspanak.tt9.extensions.ChinesePack;
import io.github.sspanak.tt9.extensions.ChinesePackInstaller;
import io.github.sspanak.tt9.extensions.ChinesePackDownload;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.notifications.DictionaryLoadingBar;
import io.github.sspanak.tt9.ui.notifications.DictionaryUpdateNotification;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class DictionaryLoader {
	private static final String LOG_TAG = "DictionaryLoader";
	@Nullable private static DictionaryLoader self;
	private static final String IMPORT_TIMER = "importTime";

	@NonNull private final SQLiteOpener sqlite;
	@NonNull private final InsertOps insertOps = new InsertOps();

	@NonNull private final DictionaryLoadingBar loadingBar;
	@Nullable private Thread loadThread;

	@NonNull private static final HashMap<Integer, Long> lastAutoLoadAttemptTime = new HashMap<>();
	private static boolean skipNextAutoLoad = false;
	private int currentFile = 0;
	@Nullable private final Uri localSource;
	@Nullable private final Consumer<Boolean> onComplete;
	private boolean importSuccessful;


	private DictionaryLoader(Context context, @Nullable Uri source, @Nullable Consumer<Boolean> onComplete) {
		loadingBar = DictionaryLoadingBar.getInstance(context);
		sqlite = WordDbOpener.getInstance(context);
		localSource = source;
		this.onComplete = onComplete;
	}


	public static void abort() {
		if (self != null) {
			self.stop();
			self.loadingBar.showCancelled();
		}
	}


	public static boolean load(@NonNull Context context, @NonNull SettingsStore settings, @Nullable ArrayList<Language> languages) {
		return load(context, settings, languages, null, null);
	}

	private static boolean load(@NonNull Context context, @NonNull SettingsStore settings, @Nullable ArrayList<Language> languages, @Nullable Uri source, @Nullable Consumer<Boolean> onComplete) {
		if (isRunning()) {
			return false;
		}

		if (languages == null || languages.isEmpty()) {
			Logger.d(LOG_TAG, "Nothing to do");
			return true;
		}

		if (self == null) {
			self = new DictionaryLoader(context, source, onComplete);
		}
		DictionaryLoader loader = self;
		loader.loadThread = new Thread(() -> {
			try {
				loader.loadSync(context, settings, languages);
			} finally {
				loader.loadThread = null;
				self = null;
				if (loader.onComplete != null) new Handler(Looper.getMainLooper()).post(() -> loader.onComplete.accept(loader.importSuccessful));
			}
		});
		loader.loadThread.start();

		return true;
	}


	public static void load(@NonNull Context context, @NonNull SettingsStore settings, @Nullable Language language) {
		ArrayList<Language> languages = new ArrayList<>(1);
		languages.add(language);
		load(context, settings, languages);
	}

	public static boolean loadChinesePack(@NonNull Context context, @NonNull SettingsStore settings, @NonNull Language language, @NonNull Uri source, @NonNull Consumer<Boolean> onComplete) {
		ArrayList<Language> languages = new ArrayList<>(1);
		languages.add(language);
		return LanguageKind.isChinesePinyin(language) && load(context, settings, languages, source, onComplete);
	}

	public static boolean downloadChinesePack(@NonNull Context context, @NonNull SettingsStore settings, @NonNull Language language, @NonNull Consumer<Boolean> onComplete) {
		ArrayList<Language> languages = new ArrayList<>(1);
		languages.add(language);
		return LanguageKind.isChinesePinyin(language) && load(context, settings, languages, null, onComplete);
	}


	public static boolean autoLoad(@NonNull InputMethodService context, @NonNull SettingsStore settings, @NonNull Language language) {
		if (!InputModePolicy.isDictionaryEnabled(language, settings.getPredictiveMode()) || isRunning()) {
			return false;
		}

		if (skipNextAutoLoad) {
			skipNextAutoLoad = false;
			return false;
		}

		final Long lastUpdateTime = lastAutoLoadAttemptTime.get(language.getId());
		final boolean isItTooSoon = lastUpdateTime != null && System.currentTimeMillis() - lastUpdateTime < SettingsStore.DICTIONARY_AUTO_LOAD_COOLDOWN_TIME;
		if (isItTooSoon) {
			return false;
		}

		DataStore.getLastLanguageUpdateTime(
			(hash) -> {
				lastAutoLoadAttemptTime.put(language.getId(), System.currentTimeMillis());

				final boolean noDictionary = hash == null || hash.isEmpty();
				// An existing Chinese dictionary deliberately emptied by Delete stays removed until an explicit load.
				if (LanguageKind.isChinesePinyin(language) && !noDictionary
					&& !new ReadOps().exists(WordDbOpener.getInstance(context).getDb(), language.getId())) return;
				// Local imports remain usable offline, but are not proof of the bundled dictionary's identity.
				final String comparisonHash = hash != null && hash.startsWith("local:") ? hash.substring(6) : hash;
				final boolean isDictionaryOutdated = noDictionary || !comparisonHash.equals(new WordFile(context, language, context.getAssets()).getHash());
				final boolean noNotifications = DeviceInfo.AT_LEAST_ANDROID_13;
				final boolean needsChinesePack = LanguageKind.isChinesePinyin(language) && !ChinesePackDownload.isCurrent(context);

				if (noDictionary || needsChinesePack || (isDictionaryOutdated && noNotifications)) {
					load(context, settings, language);
				} else if (isDictionaryOutdated) {
					new DictionaryUpdateNotification(context, language).show();
				}
			},
			language
		);

		return true;
	}


	public static void setSkipNextAutoLoad() {
		skipNextAutoLoad = true;
	}


	private void stop() {
		if (loadThread != null) {
			loadThread.interrupt();
		}
		Timer.stop(IMPORT_TIMER);
	}


	public static boolean isRunning() {
		return self != null && self.loadThread != null && self.loadThread.isAlive();
	}


	private void loadSync(@NonNull Context context, @NonNull SettingsStore settings, @NonNull ArrayList<Language> languages) {
		currentFile = 0;
		Timer.start(IMPORT_TIMER);

		loadingBar.showStart(languages.size());

		// SQLite does not support parallel queries, so let's import them one by one
		for (Language lang : languages) {
			if (loadThread != null && loadThread.isInterrupted()) {
				sendProgressMessage(lang, 0);
				break;
			}
			importAll(context, settings, lang);
			currentFile++;
		}

		Timer.stop(IMPORT_TIMER);
	}


	private void importAll(@NonNull Context context, @NonNull SettingsStore settings, @Nullable Language language) {
		if (language == null) {
			Logger.e(LOG_TAG, "Failed loading a dictionary for NULL language.");
			loadingBar.showError(InvalidLanguageException.class.getSimpleName(), null, -1);
			return;
		}

		ChinesePack.Prepared pack = null;
		try {
			Timer.start(LOG_TAG);

			float progress = 1;
			WordFile incoming = new WordFile(context, language, context.getAssets());
			ReadOps read = new ReadOps();
			boolean packOnly = localSource == null && LanguageKind.isChinesePinyin(language)
				&& !incoming.getHash().isEmpty()
				&& incoming.getHash().equals(read.getLanguageFileHash(sqlite.getDb(), language.getId()))
				&& read.exists(sqlite.getDb(), language.getId());

			sqlite.beginTransaction();
			if (packOnly) {
				try (BufferedReader reader = incoming.getReader()) {
					char[] buffer = new char[16384];
					while (reader.read(buffer) != -1) {
						if (Thread.currentThread().isInterrupted()) throw new DictionaryImportAbortedException();
					}
					pack = incoming.stageChinesePack();
				}
				if (pack != null) pack.activate();
				sqlite.finishTransaction();
				importSuccessful = true;
				if (pack != null) { pack.confirm(); ChinesePackInstaller.dictionaryInstalled(context); }
				sendProgressMessage(language, 100);
				return; // A feature-only update must preserve learned dictionary frequencies.
			}

			Tables.dropWordsIndexes(sqlite.getDb(), language);
			sendProgressMessage(language, ++progress);
			logLoadingStep("Indexes dropped", language, Timer.restart());

			DeleteOps.delete(sqlite.getDb(), language.getId());
			sendProgressMessage(language, ++progress);
			logLoadingStep("Storage cleared", language, Timer.restart());

			if (language instanceof NaturalLanguage) {
				((NaturalLanguage) language).updateKeyCharacters(settings);
			}
			int lettersCount = importLetters(language);
			sendProgressMessage(language, ++progress);
			logLoadingStep("Letters imported", language, Timer.restart());

			pack = importWordFile(context, language, lettersCount, progress, 88);
			progress = 88;
			sendProgressMessage(language, progress);
			logLoadingStep("Dictionary file imported", language, Timer.restart());

			Tables.createPositionIndex(sqlite.getDb(), language);
			progress = progress + (98f - progress) / 2f;
			sendProgressMessage(language, progress);
			Tables.createWordIndex(sqlite.getDb(), language);
			sendProgressMessage(language, progress = 98);
			logLoadingStep("Indexes restored", language, Timer.restart());

			int purgedWords = DeleteOps.purgeCustomWords(sqlite.getDb(), language.getId());
			sendProgressMessage(language, ++progress);
			logLoadingStep("Removed " + purgedWords + " custom words, which are already in the dictionary", language, Timer.restart());

			InsertOps.restoreCustomWords(sqlite.getDb(), language);
			sendProgressMessage(language, ++progress);
			logLoadingStep("Custom words restored", language, Timer.restart());

			if (pack != null) pack.activate();
			sqlite.finishTransaction();
			importSuccessful = true;
			if (pack != null) {
				pack.confirm();
				ChinesePackInstaller.dictionaryInstalled(context);
			}
			if (LanguageKind.isChinesePinyin(language) && (!ChinesePack.isDataAvailable(context) || !ChinesePackInstaller.isInstalled(context))) {
				ChinesePackInstaller.offerMissingPack(context);
			}
			SlowQueryStats.clear();
		} catch (DictionaryImportAbortedException e) {
			sqlite.failTransaction();
			stop();
			lastAutoLoadAttemptTime.put(language.getId(), null);
			Logger.i(LOG_TAG, e.getMessage() + ". File '" + language.getDictionaryFile() + "' not imported.");
		} catch (DictionaryImportException e) {
			stop();
			sqlite.failTransaction();
			lastAutoLoadAttemptTime.put(language.getId(), null);
			loadingBar.showError(DictionaryImportException.class.getSimpleName(), language, e.line);

			Logger.e(
				LOG_TAG,
				" Invalid word in dictionary: '" + language.getDictionaryFile() + "'"
				+ " of language '" + language.getName() + "'. "
				+ e.getMessage()
			);
		} catch (Exception | Error e) {
			stop();
			sqlite.failTransaction();
			loadingBar.showError(e.getClass().getSimpleName(), language, -1);

			if (e instanceof UnknownHostException) {
				lastAutoLoadAttemptTime.put(language.getId(), System.currentTimeMillis());
			} else {
				lastAutoLoadAttemptTime.put(language.getId(), null);
			}

			Logger.e(
				LOG_TAG,
				"Failed loading dictionary: " + language.getDictionaryFile()
				+ " for language '" + language.getName() + "'. "
				+ e.getClass().getSimpleName() + ": "
				+ e.getMessage()
			);
		} finally {
			if (pack != null) {
				try { pack.close(); } catch (IOException exception) {
					Logger.e(LOG_TAG, "Failed restoring Chinese pack: " + exception.getMessage());
				}
			}
			Timer.stop(LOG_TAG);
		}
	}


	private int importLetters(Language language) throws InvalidLanguageCharactersException {
		if (language.isTranscribed()) {
			return 0;
		}

		int lettersCount = 0;
		boolean isEnglish = LanguageKind.isEnglish(language);
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


	private ChinesePack.Prepared importWordFile(Context context, Language language, int positionShift, float minProgress, float maxProgress) throws Exception {
		WordFile wordFile = new WordFile(context, language, context.getAssets(), localSource);
		WordBatch batch = new WordBatch(language, SettingsStore.DICTIONARY_IMPORT_BATCH_SIZE + 1);
		float progressRatio = (maxProgress - minProgress) / wordFile.getWords();
		int wordCount = 0;

		positionShift = positionShift == 0 ? 1 : positionShift;

		try (BufferedReader ignored = wordFile.getReader()) {
			while (wordFile.notEOF()) {
				if (loadThread != null && loadThread.isInterrupted()) {
					throw new DictionaryImportAbortedException();
				}

				try {
					String digitSequence = wordFile.getNextSequence();
					ArrayList<String> words = wordFile.getNextWords(digitSequence);
					batch.add(words, digitSequence, wordCount + positionShift);
					wordCount += words.size();

					if (batch.getWords().size() > SettingsStore.DICTIONARY_IMPORT_BATCH_SIZE) {
						saveWordBatch(batch);
						batch.clear();
					}
				} catch (IOException e) {
					throw new DictionaryImportException(e.getMessage(), wordCount);
				}

				sendProgressMessage(language, minProgress + progressRatio * wordCount);
			}
			saveWordBatch(batch);
			String importedHash = localSource == null ? wordFile.getHash() : "local:" + wordFile.getHash();
			InsertOps.replaceLanguageMeta(sqlite.getDb(), language.getId(), importedHash);
			return wordFile.stageChinesePack();
		}
	}


	private void saveWordBatch(WordBatch batch) {
		for (int i = 0, end = batch.getWords().size(); i < end; i++) {
			insertOps.insertWord(sqlite.getDb(), batch.getLanguage(), batch.getWords().get(i));
		}

		for (int i = 0, end = batch.getPositions().size(); i < end; i++) {
			insertOps.insertWordPosition(sqlite.getDb(), batch.getLanguage(), batch.getPositions().get(i));
		}
	}


	private void sendProgressMessage(Language language, float progress) {
		loadingBar.showProgress(language, Timer.get(IMPORT_TIMER), currentFile, Math.round(progress));
	}


	private void logLoadingStep(String message, Language language, long time) {
		if (Logger.isDebugLevel()) {
			Logger.d(LOG_TAG, message + " for language '" + language.getName() + "' (" + language.getId() + ") in: " + time + " ms");
		}
	}
}
