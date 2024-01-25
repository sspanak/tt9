package io.github.sspanak.tt9.db.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.HashMap;

import io.github.sspanak.tt9.Logger;

class CompiledQueryCache {
	private final SQLiteDatabase db;
	private final HashMap<Integer, SQLiteStatement> statements = new HashMap<>();

	CompiledQueryCache(SQLiteDatabase db) {
		this.db = db;
	}

	SQLiteStatement getOrCreate(String sql) {
		SQLiteStatement statement = statements.get(sql.hashCode());
		if (statement == null) {
			statement = db.compileStatement(sql);
			statements.put(sql.hashCode(), statement);
		}

		return statement;
	}
}
