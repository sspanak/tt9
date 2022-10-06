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
		"WHERE " +
			"lang = :langId " +
			"AND seq = :sequence " +
			"AND (:word IS NULL OR word LIKE :word || '%') " +
		"ORDER BY freq DESC " +
		"LIMIT :limit"
	)
	List<Word> getMany(int langId, int limit, String sequence, String word);

	@Query(
		"SELECT * " +
		"FROM words " +
		"WHERE " +
			"lang = :langId " +
			"AND seq > :sequence AND seq <= :sequence || '99' " +
			"AND (:word IS NULL OR word LIKE :word || '%') " +
		"ORDER BY freq DESC, LENGTH(seq) ASC, seq ASC " +
		"LIMIT :limit"
	)
	List<Word> getFuzzy(int langId, int limit, String sequence, String word);

	@Insert
	void insert(Word word);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insertMany(List<Word> words);

	@Query(
		"UPDATE words " +
		"SET freq = (SELECT IFNULL(MAX(freq), 0) FROM words WHERE lang = :langId AND seq = :sequence AND word <> :word) + 1 " +
		"WHERE lang = :langId AND word = :word AND seq = :sequence"
	)
	int incrementFrequency(int langId, String word, String sequence);
}
