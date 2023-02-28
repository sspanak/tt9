package io.github.sspanak.tt9.db.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
	indices = {
		@Index(value = {"lang", "word"}, unique = true),
		@Index(value = {"lang", "seq", "freq"})
	},
	tableName = "words"
)
public class Word {
	@PrimaryKey(autoGenerate = true)
	public int id;

	@ColumnInfo(name = "lang")
	public int langId;

	public String word;

	@ColumnInfo(name = "seq")
	public String sequence;

	@ColumnInfo(name = "freq")
	public int frequency;
}
