package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.HashMap;

class CompiledQueryCache {
	private static CompiledQueryCache self;
	private final SQLiteDatabase db;
	private final HashMap<Integer, SQLiteStatement> statements = new HashMap<>();

	private CompiledQueryCache(@NonNull SQLiteDatabase db) {
		this.db = db;
	}

	CompiledQueryCache execute(String sql) {
		get(sql).execute();
		return this;
	}

	SQLiteStatement get(@NonNull String sql) {
		SQLiteStatement statement = statements.get(sql.hashCode());
		if (statement == null) {
			statement = db.compileStatement(sql);
			statements.put(sql.hashCode(), statement);
		}

		return statement;
	}

	long simpleQueryForLong(String sql, long defaultValue) {
		try {
			return get(sql).simpleQueryForLong();
		} catch (SQLiteDoneException e) {
			return defaultValue;
		}
	}


	static CompiledQueryCache getInstance(SQLiteDatabase db) {
		if (self == null) {
			self = new CompiledQueryCache(db);
		}

		return self;
	}

	static CompiledQueryCache execute(SQLiteDatabase db, String sql) {
		return getInstance(db).execute(sql);
	}

	static SQLiteStatement get(SQLiteDatabase db, String sql) {
		return getInstance(db).get(sql);
	}

	static long simpleQueryForLong(SQLiteDatabase db, String sql, long defaultValue) {
		return getInstance(db).simpleQueryForLong(sql, defaultValue);
	}
}
