package io.github.sspanak.tt9.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WordsDao {
	@Query(
		"SELECT * " +
		"FROM words " +
		"WHERE lang = :langId AND seq = :sequence " +
		"ORDER BY freq DESC " +
		"LIMIT :limit"
	)
	List<Word> getWordsBySequence(int langId, String sequence, int limit);

	@Query(
		"SELECT * " +
		"FROM words " +
		"WHERE " +
			"lang = :langId " +
			"AND seq >= :startSequence " +
			"AND seq < :endSequence " +
		"ORDER BY freq DESC, seq ASC " +
		"LIMIT :limit"
	)
	List<Word> getWordsBySequenceRange(int langId, String startSequence, String endSequence, int limit);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insertWords(List<Word> words);
}
