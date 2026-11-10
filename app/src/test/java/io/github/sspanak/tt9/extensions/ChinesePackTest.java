package io.github.sspanak.tt9.extensions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class ChinesePackTest {
	@Rule public TemporaryFolder temporary = new TemporaryFolder();
	private static final String[] REQUIRED_PAYLOADS = {"common-3500.txt", "zh-simplified-prefixes.bin", "chinese.apk", "chinese-ui.apk"};

	@Test public void removalAlsoClearsInterruptedStagingWithoutTouchingOtherFiles() throws IOException {
		File root = temporary.newFolder();
		String pending = "chinese-pack-pending-01234567-89ab-cdef-0123-456789abcdef";
		for (String name : new String[]{"chinese-pack", pending, pending + "-previous"}) {
			File directory = new File(root, name);
			assertTrue(directory.mkdir());
			Files.writeString(new File(directory, "delivery.properties").toPath(), "metadata");
			Files.writeString(new File(directory, "chinese.apk").toPath(), "code");
		}
		File unrelated = new File(root, "chinese-pack-pending-not-a-uuid");
		assertTrue(unrelated.mkdir());
		File personal = new File(root, "personal-words.txt");
		Files.writeString(personal.toPath(), "keep");
		ChinesePack.removeFiles(root);
		assertFalse(new File(root, "chinese-pack").exists());
		assertFalse(new File(root, pending).exists());
		assertFalse(new File(root, pending + "-previous").exists());
		assertTrue(unrelated.isDirectory());
		assertEquals("keep", Files.readString(personal.toPath()));
		assertEquals(2, root.list().length);
	}

	@Test public void availableDataRequiresBothNonemptyRegularFiles() throws IOException {
		File root = temporary.newFolder();
		assertFalse(ChinesePack.isDataAvailable(root));
		File pack = new File(root, "chinese-pack");
		assertTrue(pack.mkdir());
		File common = new File(pack, "common-3500.txt");
		File prefixes = new File(pack, "zh-simplified-prefixes.bin");
		Files.writeString(common.toPath(), "常用字");
		assertFalse(ChinesePack.isDataAvailable(root));
		Files.write(prefixes.toPath(), new byte[0]);
		assertFalse(ChinesePack.isDataAvailable(root));
		Files.writeString(prefixes.toPath(), "index");
		assertTrue(ChinesePack.isDataAvailable(root));
	}

	@Test public void stagedPackWithAllFourNonemptyPayloadsIsComplete() throws IOException {
		File root = temporary.newFolder();
		try (ZipInputStream zip = archive(completePackEntries()); ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
			assertNotNull(prepared);
			assertTrue(prepared.isComplete());
		}
		assertEquals(0, root.list().length);
	}

	@Test public void missingRequiredPayloadMakesStagedPackIncomplete() throws IOException {
		for (String missing : REQUIRED_PAYLOADS) {
			File root = temporary.newFolder();
			Map<String, String> files = completePackEntries();
			files.remove("chinese/" + missing);
			try (ZipInputStream zip = archive(files); ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
				assertNotNull(prepared);
				assertFalse("Missing file: " + missing, prepared.isComplete());
			}
			assertEquals(0, root.list().length);
		}
	}

	@Test public void emptyRequiredPayloadMakesStagedPackIncomplete() throws IOException {
		for (String empty : REQUIRED_PAYLOADS) {
			File root = temporary.newFolder();
			Map<String, String> files = completePackEntries();
			files.put("chinese/" + empty, "");
			try (ZipInputStream zip = archive(files); ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
				assertNotNull(prepared);
				assertFalse("Empty file: " + empty, prepared.isComplete());
			}
			assertEquals(0, root.list().length);
		}
	}

	@Test public void stagingLeavesTheCurrentPackUntouchedUntilActivation() throws IOException {
		File root = temporary.newFolder();
		File old = oldPack(root);
		try (ZipInputStream zip = archive(Map.of("chinese/new.bin", "new"));
			 ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
			assertNotNull(prepared);
			assertEquals("old", Files.readString(old.toPath()));
			assertFalse(new File(root, "chinese-pack/new.bin").exists());
		}
		assertEquals("old", Files.readString(old.toPath()));
		assertOnlyCurrentPackRemains(root);
	}

	@Test public void confirmedActivationReplacesThePackAndInvalidatesCachesOnce() throws IOException {
		File root = temporary.newFolder();
		File old = oldPack(root);
		long generation = ChinesePack.getGeneration();
		try (ZipInputStream zip = archive(Map.of("chinese/new.bin", "new"));
			 ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
			prepared.activate();
			assertFalse(old.exists());
			assertEquals("new", Files.readString(new File(root, "chinese-pack/new.bin").toPath()));
			prepared.confirm();
			prepared.confirm();
		}
		assertEquals(generation + 1, ChinesePack.getGeneration());
		assertOnlyCurrentPackRemains(root);
	}

	@Test public void unconfirmedActivationRestoresThePreviousPack() throws IOException {
		File root = temporary.newFolder();
		File old = oldPack(root);
		long generation = ChinesePack.getGeneration();
		try (ZipInputStream zip = archive(Map.of("chinese/new.bin", "new"));
			 ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
			prepared.activate();
		}
		assertEquals("old", Files.readString(old.toPath()));
		assertFalse(new File(root, "chinese-pack/new.bin").exists());
		assertEquals(generation, ChinesePack.getGeneration());
		assertOnlyCurrentPackRemains(root);
	}

	@Test public void unconfirmedFirstInstallationLeavesNoPack() throws IOException {
		File root = temporary.newFolder();
		try (ZipInputStream zip = archive(Map.of("chinese/new.bin", "new"));
			 ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
			prepared.activate();
		}
		assertEquals(0, root.list().length);
	}

	@Test public void repeatedCloseDoesNotDeleteTheRestoredPreviousPack() throws IOException {
		File root = temporary.newFolder();
		File old = oldPack(root);
		try (ZipInputStream zip = archive(Map.of("chinese/new.bin", "new"));
			 ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
			prepared.activate();
			prepared.close();
			assertEquals("old", Files.readString(old.toPath()));
		}
		assertEquals("old", Files.readString(old.toPath()));
		assertOnlyCurrentPackRemains(root);
	}

	@Test public void failedActivationRestoresThePreviousPack() throws IOException {
		File root = temporary.newFolder();
		File old = oldPack(root);
		try (ZipInputStream zip = archive(Map.of("chinese/new.bin", "new"));
			 ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
			File[] pending = root.listFiles(file -> file.getName().startsWith("chinese-pack-pending-"));
			assertEquals(1, pending.length);
			assertTrue(pending[0].renameTo(new File(root, "unavailable-staging")));
			assertThrows(IOException.class, prepared::activate);
			assertEquals("old", Files.readString(old.toPath()));
		}
	}

	@Test public void olderDictionaryWithoutExtraEntriesPreservesTheCurrentPack() throws IOException {
		File root = temporary.newFolder();
		File old = oldPack(root);
		try (ZipInputStream zip = archive(Map.of())) {
			assertNull(ChinesePack.stage(root, zip));
		}
		assertEquals("old", Files.readString(old.toPath()));
		assertOnlyCurrentPackRemains(root);
	}

	@Test public void traversalAndNestedPathsCannotEscapeStaging() throws IOException {
		for (String name : new String[]{"chinese/../outside.bin", "chinese/folder/new.bin", "chinese/..\\outside.bin"}) {
			File root = temporary.newFolder();
			File old = oldPack(root);
			try (ZipInputStream zip = archive(Map.of(name, "bad"))) {
				assertThrows(IOException.class, () -> ChinesePack.stage(root, zip));
			}
			assertEquals("old", Files.readString(old.toPath()));
			assertFalse(new File(root, "outside.bin").exists());
			assertOnlyCurrentPackRemains(root);
		}
	}

	@Test public void duplicateFilenamesRejectTheWholeStagedPack() throws IOException {
		File root = temporary.newFolder();
		oldPack(root);
		Map<String, String> entries = new LinkedHashMap<>();
		entries.put("chinese/one.bin", "one");
		entries.put("chinese/two.bin", "two");
		byte[] bytes = zipBytes(entries);
		byte[] from = "chinese/two.bin".getBytes(StandardCharsets.UTF_8);
		byte[] to = "chinese/one.bin".getBytes(StandardCharsets.UTF_8);
		// ZipOutputStream rejects duplicate names itself. Alter the same-length fixture
		// name in its local/central headers to exercise the reader's duplicate check.
		for (int offset = 0; offset <= bytes.length - from.length; offset++) {
			boolean match = true;
			for (int j = 0; j < from.length; j++) if (bytes[offset + j] != from[j]) { match = false; break; }
			if (match) System.arraycopy(to, 0, bytes, offset, to.length);
		}
		try (ZipInputStream zip = afterDictionary(bytes)) {
			assertThrows(IOException.class, () -> ChinesePack.stage(root, zip));
		}
		assertOnlyCurrentPackRemains(root);
	}

	@Test public void excessiveFileCountIsRejectedAndCleanedUp() throws IOException {
		File root = temporary.newFolder();
		oldPack(root);
		Map<String, String> entries = new LinkedHashMap<>();
		for (int i = 0; i < 13; i++) entries.put("chinese/file" + i + ".bin", "data");
		try (ZipInputStream zip = archive(entries)) {
			assertThrows(IOException.class, () -> ChinesePack.stage(root, zip));
		}
		assertOnlyCurrentPackRemains(root);
	}

	@Test public void combinedPackWithDomainDataAndNoticesFitsTheControlledTwelveEntryLimit() throws IOException {
		File root = temporary.newFolder();
		Map<String, String> entries = completePackEntries();
		entries.put("chinese/UNICODE-LICENSE.txt", "Unicode license");
		entries.put("chinese/chinese-domains.bin", "domain index");
		for (String name : new String[]{"rime-ice-license", "rime-ice-notice", "chinese-domains-notice", "thuocl-license", "extra-one", "extra-two"}) {
			entries.put("licenses/" + name + ".txt", "license or source notice");
		}
		assertEquals(12, entries.size());
		try (ZipInputStream zip = archive(entries); ChinesePack.Prepared prepared = ChinesePack.stage(root, zip)) {
			assertTrue(prepared.isComplete());
			prepared.activate();
			prepared.confirm();
		}
		assertEquals("domain index", Files.readString(new File(root, "chinese-pack/chinese-domains.bin").toPath()));
		assertFalse(new File(root, "licenses").exists());
		assertFalse(new File(root, "chinese-pack/thuocl-license.txt").exists());
		assertOnlyCurrentPackRemains(root);
	}

	@Test public void oversizedExpandedContentIsRejectedAndCleanedUp() throws IOException {
		for (String name : new String[]{"chinese/oversized.bin", "ignored.bin"}) {
			File root = temporary.newFolder();
			oldPack(root);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (ZipOutputStream output = new ZipOutputStream(bytes)) {
				output.putNextEntry(new ZipEntry("dictionary.txt"));
				output.closeEntry();
				output.putNextEntry(new ZipEntry(name));
				byte[] chunk = new byte[65536];
				for (int i = 0; i < 1024; i++) output.write(chunk);
				output.write(0);
				output.closeEntry();
			}
			try (ZipInputStream zip = afterDictionary(bytes.toByteArray())) {
				IOException failure = assertThrows(IOException.class, () -> ChinesePack.stage(root, zip));
				assertTrue(failure.getMessage().contains("too large"));
			}
			assertOnlyCurrentPackRemains(root);
		}
	}

	@Test public void ignoredFilesAndDirectoriesAlsoCountTowardTheEntryLimit() throws IOException {
		for (String suffix : new String[]{".bin", "/"}) {
			File root = temporary.newFolder();
			oldPack(root);
			Map<String, String> entries = new LinkedHashMap<>();
			for (int i = 0; i < 13; i++) entries.put("ignored" + i + suffix, "");
			try (ZipInputStream zip = archive(entries)) {
				assertThrows(IOException.class, () -> ChinesePack.stage(root, zip));
			}
			assertOnlyCurrentPackRemains(root);
		}
	}

	private File oldPack(File root) throws IOException {
		File directory = new File(root, "chinese-pack");
		assertTrue(directory.mkdir());
		File old = new File(directory, "old.bin");
		Files.writeString(old.toPath(), "old");
		return old;
	}

	private Map<String, String> completePackEntries() {
		Map<String, String> entries = new LinkedHashMap<>();
		for (String name : REQUIRED_PAYLOADS) entries.put("chinese/" + name, "nonempty");
		return entries;
	}

	private void assertOnlyCurrentPackRemains(File root) {
		assertArrayEquals(new String[]{"chinese-pack"}, root.list());
	}

	private ZipInputStream archive(Map<String, String> entries) throws IOException {
		return afterDictionary(zipBytes(entries));
	}

	private byte[] zipBytes(Map<String, String> entries) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream output = new ZipOutputStream(bytes)) {
			output.putNextEntry(new ZipEntry("dictionary.txt"));
			output.write("factory words".getBytes(StandardCharsets.UTF_8));
			output.closeEntry();
			for (Map.Entry<String, String> entry : entries.entrySet()) {
				output.putNextEntry(new ZipEntry(entry.getKey()));
				output.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
				output.closeEntry();
			}
		}
		return bytes.toByteArray();
	}

	private ZipInputStream afterDictionary(byte[] bytes) throws IOException {
		ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes));
		assertEquals("dictionary.txt", input.getNextEntry().getName());
		byte[] buffer = new byte[256];
		while (input.read(buffer) != -1) {}
		return input;
	}
}
