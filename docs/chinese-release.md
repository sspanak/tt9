# Publishing the Chinese language pack / 发布中文语言包

The combined Chinese ZIP contains the ordinary dictionary first, optional data,
the signed Chinese feature APK and the signed Chinese UI split. Its default local
filename remains `zh-simplified-pinyin.zip`. Packaging also writes an identical
release copy named:

```text
tt9-chinese-<applicationId>-<versionCode>-<flavor>-<buildType>-<certificateSha256>.zip
```

The package task verifies all three APK signatures and their package/version
identity before producing the ZIP. `chinese/delivery.properties` contains exactly
eight UTF-8 `key=value` fields: `schema=1`, `packageName`, `versionCode`, `variant`,
`certificateSha256`, `baseSha256`, `featureSha256`, and `uiSha256`. Hashes use
lowercase SHA-256 hex; `variant` is, for example, `liteRelease` or `fullDebug`.
The base hash binds the feature/UI resources to that exact base APK build. Do not
rebuild a base independently and pair it with an older ZIP, even under the same
version number.

The application derives the release asset name from its own identity and signing
certificate. `-PchinesePackBaseUrl=...` sets its download directory; the default is
`https://github.com/sspanak/tt9/releases/download/v<versionName>`. Fork releases
must build with their own repository/tag URL. No user setting, mutable “latest”
manifest or replacement of the ordinary dictionary URL is needed. Keep already
published versioned assets unchanged. Android still requires installation consent
and verifies the installed package/version/signature.

## Maintainer workflow

1. Configure the repository's `release` environment, preferably with required
   reviewers and deployment restrictions for trusted release refs. Add
   `TT9_RELEASE_KEYSTORE_BASE64`, `TT9_RELEASE_STORE_PASSWORD`,
   `TT9_RELEASE_KEY_ALIAS`, and optionally `TT9_RELEASE_KEY_PASSWORD` as environment
   secrets. Use the existing application signing key. An unrelated key cannot
   update installed upstream applications; a fork needs its own consistent key
   and download URL.
2. Push a version tag following the existing TT9 `v...` convention. Run **Prepare
   signed Chinese release** on that same commit and enter the existing tag.
   The workflow fetches full Git history/tags because TT9 derives its version
   code/name from them. It rejects a tag pointing to a different commit.
3. Leave **publish** unchecked for a dry run. The protected signing job runs Full
   and Lite tests/lint, builds matching signed delivery sets, and uploads
   `signed-chinese-release` for inspection. Dry runs still require the signing
   secrets; ordinary pull-request CI uses an explicitly selected debug key and
   cannot publish or access release secrets.
4. Before publication, review the base APKs, matching named ZIPs,
   `chinese-delivery.json`, `SHA256SUMS`, source archive and complete notices
   archive. Install the matching APKs and exercise the local-ZIP import path;
   public URLs for unpublished release assets are not available yet.
5. Run again with **publish** selected when ready. Only the separate publishing
   job has `contents: write`. It rechecks the tag and the complete checksum
   inventory: every regular asset must be listed exactly once, extra files are
   rejected, and `SHA256SUMS` is not self-listed. Existing release
   descriptions are preserved; any asset-name collision fails instead of
   replacing an asset. A new release is created only for an already existing
   verified tag. Publication is never triggered by a pull request.
6. After publication, the workflow downloads both named Chinese ZIPs over
   anonymous HTTPS with retries and compares their SHA-256 against the verified
   local copies. Then verify the ordinary in-app Chinese download using the
   published matching base APK. A failed public-download check leaves published
   files intact; investigate it without replacing a versioned asset.

In-app delivery requires a **public repository with anonymously accessible release
assets**. Private/internal repositories can prepare review artifacts, but the
publish job rejects them because the application does not send GitHub credentials.

The workflow writes the decoded keystore only under the ephemeral runner's
temporary directory and removes it in an always-run cleanup step. No signing
key, password or device backup is a release asset. `app/release-signing.gradle`
is an explicit Gradle init script, not an automatically applied project file:

```sh
TT9_SIGNING_MODE=release ./gradlew -I app/release-signing.gradle \
  -PchinesePackBaseUrl=https://github.com/OWNER/REPO/releases/download/TAG \
  :app:packageLiteReleaseChinesePack :app:packageFullReleaseChinesePack
python3 tools/chinese_release.py --tag TAG --output app/build/outputs/chinese-release
```

For local packaging validation only, `TT9_SIGNING_MODE=debug` uses the normal
Android debug signing configuration. Missing release secrets never silently
fall back to that key. To run the pure build-helper checks without Android builds,
load `app/chinese-delivery-tools.gradle` with `GroovyClassLoader` and evaluate
`tools/test_chinese_delivery.groovy` with the class bound as `deliveryTools`.
Python archive/inventory tests run with
`python3 -m unittest discover -s tools -p test_chinese_release.py -v`.

## 中文说明

中文组合 ZIP 将普通词库放在第一项，并携带中文功能 APK、界面翻译 APK、索引和完整授权说明。
本地导入文件名保留 `zh-simplified-pinyin.zip`；发布时另生成包含应用包名、版本号、构建类型和
签名证书哈希的确定文件名。打包前会验证三个 APK 的签名、包名、版本及分包身份；包内八项
`delivery.properties` 还记录主 APK、功能 APK、翻译 APK 的 SHA-256，避免混用不同构建。

维护者在 GitHub 配置受保护的 `release` 环境与现有签名密钥，推送版本标签后，手动运行
**Prepare signed Chinese release**。标签必须与所选工作流提交一致。默认不发布，只生成待检查
产物；普通 PR 检查仅用调试签名，不接触发布密钥。Fork 可配置自己的环境、密钥和下载地址。

发布前检查并测试匹配主 APK 的本地 ZIP 导入，再明确勾选 **publish**；尚未发布的附件还没有
可供普通用户访问的下载地址。工作流会同时提供 Full／Lite
主 APK、对应中文包、来源源码归档、完整授权归档及校验清单；保留已有 Release 说明，遇到同名
附件直接停止，不覆盖发布文件。默认下载地址使用上游仓库和版本名，Fork 必须通过
`chinesePackBaseUrl` 构建参数指定自己的仓库与实际标签。无需改写旧的普通字典下载地址。
发布后，工作流匿名下载两份中文 ZIP 并核对 SHA-256，再使用对应主 APK 验证应用内下载。
此交付方式要求公开仓库和可匿名访问的 Release 附件；私有仓库可准备审查产物，但不会发布。
签名材料与手机私有备份都不会进入发布产物，Android 的安装确认仍保留。
