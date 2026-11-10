// Uses the actual build-time helper, with no Gradle build or Android dependencies.
if (!binding.hasVariable('deliveryTools')) {
	def loader = new GroovyClassLoader()
	loader.parseClass(new File('app/chinese-delivery-tools.gradle'))
	binding.setVariable('deliveryTools', loader.loadClass('ChineseDeliveryTools'))
}
String certificate = 'ab' * 32
String certificateOutput = "Verifies\nSigner #1 certificate SHA-256 digest: ${certificate.toUpperCase()}\n"
assert deliveryTools.certificateSha256(certificateOutput) == certificate
Map base = deliveryTools.apkIdentity("package: name='io.github.sspanak.tt9' versionCode='1660' versionName='65.0'\n")
Map feature = deliveryTools.apkIdentity("package: name='io.github.sspanak.tt9' versionCode='1660' versionName='' split='chinese'\n")
Map ui = deliveryTools.apkIdentity("package: name='io.github.sspanak.tt9' versionCode='1660' versionName='' split='config.zh_rCN'\n")
List<Map> matching = [base, feature, ui].collect { it + [certificateSha256: certificate] }
deliveryTools.requireMatching(matching, 'io.github.sspanak.tt9', '1660')
int rejected = 0
Closure reject = { Closure operation ->
	try { operation(); throw new AssertionError('Invalid delivery accepted') }
	catch (IllegalArgumentException expected) { rejected++ }
}
reject { deliveryTools.apkIdentity('not an APK') }
reject { deliveryTools.certificateSha256('unsigned') }
reject { deliveryTools.certificateSha256(certificateOutput + "Signer #2 certificate SHA-256 digest: ${'cd' * 32}\n") }
reject { deliveryTools.requireMatching([matching[0], matching[1] + [packageName: 'other.app'], matching[2]], 'io.github.sspanak.tt9', '1660') }
reject { deliveryTools.requireMatching([matching[0], matching[1], matching[2] + [versionCode: '1659']], 'io.github.sspanak.tt9', '1660') }
reject { deliveryTools.requireMatching([matching[0], matching[1] + [certificateSha256: 'cd' * 32], matching[2]], 'io.github.sspanak.tt9', '1660') }
reject { deliveryTools.requireMatching([matching[0], matching[1], matching[2] + [split: 'wrong']], 'io.github.sspanak.tt9', '1660') }
reject { deliveryTools.assetName('../unsafe', '1660', 'full', 'release', certificate) }
assert rejected == 8
assert deliveryTools.assetName('io.github.sspanak.tt9', '1660', 'full', 'release', certificate) ==
	"tt9-chinese-io.github.sspanak.tt9-1660-full-release-${certificate}.zip"
File directory = java.nio.file.Files.createTempDirectory('tt9-delivery-unit').toFile()
List<File> files = ['base', 'feature', 'ui'].collect { new File(directory, it) }
try {
	files.each { it.text = it.name }
	String properties = deliveryTools.properties('io.github.sspanak.tt9', '1660', 'fullRelease', certificate, *files)
	Properties parsed = new Properties()
	parsed.load(new StringReader(properties))
	assert parsed.size() == 8
	assert parsed.schema == '1' && parsed.variant == 'fullRelease'
	assert parsed.baseSha256 == deliveryTools.sha256(files[0])
	assert parsed.featureSha256 == deliveryTools.sha256(files[1])
	assert parsed.uiSha256 == deliveryTools.sha256(files[2])
	assert properties == deliveryTools.properties('io.github.sspanak.tt9', '1660', 'fullRelease', certificate, *files)
} finally {
	files.each { it.delete() }
	directory.delete()
}
println 'Chinese delivery helper checks passed (identity/signature rejection, deterministic filename, exact metadata and hashes).'
