# Traditional T9 Installation Guide

## Phones which allow installing APKs

### From F-Droid
The recommended way of installing is using F-droid. It will automatically manage downloads and keep TT9 up-to-date. The downside is new versions may become available as late as two weeks after the official release.

If you have F-droid, just search for "Traditional T9" and install it. If not, get the official F-droid APK from https://f-droid.org/ and follow the [installation instructions](https://f-droid.org/en/docs/Get_F-Droid/).

### From Github
If you would like to run the latest version as soon as it is released, go to the Github [Releases section](https://github.com/sspanak/tt9/releases). Then either download the APK on your phone, or download it on a computer and transfer it to the phone. After that, just click or tap to install. If custom APK installation is enabled, it will just work. If not, your phone may show a popup, asking you to grant the installation permission, before proceeding.

In some cases, installation permissions are disabled by default, but you can usually enable them by going to: Android Settings → Apps → Settings → Security, then enable "Installation from unknown sources".

_The setting name or location may be different on different Android versions. Nevertheless, a quick Google search for your phone make and model, should point you in the right direction._

## Phones which do not allow installing APKs or do not display TT9 after installation
Some manufacturers prefer to lock their device and disallow installing or enabling (or both) third party keyboards or apps in general. It is likely to reduce the complaints "my phone isn't working", caused by the custom installed apps. The problem has been discussed [here](https://github.com/sspanak/tt9/issues/455) and [here](https://github.com/sspanak/tt9/issues/198).

An examples of such phones are all models by Sonim, especially since the November 2022 update and some Kyocera models, for example: Kyocera DuraXV Extreme+.

### Prepare Your Computer
To install TT9 on such locked devices, you will have to connect your phone to a computer and use a program called "adb" (Android Debug Bridge). If you feel confident using the command-line, follow the instructions below.

Go through the [adb quick setup](https://www.xda-developers.com/install-adb-windows-macos-linux/). If you need more detailed info, see the [developer documentation](https://developer.android.com/tools/adb).

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