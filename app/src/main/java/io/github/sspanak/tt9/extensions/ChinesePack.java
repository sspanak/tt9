package io.github.sspanak.tt9.extensions;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.github.sspanak.tt9.util.Logger;

/** Private data shipped beside the Chinese dictionary and its optional Android split. */
public final class ChinesePack {
	public static final String ARCHIVE_PREFIX = "chinese/";
	public static final String APK_NAME = "chinese.apk";
	public static final String LANGUAGE_APK_NAME = "chinese-ui.apk";
	private static final String DIRECTORY = "chinese-pack";
	private static final int MAX_FILES = 12;
	private static final int MAX_BYTES = 64 * 1024 * 1024;
	private static final AtomicLong generation = new AtomicLong();

	private ChinesePack() {}

	public static long getGeneration() { return generation.get(); }

	public static boolean isDataAvailable(Context context) { return isDataAvailable(context.getFilesDir()); }

	static boolean isDataAvailable(File filesDirectory) {
		return containsData(new File(filesDirectory, DIRECTORY));
	}

	private static boolean containsData(File directory) {
		return containsNonemptyFile(directory, "common-3500.txt") && containsNonemptyFile(directory, "zh-simplified-prefixes.bin");
	}

	private static boolean containsNonemptyFile(File directory, String name) {
		File file = new File(directory, name);
		return file.isFile() && file.length() > 0;
	}

	public static InputStream open(Context context, String name) throws IOException {
		return new FileInputStream(new File(directory(context), checkedName(name)));
	}

	public static File getApk(Context context) {
		return new File(directory(context), APK_NAME);
	}

	public static File getLanguageApk(Context context) {
		return new File(directory(context), LANGUAGE_APK_NAME);
	}

	private static File directory(Context context) {
		return new File(context.getFilesDir(), DIRECTORY);
	}

	private static String checkedName(String name) throws IOException {
		if (name == null || !name.matches("[A-Za-z0-9_-][A-Za-z0-9._-]*") || name.contains("..")) {
			throw new IOException("Invalid Chinese pack filename");
		}
		return name;
	}

	/** Called after the dictionary's first ZIP entry has reached EOF. */
	public static Prepared stage(Context context, ZipInputStream archive) throws IOException {
		return stage(context.getFilesDir(), archive);
	}

	static Prepared stage(File filesDirectory, ZipInputStream archive) throws IOException {
		File destination = new File(filesDirectory, DIRECTORY);
		File pending = new File(filesDirectory, DIRECTORY + "-pending-" + UUID.randomUUID());
		if (!pending.mkdir()) throw new IOException("Cannot stage Chinese pack");
		Prepared prepared = new Prepared(destination, pending);
		HashSet<String> files = new HashSet<>();
		try {
			byte[] buffer = new byte[16384];
			int total = 0;
			int entries = 0;
			for (ZipEntry entry; (entry = archive.getNextEntry()) != null;) {
				if (++entries > MAX_FILES) throw new IOException("Too many Chinese pack entries");
				if (entry.isDirectory() || !entry.getName().startsWith(ARCHIVE_PREFIX)) {
					total = copyEntry(archive, null, buffer, total);
					continue;
				}
				String name = checkedName(entry.getName().substring(ARCHIVE_PREFIX.length()));
				if (!files.add(name)) throw new IOException("Duplicate Chinese pack entry");
				try (FileOutputStream output = new FileOutputStream(new File(pending, name))) {
					total = copyEntry(archive, output, buffer, total);
				}
			}
			if (files.isEmpty()) {
				prepared.close();
				return null; // Older dictionaries contain only their word entry.
			}
			return prepared;
		} catch (IOException | RuntimeException exception) {
			prepared.close();
			throw exception;
		}
	}

	private static int copyEntry(InputStream archive, OutputStream output, byte[] buffer, int total) throws IOException {
		for (int size; (size = archive.read(buffer)) != -1;) {
			total += size;
			if (total > MAX_BYTES) throw new IOException("Chinese pack is too large");
			if (output != null) output.write(buffer, 0, size);
		}
		return total;
	}

	public static void remove(Context context) {
		generation.incrementAndGet();
		try {
			removeFiles(context.getFilesDir());
		} catch (IOException exception) {
			Logger.e(ChinesePack.class.getSimpleName(), exception.getMessage());
		}
		ChinesePackInstaller.dictionaryRemoved(context);
	}

	static void removeFiles(File filesDirectory) throws IOException {
		delete(new File(filesDirectory, DIRECTORY));
		File[] siblings = filesDirectory.listFiles();
		if (siblings == null) return;
		for (File file : siblings) {
			if (file.getName().matches(DIRECTORY + "-pending-[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}(-previous)?")
				&& filesDirectory.getCanonicalFile().equals(file.getCanonicalFile().getParentFile())) delete(file);
		}
	}

	private static void delete(File file) throws IOException {
		File[] children = file.listFiles();
		if (children != null) for (File child : children) delete(child);
		if (file.exists() && !file.delete()) throw new IOException("Cannot delete Chinese pack file: " + file.getName());
	}

	/** Restores the preceding pack if the surrounding dictionary transaction fails. */
	public static final class Prepared implements AutoCloseable {
		private final File destination;
		private final File pending;
		private final File backup;
		private boolean activated;
		private boolean confirmed;
		private boolean closed;

		private Prepared(File destination, File pending) {
			this.destination = destination;
			this.pending = pending;
			backup = new File(pending.getPath() + "-previous");
		}

		public boolean isComplete() {
			return containsData(pending) && containsNonemptyFile(pending, APK_NAME) && containsNonemptyFile(pending, LANGUAGE_APK_NAME);
		}

		public void validateDelivery(Context context, boolean required) throws IOException {
			ChinesePackDownload.validate(context, pending, required);
		}

		public void activate() throws IOException {
			if (destination.exists() && !destination.renameTo(backup)) throw new IOException("Cannot replace Chinese pack");
			if (!pending.renameTo(destination)) {
				if (backup.exists()) backup.renameTo(destination);
				throw new IOException("Cannot activate Chinese pack");
			}
			activated = true;
		}

		public void confirm() {
			if (confirmed) return;
			confirmed = true;
			generation.incrementAndGet();
			try { delete(backup); } catch (IOException exception) {
				Logger.w(ChinesePack.class.getSimpleName(), exception.getMessage());
			}
		}

		@Override public void close() throws IOException {
			if (closed) return;
			if (!confirmed && (activated || backup.exists())) {
				delete(destination);
				if (backup.exists() && !backup.renameTo(destination)) throw new IOException("Cannot restore Chinese pack");
			}
			delete(pending);
			closed = true;
		}
	}
}
