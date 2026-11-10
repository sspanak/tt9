import java.util.zip.ZipFile

// Run with the Gradle distribution's lib/* classpath and groovy.ui.GroovyMain.
// The production parser, sorter and ZIP writer must preserve every exact reading.
File repo = new File(args[0])
File output = new File(args[1])
output.mkdirs()
GroovyClassLoader loader = new GroovyClassLoader()
loader.parseClass(new File(repo, 'app/dictionary-tools.gradle'))
Class tools = loader.loadClass('Wrapper')
Class converter = loader.parseClass('import org.gradle.api.GradleException\n'
    + new File(repo, 'app/build-dictionaries.gradle').getText('UTF-8'))

Map<String, String> sounds = [:]
new File(repo, 'app/languages/definitions/ChineseSimplifiedPinyin.yml').eachLine('UTF-8') { line ->
    def match = line =~ /\[([A-Z]),\s*(\d+)\]/
    if (match.find()) sounds[match.group(1)] = match.group(2)
}
Set<String> expected = new LinkedHashSet<>()
new File(repo, 'app/languages/chinese/polyphonic-readings.tsv').eachLine('UTF-8') { line ->
    if (!line.startsWith('#')) {
        String[] fields = line.split('\t')
        assert expected.add(fields[1] + fields[0]): 'Duplicate reviewed reading'
    }
}
assert expected.size() == 772
def groups = new LinkedHashMap<String, ArrayList<String>>()
new File(repo, 'app/languages/dictionaries/zh-simplified-pinyin.csv').eachLine('UTF-8') { line ->
    def (word, reading, frequency) = tools.getDictionaryLineData(line, '\t')
    if (expected.contains(reading + word)) {
        String digits = tools.wordToDigitSequence(new Locale('zh', 'CN'), reading, sounds, true)
        groups.computeIfAbsent(digits, { new ArrayList<String>() })
            .add(String.format('%04d', frequency) + reading + word)
    }
}
def sorted = converter.sortDictionary(groups)
File dictionary = new File(repo, 'app/languages/dictionaries/zh-simplified-pinyin.csv')
def (error, archive) = converter.writeZippedDictionary(output.path, dictionary, sorted, 'zip', false,
    tools.getDictionaryNotices(dictionary))
assert error == '': error
new ZipFile(archive).withCloseable { zip ->
    String payload = zip.getInputStream(zip.getEntry('zh-simplified-pinyin.txt')).getText('UTF-8')
    List<String> restored = []
    (payload =~ /[2-9]+([^2-9]*)/).each { match ->
        restored.addAll(match[1].trim().split(/\s+/).toList())
    }
    assert restored.size() == expected.size(): 'A reading was lost or repeated during conversion'
    assert restored.toSet() == expected: 'The converted archive differs from the reviewed reading pairs'
    for (String notice : ['licenses/rime-ice-license.txt', 'licenses/rime-ice-notice.txt']) {
        assert zip.getEntry(notice) != null
    }
}
println 'Production dictionary parser/sorter/ZIP writer preserved all 772 reviewed readings and license notices.'
