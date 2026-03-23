package io.github.sspanak.tt9.ime.mindreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MindReaderStats {
	private static final MindReaderStats self = new MindReaderStats();

	private MindReaderStats() {}

	private volatile boolean isOff = true;
	private volatile int dictionarySize = 0;
	private volatile int ngramsSize = 0;
	private volatile int ngramsCapacity = 0;
	@Nullable private volatile String language = null;

	private volatile long countComplete = 0;
	private volatile long countGuess = 0;
	private volatile long countSetContext = 0;
	private volatile long countSetLanguage = 0;

	private volatile long totalComplete = 0;
	private volatile long totalGuess = 0;
	private volatile long totalSetContext = 0;
	private volatile long totalSetLanguage = 0;

	private volatile long slowestComplete = 0;
	private volatile long slowestGuess = 0;
	private volatile long slowestSetContext = 0;
	private volatile long slowestSetLanguage = 0;

	private volatile long slowestLoadNgrams = 0;
	private volatile long slowestLoadTokens = 0;
	private volatile long slowestSaveNgrams = 0;
	private volatile long slowestSaveTokens = 0;


	@NonNull
	public static MindReaderStats getInstance() {
		return self;
	}


	@NonNull
	public static String get() {
		return getInstance().generate();
	}


	MindReaderStats update(@NonNull MindReader mindReader) {
		isOff = mindReader.isOff();
		dictionarySize = mindReader.dictionary.size();
		ngramsSize = mindReader.ngrams.size();
		ngramsCapacity = mindReader.ngrams.capacity();
		language = mindReader.wordContext.language != null ? mindReader.wordContext.language.toString() : null;

		return this;
	}


	synchronized void resetTimings() {
		countComplete = countGuess = countSetContext = countSetLanguage = 0;
		totalComplete = totalGuess = totalSetContext = totalSetLanguage = 0;
		slowestComplete = slowestGuess = slowestSetContext = slowestSetLanguage = 0;
		slowestLoadNgrams = slowestLoadTokens = slowestSaveNgrams = slowestSaveTokens = 0;
	}


	void setOff(boolean off) {
		isOff = off;
	}


	synchronized void setCompleteTime(long time) {
		countComplete++;
		totalComplete += time;
		slowestComplete = Math.max(slowestComplete, time);
	}


	synchronized MindReaderStats setDbTimes(long loadNgrams, long loadTokens, long saveNgrams, long saveTokens) {
		slowestLoadNgrams = Math.max(slowestLoadNgrams, loadNgrams);
		slowestLoadTokens = Math.max(slowestLoadTokens, loadTokens);
		slowestSaveNgrams = Math.max(slowestSaveNgrams, saveNgrams);
		slowestSaveTokens = Math.max(slowestSaveTokens, saveTokens);
		return this;
	}


	synchronized void setGuessTime(long time) {
		countGuess++;
		totalGuess += time;
		slowestGuess = Math.max(slowestGuess, time);
	}


	synchronized void setChangeContextTime(long time) {
		countSetContext++;
		totalSetContext += time;
		slowestSetContext = Math.max(slowestSetContext, time);
	}


	synchronized void setChangeLanguageTime(long time) {
		countSetLanguage++;
		totalSetLanguage += time;
		slowestSetLanguage = Math.max(slowestSetLanguage, time);
	}


	@NonNull
	private String generate() {
		final StringBuilder sb = new StringBuilder();

		sb.append("Status").append(isOff ? ": Off\n" : ": On\n");
		sb.append("Language: ").append(language).append("\n");
		sb.append("Dictionary size: ").append(dictionarySize).append(" tokens\n");
		sb.append("N-grams: ").append(ngramsSize).append(" / ").append(ngramsCapacity).append("\n");

		if (!isOff) {
			sb.append("\n -== Operations ==-\n");
			sb.append("\nComplete: ").append(countComplete)
				.append(".  Avg: ").append(countComplete == 0 ? 0 : totalComplete / countComplete)
				.append(" ms.  Max: ")
				.append(slowestComplete).append(" ms\n");
			sb.append("Guess: ").append(countGuess)
				.append(".  Avg: ").append(countGuess == 0 ? 0 : totalGuess / countGuess)
				.append(" ms.  Max: ")
				.append(slowestGuess).append(" ms\n");
			sb.append("Context: ").append(countSetContext)
				.append(".  Avg: ").append(countSetContext == 0 ? 0 : totalSetContext / countSetContext)
				.append(" ms.  Max: ")
				.append(slowestSetContext).append(" ms\n");
			sb.append("Language: ").append(countSetLanguage)
				.append(".  Avg: ").append(countSetLanguage == 0 ? 0 : totalSetLanguage / countSetLanguage)
				.append(" ms.  Max: ")
				.append(slowestSetLanguage).append(" ms\n");

			sb.append("\n -== Max Db Times ==-\n")
				.append("\nLoad. N-grams: ").append(slowestLoadNgrams)
				.append(" ms. Tokens: ").append(slowestLoadTokens).append(" ms")
				.append("\nSave. N-grams: ").append(slowestSaveNgrams)
				.append(" ms. Tokens: ").append(slowestSaveTokens).append(" ms");
		}

		return sb.toString();
	}
}
