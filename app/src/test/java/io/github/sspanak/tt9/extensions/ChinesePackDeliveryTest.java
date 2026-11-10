package io.github.sspanak.tt9.extensions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ChinesePackDeliveryTest {
	@Rule public TemporaryFolder temporary = new TemporaryFolder();
	private static final String PACKAGE = "io.github.sspanak.tt9";
	private static final String CERT = "ab".repeat(32);
	private static final String BASE = "12".repeat(32);

	@Test public void assetIdentitySeparatesFlavorBuildAndSigner() {
		String name = ChinesePackDelivery.assetName(PACKAGE, 1660, "lite", "release", CERT.toUpperCase());
		assertEquals("tt9-chinese-io.github.sspanak.tt9-1660-lite-release-" + CERT + ".zip", name);
		assertNotEquals(name, ChinesePackDelivery.assetName(PACKAGE, 1660, "full", "release", CERT));
		assertNotEquals(name, ChinesePackDelivery.assetName(PACKAGE, 1660, "lite", "debug", CERT));
		assertNotEquals(name, ChinesePackDelivery.assetName(PACKAGE, 1660, "lite", "release", "cd".repeat(32)));
	}

	@Test public void assetNamesRejectPathsMalformedIdentitiesAndHashes() {
		for (String pkg : new String[]{null, "", "tt9", "io..tt9", "io.9tt9", "../io.tt9", "io.tt9/a", "io.tt9?x", "io.tt9%2f"}) {
			assertThrows(pkg, IllegalArgumentException.class, () -> ChinesePackDelivery.assetName(pkg, 1660, "lite", "release", CERT));
		}
		for (String token : new String[]{"", "../lite", "a/b", "a b", "a?b", "a%2fb"}) {
			assertThrows(IllegalArgumentException.class, () -> ChinesePackDelivery.assetName(PACKAGE, 1660, token, "release", CERT));
			assertThrows(IllegalArgumentException.class, () -> ChinesePackDelivery.assetName(PACKAGE, 1660, "lite", token, CERT));
		}
		assertThrows(IllegalArgumentException.class, () -> ChinesePackDelivery.assetName(PACKAGE, 0, "lite", "release", CERT));
		for (String hash : new String[]{null, "", "ab".repeat(31), "gg".repeat(32), CERT + "\n"}) {
			assertThrows(IllegalArgumentException.class, () -> ChinesePackDelivery.assetName(PACKAGE, 1660, "lite", "release", hash));
		}
	}

	@Test public void completeMatchingDeliveryValidatesWithoutModifyingFiles() throws IOException {
		File pack = pack();
		byte[] before = Files.readAllBytes(new File(pack, "delivery.properties").toPath());
		validate(pack);
		assertArrayEquals(before, Files.readAllBytes(new File(pack, "delivery.properties").toPath()));
		assertEquals(3, pack.list().length);
	}

	@Test public void mismatchedApplicationVersionVariantSignerAndBaseAreRejected() throws IOException {
		for (Map.Entry<String, String> mismatch : Map.of("packageName", "other.application", "versionCode", "1661",
			"variant", "liteDebug", "certificateSha256", "cd".repeat(32), "baseSha256", "34".repeat(32)).entrySet()) {
			File pack = pack();
			Map<String, String> metadata = metadata(pack);
			metadata.put(mismatch.getKey(), mismatch.getValue());
			writeMetadata(pack, metadata);
			assertThrows(mismatch.getKey(), IOException.class, () -> validate(pack));
		}
	}

	@Test public void independentlyTruncatedAlteredMissingAndEmptySplitsAreRejected() throws IOException {
		for (String name : new String[]{"chinese.apk", "chinese-ui.apk"}) {
			for (byte[] replacement : new byte[][]{new byte[0], "truncated".getBytes(StandardCharsets.UTF_8), "altered payload".getBytes(StandardCharsets.UTF_8)}) {
				File pack = pack();
				Files.write(new File(pack, name).toPath(), replacement);
				assertThrows(name, IOException.class, () -> validate(pack));
			}
			File pack = pack();
			Files.delete(new File(pack, name).toPath());
			assertThrows(name, IOException.class, () -> validate(pack));
		}
	}

	@Test public void metadataRequiresEveryFieldAndValidSchemaAndHashes() throws IOException {
		File fixture = pack();
		for (String key : metadata(fixture).keySet()) {
			File pack = pack();
			Map<String, String> metadata = metadata(pack);
			metadata.remove(key);
			writeMetadata(pack, metadata);
			assertThrows(key, IOException.class, () -> validate(pack));
		}
		for (Map.Entry<String, String> invalid : Map.of("schema", "2", "featureSha256", "gg".repeat(32), "uiSha256", "1".repeat(63)).entrySet()) {
			File pack = pack();
			Map<String, String> metadata = metadata(pack);
			metadata.put(invalid.getKey(), invalid.getValue());
			writeMetadata(pack, metadata);
			assertThrows(invalid.getKey(), IOException.class, () -> validate(pack));
		}
	}

	@Test public void malformedDuplicateOversizedAndMissingMetadataFailClosed() throws IOException {
		for (String content : new String[]{"", "schema=1\n", "bad=\\uZZZZ", "#".repeat(8193)}) {
			File pack = pack();
			Files.writeString(new File(pack, "delivery.properties").toPath(), content);
			assertThrows(IOException.class, () -> validate(pack));
		}
		File duplicate = pack();
		Files.writeString(new File(duplicate, "delivery.properties").toPath(), "\nschema=1", java.nio.file.StandardOpenOption.APPEND);
		assertThrows(IOException.class, () -> validate(duplicate));
		File missing = pack();
		Files.delete(new File(missing, "delivery.properties").toPath());
		assertThrows(IOException.class, () -> validate(missing));
	}

	@Test public void hashMatchesKnownSha256AndMissingFilesFail() throws IOException {
		File file = temporary.newFile();
		Files.writeString(file.toPath(), "abc");
		assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", ChinesePackDelivery.sha256(file));
		assertThrows(IOException.class, () -> ChinesePackDelivery.sha256(new File(temporary.getRoot(), "missing")));
	}

	private File pack() throws IOException {
		File directory = temporary.newFolder();
		Files.writeString(new File(directory, "chinese.apk").toPath(), "feature payload bytes");
		Files.writeString(new File(directory, "chinese-ui.apk").toPath(), "translation payload bytes");
		writeMetadata(directory, metadata(directory));
		return directory;
	}

	private Map<String, String> metadata(File directory) throws IOException {
		Map<String, String> values = new LinkedHashMap<>();
		values.put("schema", "1");
		values.put("packageName", PACKAGE);
		values.put("versionCode", "1660");
		values.put("variant", "fullDebug");
		values.put("certificateSha256", CERT);
		values.put("baseSha256", BASE);
		values.put("featureSha256", ChinesePackDelivery.sha256(new File(directory, "chinese.apk")));
		values.put("uiSha256", ChinesePackDelivery.sha256(new File(directory, "chinese-ui.apk")));
		return values;
	}

	private void writeMetadata(File directory, Map<String, String> values) throws IOException {
		StringBuilder text = new StringBuilder();
		values.forEach((key, value) -> text.append(key).append('=').append(value).append('\n'));
		Files.writeString(new File(directory, "delivery.properties").toPath(), text);
	}

	private void validate(File pack) throws IOException { ChinesePackDelivery.validate(pack, PACKAGE, 1660, "fullDebug", CERT, BASE); }
}
