package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

import java.util.HashMap;

class CompiledQueryCache {
	private static CompiledQueryCache self;
	private final SQLiteDatabase db;
	private final HashMap<Integer, SQLiteStatement> statements = new HashMap<>();

	CompiledQueryCache(SQLiteDatabase db) {
		this.db = db;
	}

	static CompiledQueryCache getInstance(SQLiteDatabase db) {
		if (self == null) {
			self = new CompiledQueryCache(db);
		}

		return self;
	}

	SQLiteStatement get(String sql) {
		SQLiteStatement statement = statements.get(sql.hashCode());
		if (statement == null) {
			statement = db.compileStatement(sql);
			statements.put(sql.hashCode(), statement);
		}

		return statement;
	}

	void execute(String sql) {
		get(sql).execute();
	}

	long simpleQueryForLong(String sql, long defaultValue) {
		try {
			return get(sql).simpleQueryForLong();
		} catch (SQLiteDoneException e) {
			return defaultValue;
		}
	}
}
