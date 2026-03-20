package io.github.sspanak.tt9.db.mindreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.db.BaseSyncStore;
import io.github.sspanak.tt9.db.sqlite.DeleteOps;
import io.github.sspanak.tt9.db.sqlite.InsertOps;
import io.github.sspanak.tt9.db.sqlite.SQLiteOpener;
import io.github.sspanak.tt9.ime.mindreader.MindReaderDictionary;
import io.github.sspanak.tt9.ime.mindreader.MindReaderNgramList;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class MindReaderStore extends BaseSyncStore {
	private static final String LOG_TAG = MindReaderStore.class.getSimpleName();

	private InsertOps insertOps;


	public MindReaderStore(@NonNull Context context) {
		super(context);
	}


	@NonNull
	@Override
	protected SQLiteOpener openDb(Context context) {
		return MindReaderDbOpener.getInstance(context);
	}


	@NonNull
	private InsertOps getInsertOps(@NonNull Language language) {
		if (insertOps == null) {
			insertOps = new InsertOps(sqlite.getDb(), language);
		}
		return insertOps;
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

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			Timer.stop(LOG_TAG); // avoid timer memory leaks
		}

		printSaveSummary(language, Timer.stop(LOG_TAG + "_save"), deleteNgramsTime, deleteTokensTime, saveNgramsTime, saveTokensTime, ngramCount, tokenCount);
	}

	private void printSaveSummary(Language language, long stop, long deleteNgramsTime, long deleteTokensTime, long saveNgramsTime, long saveTokensTime, int ngramCount, int tokenCount) {
		if (!Logger.isDebugLevel()) {
			return;
		}

		final String log = "Saved mind reading data for: " + language +
			".\nTime: " + stop +
			" ms (delete N-grams: " + deleteNgramsTime + " ms" +
			", delete tokens: " + deleteTokensTime + " ms" +
			", save " + ngramCount + " N-grams: " + saveNgramsTime + " ms" +
			", save " + tokenCount + " tokens: " + saveTokensTime + " ms).";

		Logger.d(LOG_TAG, log);
	}
}
