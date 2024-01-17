package io.github.sspanak.tt9.db.objectbox;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.config.DebugFlags;
import io.objectbox.query.Query;

public class WordStore {
	private BoxStore boxStore;
	private Box<Word> wordBox;
	private Box<SequenceRange> sequenceBox;

	private Query<Word> exactWordsQuery;
	private Query<Word> fuzzyWordsQuery;


	public WordStore(Context context) {
		init(context);
	}


	private void init(Context context) {
		boxStore = MyObjectBox.builder()
			.androidContext(context.getApplicationContext())
			.debugFlags(Logger.isDebugLevel() ? DebugFlags.LOG_QUERY_PARAMETERS : 0)
			.build();

		wordBox = boxStore.boxFor(Word.class);
		sequenceBox = boxStore.boxFor(SequenceRange.class);

//		exactWordsQuery = getExactWordsQuery();
//		fuzzyWordsQuery = getFuzzyWordsQuery();
	}


//	private QueryBuilder<Word> getBaseWordQuery() {
//		return boxStore.boxFor(Word.class).query()
//				.equal(Word_.langId, 0)
//				.startsWith(Word_.word, "", QueryBuilder.StringOrder.CASE_SENSITIVE);
//	}
//
//
//	private Query<Word> getExactWordsQuery() {
//		return getBaseWordQuery()
//			.equal(Word_.sequence, "", QueryBuilder.StringOrder.CASE_SENSITIVE)
//			.orderDesc(Word_.frequency)
//			.build();
//	}
//
//
//	private Query<Word> getFuzzyWordsQuery() {
//		return getBaseWordQuery()
//			.startsWith(Word_.sequence, "", QueryBuilder.StringOrder.CASE_SENSITIVE)
//			.order(Word_.length)
//			.orderDesc(Word_.frequency)
//			.build();
//	}


	public long count(int langId) {
		return 0;
//			try (Query<Word> query = wordBox.query(Word_.langId.equal(langId)).build()) {
//				return query.count();
//			} catch (Exception e) {
//				return 0;
//			}
	}


	public boolean exists(int langId, @NonNull String word, @NonNull String sequence) {
		return get(langId, word, sequence) != null;
	}


	@Nullable
	public Word get(int langId, @NonNull String word, @NonNull String sequence) {
		// @todo: try searching by the unique "langId_word"
//		QueryCondition<Word> where = Word_.langId.equal(langId)
//			.and(Word_.sequenceShort.equal(Word.shrinkSequence(sequence)))
//			.and(Word_.word.equal(word, QueryBuilder.StringOrder.CASE_SENSITIVE));
//
//		try (Query<Word> query = wordBox.query(where).build()) {
//			return query.findFirst();
//		}

		return null;
	}


	@Nullable
	public List<Word> getMany(int langId) {
		return new WordList();
//		try (Query<Word> query = wordBox.query(Word_.langId.equal(langId)).build()) {
//			return query.find();
//		}
	}


	@NonNull
	public WordList getMany(Language language, @NonNull String sequence, @Nullable String filter, int maxWords) {
		WordList words = new WordList();

		return words;
	}


	public int[] getLanguages() {
		return null;
//		try (Query<Word> query = wordBox.query().build()) {
//			return query.property(Word_.langId).distinct().findInts();
//		}
	}


	public int getMaxFrequency(int langId) {
		return getMaxFrequency(langId, null, null);
	}


	public int getMaxFrequency(int langId, String sequence, String word) {
		return 0;
//		QueryCondition<Word> where = Word_.langId.equal(langId);
//
//		if (sequence != null && word != null) {
//			where = where
//				.and(Word_.sequenceShort.equal(Word.shrinkSequence(sequence)))
//				.and(Word_.sequence.equal(sequence))
//				.and(Word_.word.notEqual(word)); // @todo: Word_.id.notEqual(...)
//		}
//
//		try (Query<Word> query = wordBox.query(where).build()) {
//			long max = query.property(Word_.frequency).max();
//			return max == Long.MIN_VALUE ? 0 : (int)max;
//		}
	}


	public void put(@NonNull WordBatch batch) {
		wordBox.put(batch.words);
		sequenceBox.put(batch.sequences);
	}


	public void put(@NonNull Word word, @NonNull SequenceRange sequenceRange) {
		wordBox.put(word);
		sequenceBox.put(sequenceRange);
	}


	public WordStore removeMany(@NonNull ArrayList<Integer> languageIds) {
//		if (languageIds.size() > 0) {
//			try (Query<Word> query = wordBox.query(Word_.langId.oneOf(IntegerListToIntArray(languageIds))).build()) {
//				query.remove();
//			}
//		}

		return this;
	}


	public void destroy() {
		boxStore.closeThreadResources();
		boxStore.close();
		boxStore.deleteAllFiles();
	}


	public void runInTransaction(Runnable r) {
		boxStore.runInTx(r);
	}


	public void runInTransactionAsync(Runnable action) {
		boxStore.runInTxAsync(action, null);
	}


	public void closeThreadResources() {
		boxStore.closeThreadResources();
	}


	private int[] IntegerListToIntArray(ArrayList<Integer> in) {
		int[] out = new int[in.size()];
		Iterator<Integer> iterator = in.iterator();
		for (int i = 0; i < out.length; i++) {
			out[i] = iterator.next();
		}
		return out;
	}
}
