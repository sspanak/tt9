package io.github.sspanak.tt9.db.words;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.github.sspanak.tt9.db.BaseSyncStore;
import io.github.sspanak.tt9.db.sqlite.DeleteOps;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

public class DictionaryDeleter extends BaseSyncStore {
	private static final String LOG_TAG = DictionaryDeleter.class.getSimpleName();
	private static DictionaryDeleter self;

	@NonNull private final ExecutorService executor = Executors.newSingleThreadExecutor();
	@Nullable Future<?> deleteTask;
	@Nullable private Runnable notification;


	protected DictionaryDeleter(Context context) {
		super(context);
	}


	public static DictionaryDeleter getInstance(Context context) {
		if (self == null) {
			self = new DictionaryDeleter(context);
		}
		return self;
	}


	public void deleteLanguages(@NonNull ArrayList<Language> languages) {
		if (!checkOrNotify()) {
			onFinish();
			return;
		}

		Timer.start(LOG_TAG);
		deleteTask = executor.submit(() -> deleteLanguagesSync(languages));
	}


	private void deleteLanguagesSync(@NonNull ArrayList<Language> languages) {
		for (Language language : languages) {
			if (!deleteLanguage(language)) {
				break;
			}
		}

		deleteTask = null;
		onFinish();

		Logger.d(LOG_TAG, "Deleted " + languages.size() + " languages. Time: " + Timer.stop(LOG_TAG) + " ms");
	}


	private boolean deleteLanguage(Language language) {
		if (!(language instanceof NaturalLanguage)) {
			Logger.w(LOG_TAG, "Invalid language type to delete: " + language.getClass().getSimpleName() + ". Skipping.");
			return true;
		}

		try {
			sqlite.beginTransaction();
			DeleteOps.delete(sqlite.getDb(), language.getId());
			DeleteOps.deleteWordPairs(sqlite.getDb(), language.getId());
			sqlite.finishTransaction();
		} catch (Exception e) {
			sqlite.failTransaction();
			Logger.e(LOG_TAG, "Failed deleting language: " + language.getId() + ". " + e.getMessage());
			return false;
		}

		return true;
	}


	private void onFinish() {
		if (notification != null) {
			notification.run();
			notification = null;
		}
	}


	public boolean isRunning() {
		return deleteTask != null && !deleteTask.isDone();
	}


	public void setOnFinish(Runnable notification) {
		this.notification = notification;
	}
}
