# Traditional T9 Installation Guide
Traditional T9 has two variants: "lite" and "full". The "lite" version is the standard one, available on all app stores and GitHub, while the "full" version is only on GitHub.

The "lite" version is meant to take minimum storage space and for fast upgrades over the air. The APK is about 2 Mb and contains no language files. It downloads them on demand from GitHub. But excluding that, it uses no Internet at all.

The "full" version has all language files bundled in the APK, making it about 70 Mb. It is a good choice when working offline and for devices where network traffic is expensive or very slow. It is also the recommended choice on low-end or 3G devices, because they may not be able to keep a stable Internet connection under a heavy load.

_The "lite" version is available since release 34.0. Before that, the APK always included all available languages, meaning all older versions were always "full"._

## Devices with Google
On devices with Google Services, the recommended way of installing Traditional T9 is from the Play Store. New releases usually appear one working day after the release on GitHub.

_Google Play Store uses the .aab file format, which is incompatible with the .apk format in all other sources. This means you must first uninstall Traditional T9 if you want to switch to or from the Play Store._

## Devices without Google
If your device does not have Google services but allows installing APKs, you can use an alternative app store or manually download and install the APK from Github. Each variant has pros and cons and it is a matter of a personal preference which one to choose.

### From F-Droid
Just like Play Store, F-droid will automatically manage downloads and keep Traditional T9 up-to-date. The downside is new versions may become available as late as two weeks after the official release on GitHub.

If you don't have F-droid, get the official F-droid APK from https://f-droid.org/ and install it. For more info, refer to the [installation instructions](https://f-droid.org/en/docs/Get_F-Droid/).

After you get F-droid working, just open it, search for "Traditional T9" and install it.

### From Uptodown
Uptodown is another Play Store alternative, very similar to F-droid. What is different is, the new releases appear within 1-2 days after the GitHub release.

If you don't have the Uptodown Store, download and install it from [uptodown.com](https://uptodown-android.en.uptodown.com/android).

### From GitHub
GitHub is the primary release platform. All new releases are published there first. Also, it is the only place where you can find the "full" version. However, it is not an application store, which means you will have to manually check for and install updates. And, it requires a bit more technical skill than the other options.

To obtain TT9 from GitHub, go to the [Releases section](https://github.com/sspanak/tt9/releases). Then either download the APK on your device or download it on a computer and transfer it to the device. After that, just click or tap to install. If custom APK installation is enabled, it will just work. If not, Android may show a popup, asking you to grant the installation permission, before proceeding.

In some cases, installation permissions are disabled by default, but you can usually enable them by going to Android Settings → Apps → Settings → Security, then enabling "Installation from unknown sources".

_The setting name or location may be different on different Android versions. Nevertheless, a quick Google search for your phone make and model, should point you in the right direction._

## Limited or Locked Phones
Some manufacturers prefer to lock their devices and disallow installing or enabling (or both) third-party keyboards or apps in general. It is likely to reduce the complaints "my phone isn't working", caused by the manually installed apps. The problem has been discussed [here](https://github.com/sspanak/tt9/issues/455) and [here](https://github.com/sspanak/tt9/issues/198).

Examples of such phones include some Kyocera models, notably, the DuraXV Extreme+, ZMI Z1, all models by Sonim, and so on. On Sonim XP3800 and XP5800, there is an "Install from Unknown Sources" option, but since the November 2022 update, the phone will still refuse to install APKs unless they have been sent to and approved by Sonim.

On such limited phones, your only option is to download the APK on a computer, then connect the phone to the computer and install using a program called "adb" (Android Debug Bridge). If you feel confident using the command line, follow the instructions below.

### Prepare Your Computer
First, go through the [adb quick setup](https://www.xda-developers.com/install-adb-windows-macos-linux/). If you need more detailed info, see the [developer documentation](https://developer.android.com/tools/adb).


After you are done, download the APK of your choice from the GitHub [Releases section](https://github.com/sspanak/tt9/releases).

### Prepare the Phone
If you have successfully enabled the Developer Mode while setting up ADB, skip this step and proceed to the next one.

On older phones, the Developer Mode cannot be enabled by clicking the build number as described in the ADB setup guide. Instead, it is done by dialing a special code which usually looks like this: `*#*#XXXX#*#*`, where XXXX is some number. Find the code for your phone and dial it. Usually, phones from the same company, running the same Android version, share the same code. For example, the code for Sonim XP3800 also works on XP5800.

_The procedure on your phone may be entirely different from the methods described above, but Google is your friend. It should be fairly easy to find how to do it unless your phone is completely locked._

### Install
If your phone permits installing APKs, skip this step and install normally. Otherwise, install using:

```bash
adb install /path/to/apk/on/your/computer/tt9-vXXX.apk
```

_If you are installing over a previously installed version, you may have to uninstall the old one first. See below._

### Enable

```bash
adb shell ime enable io.github.sspanak.tt9/.ime.TraditionalT9
```

_That's it! TT9 should appear the next you start typing._

### Uninstall

```bash
adb shell pm uninstall io.github.sspanak.tt9
```