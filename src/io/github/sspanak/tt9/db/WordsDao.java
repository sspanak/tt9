package io.github.sspanak.tt9.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
interface WordsDao {
	@Query("SELECT COUNT(id) FROM words WHERE :langId < 0 OR lang = :langId")
	int count(int langId);

	@Query("SELECT COUNT(id) FROM words WHERE lang = :langId AND word = :word")
	int doesWordExist(int langId, String word);

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
		"ORDER BY LENGTH(seq) ASC, freq DESC, seq ASC " +
		"LIMIT :limit"
	)
	List<Word> getFuzzy(int langId, int limit, String sequence, String word);

	@Insert
	void insert(Word word);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void upsertMany(List<Word> words);

	@Query(
		"UPDATE words " +
		"SET freq = (SELECT IFNULL(MAX(freq), 0) FROM words WHERE lang = :langId AND seq = :sequence AND word <> :word) + 1 " +
		"WHERE lang = :langId AND word = :word AND seq = :sequence"
	)
	int incrementFrequency(int langId, String word, String sequence);

	@Query(
		"UPDATE words " +
		"SET freq = freq / :normalizationDivider " +
		"WHERE lang IN ( " +
			"SELECT lang " +
			"FROM words " +
			"WHERE freq >= :maxFrequency " +
			"GROUP BY lang" +
		")"
	)
	int normalizeFrequencies(int normalizationDivider, int maxFrequency);
}
