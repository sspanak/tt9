package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import io.github.sspanak.tt9.util.Logger;

public class WordFileLine {
	public final String digitSequence;
	public final ArrayList<String> words;


	public WordFileLine(String lineData) throws Exception {
		byte[] lineBytes = lineData.getBytes();
		if (lineBytes.length < 1) {
			throw new IllegalArgumentException("Line is empty.");
		}

		byte wordLength = lineBytes[0];
		byte sequenceLength = (byte) ((wordLength + 1) / 2);
		digitSequence = decodeDigitSequence(lineBytes, wordLength, sequenceLength);
		words = decodeWords(lineData, wordLength, sequenceLength);
	}

	private String decodeDigitSequence(byte[] line, byte wordLength, byte sequenceLength) {
		StringBuilder sequence = new StringBuilder();

		for (int i = 1; i <= sequenceLength; i++) {
			int high = ((line[i] & 0xF0) >> 4);
			int low = (line[i] & 0x0F);

			Logger.d("WordFileLine", "Decoding: " + high + " " + low + " byte: " + line[i]);

			sequence.append(high);
			if (sequence.length() < wordLength) {
				sequence.append(low);
			}
		}

		return sequence.toString();
	}

	private ArrayList<String> decodeWords(String line, byte wordLength, byte sequenceLength) {
		ArrayList<String> words = new ArrayList<>();

		for (int i = 1 + sequenceLength; i < line.length(); i += wordLength) {
			words.add(line.substring(i, i + wordLength));
		}

		return words;
	}

/*
	public WordFileLine(String lineData) throws Exception {
		Logger.d("WordFileLine", "Parsing line: " + lineData);

		ByteBuffer buffer = ByteBuffer.wrap(lineData.getBytes(StandardCharsets.UTF_8));

		int wordLength = Byte.toUnsignedInt(buffer.get());
		this.digitSequence = decodeDigitSequence(buffer, wordLength);

		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		while (buffer.hasRemaining()) {
			words.add(decodeFixedLengthWord(buffer, wordLength, decoder));
		}
	}





	private String decodeFixedLengthWord(ByteBuffer buffer, int wordLength, CharsetDecoder decoder) throws Exception {
		// Buffer for decoding up to `wordLength` characters
		ByteBuffer charBytes = ByteBuffer.allocate(4 * wordLength); // Max 4 bytes per UTF-8 character
		int charsRead = 0;

		// Read bytes until decoding `wordLength` characters
		while (charsRead < wordLength && buffer.hasRemaining()) {
			charBytes.put(buffer.get());
			charBytes.flip();
			if (decoder.decode(charBytes).length() > 0) {
				charsRead++;
			}
			charBytes.compact();
		}

		charBytes.flip();
		return decoder.decode(charBytes).toString();
	}
*/
	@NonNull
	@Override
	public String toString() {
		return "WordFileLine{" +
				"words=" + words +
				", digitSequence='" + digitSequence + '\'' +
				'}';
	}
}
