package io.github.sspanak.tt9.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Properties;

/** Identity and integrity checks shared by online and locally imported Chinese packs. */
public final class ChinesePackDelivery {
	private static final int MAX_METADATA_BYTES = 8192;
	private static final String PACKAGE = "[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+";
	private static final String TOKEN = "[A-Za-z][A-Za-z0-9]*";
	private static final String SHA256 = "[0-9a-fA-F]{64}";

	private ChinesePackDelivery() {}

	public static String assetName(String packageName, long versionCode, String flavor, String buildType, String certificateSha256) {
		if (!matches(packageName, PACKAGE) || versionCode <= 0 || !matches(flavor, TOKEN) || !matches(buildType, TOKEN)
			|| !matches(certificateSha256, SHA256)) throw new IllegalArgumentException("Invalid Chinese pack identity");
		return "tt9-chinese-" + packageName + "-" + versionCode + "-" + flavor + "-" + buildType + "-"
			+ certificateSha256.toLowerCase(Locale.ROOT) + ".zip";
	}

	/** Validate before activating staged data or requesting Android's split installation. */
	public static void validate(File directory, String packageName, long versionCode, String variant,
		String certificateSha256, String baseSha256) throws IOException {
		if (!matches(packageName, PACKAGE) || versionCode <= 0 || !matches(variant, TOKEN)
			|| !matches(certificateSha256, SHA256) || !matches(baseSha256, SHA256)) {
			throw new IOException("Invalid installed Chinese pack identity");
		}
		Properties metadata = readMetadata(child(directory, "delivery.properties"));
		if (metadata.size() != 8 || !"1".equals(metadata.getProperty("schema"))
			|| !packageName.equals(metadata.getProperty("packageName"))
			|| !Long.toString(versionCode).equals(metadata.getProperty("versionCode"))
			|| !variant.equals(metadata.getProperty("variant"))
			|| !sameHash(certificateSha256, metadata.getProperty("certificateSha256"))
			|| !sameHash(baseSha256, metadata.getProperty("baseSha256"))) {
			throw new IOException("Chinese pack does not match the installed application");
		}
		for (String[] entry : new String[][]{{"chinese.apk", "featureSha256"}, {"chinese-ui.apk", "uiSha256"}}) {
			File file = child(directory, entry[0]);
			if (!file.isFile() || file.length() == 0 || !sameHash(sha256(file), metadata.getProperty(entry[1]))) {
				throw new IOException("Chinese pack file is missing or damaged: " + entry[0]);
			}
		}
	}

	private static Properties readMetadata(File file) throws IOException {
		if (!file.isFile() || file.length() == 0 || file.length() > MAX_METADATA_BYTES) {
			throw new IOException("Missing or oversized Chinese delivery metadata");
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (FileInputStream input = new FileInputStream(file)) {
			byte[] buffer = new byte[1024];
			for (int size; (size = input.read(buffer)) != -1;) {
				if (bytes.size() + size > MAX_METADATA_BYTES) throw new IOException("Chinese delivery metadata is too large");
				bytes.write(buffer, 0, size);
			}
		}
		Properties metadata = new Properties() {
			@Override public synchronized Object put(Object key, Object value) {
				if (containsKey(key)) throw new IllegalArgumentException("Duplicate Chinese delivery property");
				return super.put(key, value);
			}
		};
		try {
			metadata.load(new ByteArrayInputStream(bytes.toByteArray()));
		} catch (IllegalArgumentException exception) {
			throw new IOException("Invalid Chinese delivery metadata", exception);
		}
		return metadata;
	}

	private static File child(File directory, String name) throws IOException {
		if (directory == null || !directory.isDirectory()) throw new IOException("Missing Chinese pack directory");
		File file = new File(directory, name).getCanonicalFile();
		if (!directory.getCanonicalFile().equals(file.getParentFile())) throw new IOException("Invalid Chinese pack file location");
		return file;
	}

	private static boolean matches(String value, String pattern) { return value != null && value.matches(pattern); }
	private static boolean sameHash(String expected, String actual) {
		return matches(actual, SHA256) && expected.equalsIgnoreCase(actual);
	}

	public static String sha256(File file) throws IOException {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			try (FileInputStream input = new FileInputStream(file)) {
				byte[] buffer = new byte[16384];
				for (int size; (size = input.read(buffer)) != -1;) digest.update(buffer, 0, size);
			}
			char[] hex = "0123456789abcdef".toCharArray();
			StringBuilder result = new StringBuilder(64);
			for (byte value : digest.digest()) result.append(hex[(value & 255) >>> 4]).append(hex[value & 15]);
			return result.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IOException("SHA-256 is unavailable", exception);
		}
	}
}
