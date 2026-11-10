# Optional Chinese domain vocabulary / 中文专业词库

The Chinese extension includes seven individually selectable starter vocabularies.
They support specialist term entry while keeping the domain engine, settings and
data in the optional Chinese pack. No network request or automatic corpus update
is needed while typing.

中文扩展提供七类可独立开关的入门专业词库，用于辅助输入领域词语。领域查询、设置和数据均随可选中文包提供，打字时不联网，也不自动更新词库。

| Domain / 领域 | ID | Category rows / 类别记录数 | Source / 来源 |
| --- | --- | ---: | --- |
| Medicine / 医学 | `medical` | 1,000 | THUOCL + existing factory readings / 现有基础词库读音 |
| Finance / 金融 | `finance` | 800 | THUOCL + existing factory readings / 现有基础词库读音 |
| Computing and technology / 计算机与技术 | `technology` | 800 | THUOCL + existing factory readings / 现有基础词库读音 |
| Games and ACG / 游戏与 ACG | `acg` | 100 | Original TT9 curation / TT9 原创整理 |
| Psychology / 心理学 | `psychology` | 90 | Original TT9 curation / TT9 原创整理 |
| Biochemistry / 生物化学 | `biochemistry` | 94 | Original TT9 curation / TT9 原创整理 |
| Engineering / 工程技术 | `engineering` | 96 | Original TT9 curation / TT9 原创整理 |

There are **2,980 category rows and 2,961 merged word/reading entries**. The current
binary index is **126,839 bytes**. Cross-domain duplicates share one record and a
membership mask. The four original vocabularies add **103 word/reading pairs**
absent from the factory dictionary; the 2,600 THUOCL rows reuse existing weighted
factory readings and provide optional domain prioritization. These counts describe
the checked-in starter data; specialist coverage remains limited.

合计 **2,980 条类别记录、2,961 组去重后的词语/读音**，当前二进制索引为 **126,839 字节**。
同一词语和读音跨类别出现时共用记录，并保留所属类别。四类原创词表新增了基础词库中没有的 **103 组词语/读音**；THUOCL 的 2,600 条记录复用已有带权重读音，作用是按需提高领域词的候选优先级。以上数量对应当前提交的入门词表，专业覆盖范围有限。

## English

### Activation, ranking and lifetime

Open **Languages → Chinese Input → Domain vocabulary**. All domains are off
by default. Enabling a domain contributes its matching terms to Chinese candidate
queries. The leading base candidate, frequent common exact single characters and
explicit personal words retain priority; enabled domain matches can then move
ahead of other exact candidates. Domain weights and separate domain learning
order these contributions.

Switching a domain off restores the normal base ordering and removes words
provided solely by that domain. A word remains available if the factory dictionary,
a personal word or another enabled domain supplies it. The switches do not
individually uninstall files: all seven catalogues are stored together in
`chinese-domains.bin` inside the Chinese pack.

Domain learning follows the existing top-word learning switch and uses separate
`pref_chinese_` preferences, capped at 512 learned word/readings. It does not insert domain records into `custom_words` or overwrite
factory word positions/frequencies. Domain activation and learning are cleared
when the Chinese profile/private pack is removed; the private index is removed
with the pack. This domain cleanup leaves ordinary personal words/readings and
factory learning alone. Removing or reloading the underlying factory dictionary
continues to follow the ordinary dictionary lifecycle described in the
[extension notes](chinese-extension.md).

### Sources, pronunciation and review

The authoritative files are the seven TSVs and
[`source.json`](../app/languages/chinese/domains/source.json). Each metadata entry
contains a stable ID, English/Chinese labels, license and provenance. The domain
catalog shows names and counts; **About these vocabularies and sources** opens
the full explanation, licenses and provenance in a scrollable dialog, keeping long
URLs out of the toggle list.

Medicine, finance and technology use
[THUOCL](https://github.com/thunlp/THUOCL/tree/a30ce79d895d01ab5132a5c74c29703ff7efb4cc),
pinned to commit `a30ce79d895d01ab5132a5c74c29703ff7efb4cc`. THUOCL supplies Chinese
terms and document frequency, without Pinyin. The selected terms therefore use
only positive-weight word/reading pairs already in TT9's factory dictionary.
The actual `PinyinSyllables.segment` implementation must find exactly one split
with one syllable per Han character. Ambiguous or unsegmentable readings are
skipped rather than inferred character by character. All qualifying readings of
a chosen word are retained; each word in these three subsets currently has one.
This policy reuses known factory readings and does not independently certify
every pronunciation.

Every retained term string was reviewed for obvious topic noise, unrelated
people/places, advertising/contact content and malformed fragments. Examples of
excluded noise include unrelated public figures in the medical list, media titles
and stale unrelated stock names in the finance list, and the fragment `万年薪`.
Legitimate reproductive/medical and financial-crime vocabulary remains available.
The data contain terms and readings, with no copied definitions or advice.

THUOCL's files date from 2016/2017. Its IT DF comes from 2014–2016 CSDN documents;
finance and medicine use 2008–2016 Sina News documents. Those values guide ranking
within a selected domain and are not current popularity measurements. Selected
weights use `round(9999 × log1p(DF) / log1p(maximum selected domain DF))`.
The four original vocabularies use curated relative weights. Weights across
domains are independent ranking hints, not a universal corpus frequency scale.
For a duplicate word/reading, the compiled record keeps the greatest source
weight and all domain memberships.

THUOCL's source terms/DF are MIT-licensed; the exact copyright and permission text
is in [`THUOCL-LICENSE.txt`](../app/languages/chinese/domains/THUOCL-LICENSE.txt).
The selected data accompany the Chinese data under GPL-3.0-only, retaining the
MIT attribution. The four original vocabularies are project-authored
GPL-3.0-only data. Source pins, hashes, changes and the selection policy are in
[`THUOCL-NOTICE.txt`](../app/languages/chinese/domains/THUOCL-NOTICE.txt); existing
factory provenance remains in
[`RIME-ICE-NOTICE.txt`](../app/languages/chinese/RIME-ICE-NOTICE.txt). Application
code retains its existing Apache-2.0 license. The packaged domain notice and
THUOCL license accompany the combined ZIP, alongside the existing GPLv3 text.

### Maintaining the vocabulary

1. Edit `app/languages/chinese/domains/<id>.tsv` as UTF-8. Each row is
   `word<TAB>explicit reading<TAB>weight`. Use 1–32 Han characters, one accepted
   uppercase Pinyin syllable per character separated by apostrophes or spaces,
   and an integer weight from 0 to 9999. The THUOCL subsets use the stricter
   2–12-character selection policy above. Preserve known alternative readings;
   resolve new polyphonic readings explicitly.
2. Keep published domain IDs stable. Update `source.json` labels, provenance,
   license and data counts/hashes when changing sources or selections. A new
   category requires a matching `<id>.tsv` and metadata entry. Record the source
   commit or snapshot hash, transformations and required notices. Avoid importing
   an entire third-party corpus merely to increase the entry count.
3. Run `./gradlew :app:buildChineseDomains` (`gradlew.bat` on Windows). The builder
   checks fields, UTF-8, Han characters, explicit syllable counts, supported Pinyin,
   weights, duplicate pairs, metadata and size bounds. Duplicate pairs within a
   category fail validation; cross-domain pairs merge by exact word and explicit
   reading, combining memberships and taking the greatest weight.
4. Inspect `app/build/generated/chinese-domains/chinese-domains.bin` and
   `chinese-domains-notice.txt`, then build the matching signed Chinese pack using
   the [extension build instructions](chinese-extension.md). Verify per-domain
   on/off behavior, exact reading selection, ordinary single-character/personal
   priority and unchanged base order when all domains are off. Keep the source
   TSVs and notices with the release source distribution.

The format is bounded to 30 domains, 50,000 merged entries and a 4 MiB index.
Lookup uses the sorted digit-sequence index with bounded result/prefix scans;
it does not scan every TSV while typing. Per-domain download/uninstall and
automatic corpus refresh are future scope. Coverage is limited to the selected entries.

## 中文

### 开关、排序与数据生命周期

在 **语言 → 中文输入 → 领域词包** 中选择类别，所有类别默认关闭。开启后，对应领域词参与中文候选查询。基础首选、常用的精确单字和明确保存的个人词保留优先级，随后可以优先显示匹配的领域词；这些领域候选再按词库权重和独立的领域学习记录排序。

关闭类别会恢复普通基础排序，并移除仅由该类别提供的词。基础词库、个人词或其他已开启类别中也有的词仍可出现。开关不会单独卸载某一类文件，七类数据共同存放在中文包的 `chinese-domains.bin` 中。

领域学习遵从已有首选词学习开关，单独存入 `pref_chinese_` 设置，最多保留 512 组词语/读音；不会把领域词写进 `custom_words`，也不覆盖基础词的位置或词频。移除中文配置/私有包时会清除领域开关和领域学习，并随包删除私有索引。此领域清理不触碰普通个人词/读音和基础词频学习；底层基础字典的删除、重载仍遵循原有字典流程，详见[扩展说明](chinese-extension.md)。

### 来源、读音与审查

七份 TSV 与 [`source.json`](../app/languages/chinese/domains/source.json) 是可编辑源数据。元数据包含稳定 ID、中英文名称、许可证和来源。开关列表仅显示名称和词数；“词包说明与来源”提供可滚动的完整说明、许可和来源，避免长网址撑大列表。

医学、金融、计算机与技术来源于 [THUOCL](https://github.com/thunlp/THUOCL/tree/a30ce79d895d01ab5132a5c74c29703ff7efb4cc)，固定提交为 `a30ce79d895d01ab5132a5c74c29703ff7efb4cc`。THUOCL 提供汉语词语及文档频次，没有拼音，因此仅选择 TT9 基础词库中已有且带正权重的词语/读音。使用实际 `PinyinSyllables.segment` 检查汉字数对应的音节拆分，只有唯一合法结果才采用；有歧义或无法拆分的读音直接跳过，不按单字默认音猜读。所选词的全部合格读音均保留，目前这三份子集每词只有一个读音。这是复用已有读音的策略，并不表示逐条进行了独立的语音学认证。

保留的词语文本均检查了明显的领域噪声、无关人物/地名、广告联系方式和错误片段，例如医学表中的无关公众人物、金融表中的媒体标题和过时的无关股票名，以及 `万年薪` 这类片段。正常的生殖医学和金融犯罪术语保留。数据只包含词语及读音，不复制定义或提供专业建议。

THUOCL 源文件更新于 2016/2017 年，IT 文档频次来自 2014–2016 年 CSDN，金融和医学来自 2008–2016 年新浪新闻。这里用这些频次辅助领域内部排序，不将其当成当前热度。所选词权重按 `round(9999 × log1p(DF) / log1p(该领域所选最大 DF))` 计算；四份原创词表使用人工整理的相对权重。各领域权重是独立排序提示，不是统一语料词频。跨类别重复的词语/读音保留全部所属类别，并取来源中的最大权重。

THUOCL 的词语/文档频次来源采用 MIT，完整版权与许可文本见 [`THUOCL-LICENSE.txt`](../app/languages/chinese/domains/THUOCL-LICENSE.txt)。所选数据随中文数据以 GPL-3.0-only 分发并保留 MIT 署名，四份原创词表也采用项目原创数据的 GPL-3.0-only。固定来源、哈希、修改和筛选规则见 [`THUOCL-NOTICE.txt`](../app/languages/chinese/domains/THUOCL-NOTICE.txt)；基础词库来源继续由 [`RIME-ICE-NOTICE.txt`](../app/languages/chinese/RIME-ICE-NOTICE.txt) 记录。应用代码维持原有 Apache-2.0。组合 ZIP 附带领域声明及 THUOCL 许可证，并沿用已有完整 GPLv3 文本。

### 维护方法

1. 以 UTF-8 编辑 `app/languages/chinese/domains/<id>.tsv`，每行为 `词语<TAB>明确拼音<TAB>权重`。支持 1–32 个汉字，每字一个合法大写拼音音节，以撇号或空格分隔，权重为 0–9999 的整数；THUOCL 子集另按上述规则限制为 2–12 字。保留已知多读音，新增多音词时明确核对读法。
2. 已发布的类别 ID 保持稳定。修改来源或筛选结果时更新 `source.json` 的名称、来源、许可证及数据数量/哈希；新增类别同时增加同名 TSV 和元数据。记录来源提交或快照哈希、转换过程及所需声明，按实际输入需求收词。
3. 执行 `./gradlew :app:buildChineseDomains`，Windows 使用 `gradlew.bat`。构建器检查字段、UTF-8、汉字、明确音节数、支持的拼音、权重、重复词语/读音、元数据及体积上限。同类别重复记录会报错；跨类别按词语及明确读音去重，合并所属类别并取最大权重。
4. 检查 `app/build/generated/chinese-domains/` 中的 `chinese-domains.bin` 和 `chinese-domains-notice.txt`，再按[扩展构建说明](chinese-extension.md) 生成匹配签名的中文包。验证各类开关、准确读音选择、常用单字/个人词优先，以及全部关闭后的基础排序；发布源码保留 TSV 与许可证声明。

格式上限为 30 类、50,000 组去重记录及 4 MiB 索引。查询使用按数字编码排序的索引，并限制结果数和前缀扫描范围，打字时不遍历所有 TSV。按类别单独下载/卸载及自动语料更新留作后续范围，实际覆盖以所选条目为准。
