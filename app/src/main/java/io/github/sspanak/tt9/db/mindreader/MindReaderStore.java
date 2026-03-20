package io.github.sspanak.tt9.db.mindreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.db.BaseSyncStore;
import io.github.sspanak.tt9.db.sqlite.DeleteOps;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.ReadOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.ime.mindreader.MindReaderDictionary;
import io.github.sspanak.tt9.ime.mindreader.MindReaderNgramList;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class MindReaderStore extends BaseSyncStore {
	private static final String LOG_TAG = MindReaderStore.class.getSimpleName();

	private InsertOps insertOps;
	private final ReadOps readOps = new ReadOps();

	private long lastLoadNgramsTime = 0;
	private long lastLoadTokensTime = 0;
	private long lastSaveNgramsTime = 0;
	private long lastSaveTokensTime = 0;


	public MindReaderStore(@NonNull Context context) {
		super(context);
	}


	@NonNull
	@Override
	protected SQLiteOpener openDb(Context context) {
		return MindReaderDbOpener.getInstance(context);
	}


	private void getInsertOps(@NonNull Language language) {
		if (insertOps == null) {
			insertOps = new InsertOps(sqlite.getDb(), language);
		}
	}


	@NonNull
	public MindReaderNgramList loadNgrams(@NonNull Language language) {
		Timer.start(LOG_TAG);

		final MindReaderNgramList ngrams = new MindReaderNgramList();

		if (checkOrNotify()) {
			ngrams.addAllUnsafe(readOps.getMindReaderNgrams(sqlite.getDb(), language.getId()));
		}

		lastLoadNgramsTime = Timer.stop(LOG_TAG);
		Logger.d(LOG_TAG, "Loaded " + ngrams.size() + " N-grams for: " + language + " in " + lastLoadNgramsTime + " ms.");

		return ngrams;
	}


	@NonNull
	public MindReaderDictionary loadDictionary(@NonNull Language language) {
		Timer.start(LOG_TAG);

		final MindReaderDictionary dictionary = new MindReaderDictionary(language);

		if (checkOrNotify()) {
			dictionary.addAllUnsafe(readOps.getMindReaderTokens(sqlite.getDb(), language.getId()));
		}

		lastLoadTokensTime = Timer.stop(LOG_TAG);
		Logger.d(LOG_TAG, "Loaded " + dictionary.size() + " tokens for: " + language + " in " + lastLoadTokensTime + " ms.");

		return dictionary;
	}


	public void save(@Nullable Language language, @NonNull MindReaderNgramList ngrams, @NonNull MindReaderDictionary dictionary) {
		if (!checkOrNotify() || language == null) {
			return;
		}

		Timer.start(LOG_TAG + "_save");
		long deleteNgramsTime = 0;
		long deleteTokensTime = 0;
		long saveNgramsTime = 0;
		long saveTokensTime = 0;
		int ngramCount = 0;
		int tokenCount = 0;

		final SQLiteDatabase db = sqlite.getDb();

		db.beginTransaction();
		try {
			getInsertOps(language);

			if (ngrams.dirty()) {
				Timer.start(LOG_TAG);
				DeleteOps.deleteMindReaderNgrams(db, language.getId());
				deleteNgramsTime = Timer.stop(LOG_TAG);

				Timer.start(LOG_TAG);
				ngramCount = insertOps.insertMindReaderNgrams(db, language.getId(), ngrams.getBefore(), ngrams.getNext());
				saveNgramsTime = Timer.stop(LOG_TAG);
			}

			if (dictionary.dirty()) {
				Timer.start(LOG_TAG);
				DeleteOps.deleteMindReaderTokens(db, language.getId());
				deleteTokensTime = Timer.stop(LOG_TAG);

				Timer.start(LOG_TAG);
				tokenCount = insertOps.insertMindReaderTokens(db, language.getId(), dictionary.getAll());
				saveTokensTime = Timer.stop(LOG_TAG);
			}

			lastSaveNgramsTime = saveNgramsTime + deleteNgramsTime;
			lastSaveTokensTime = saveTokensTime + deleteTokensTime;

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			Timer.stop(LOG_TAG); // avoid timer memory leaks
		}

		printSaveSummary(language, Timer.stop(LOG_TAG + "_save"), deleteNgramsTime, deleteTokensTime, saveNgramsTime, saveTokensTime, ngramCount, tokenCount);
	}


	private void printSaveSummary(Language language, long totalTime, long deleteNgramsTime, long deleteTokensTime, long saveNgramsTime, long saveTokensTime, int ngramCount, int tokenCount) {
		if (Logger.isVerboseLevel()) {
			Logger.v(LOG_TAG,
				"Saved mind reading data for: " + language +
				".\nTime: " + totalTime +
				" ms (delete N-grams: " + deleteNgramsTime + " ms" +
				", delete tokens: " + deleteTokensTime + " ms" +
				", save " + ngramCount + " N-grams: " + saveNgramsTime + " ms" +
				", save " + tokenCount + " tokens: " + saveTokensTime + " ms).");
		} else if (Logger.isDebugLevel()) {
			Logger.d(LOG_TAG,
				"Saved mind reading data for: " + language +
					". Time: " + totalTime + " ms (N-grams: " + lastSaveNgramsTime +
					" ms, tokens: " + lastSaveTokensTime + " ms)."
			);
		}
	}


	public long getLastLoadNgramsTime() { return lastLoadNgramsTime; }
	public long getLastLoadTokensTime() { return lastLoadTokensTime; }
	public long getLastSaveNgramsTime() { return lastSaveNgramsTime; }
	public long getLastSaveTokensTime() { return lastSaveTokensTime; }
}
