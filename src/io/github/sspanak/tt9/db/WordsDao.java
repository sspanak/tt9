package io.github.sspanak.tt9.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
interface WordsDao {
	@Query(
		"SELECT * " +
		"FROM words " +
		"WHERE lang = :langId AND seq = :sequence " +
		"ORDER BY freq DESC " +
		"LIMIT :limit"
	)
	List<Word> getMany(int langId, String sequence, int limit);

	@Query(
		"SELECT * " +
		"FROM words " +
		"WHERE lang = :langId AND seq > :sequence AND seq < :sequence + 1 " +
		"ORDER BY freq DESC, seq ASC " +
		"LIMIT :limit"
	)
	List<Word> getFuzzy(int langId, String sequence, int limit);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insertWords(List<Word> words);

	@Query(
		"UPDATE words " +
		"SET freq = (SELECT IFNULL(MAX(freq), 0) FROM words WHERE lang = :langId AND seq = :sequence AND word <> :word) + 1 " +
		"WHERE lang = :langId AND word = :word "
	)
	void incrementFrequency(int langId, String word, String sequence);
}
