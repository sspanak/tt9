# Traditional T9 [![codebeat badge](https://codebeat.co/badges/f7ab222f-4c5d-4b79-b1c8-401eea79c206)](https://codebeat.co/projects/github-com-sspanak-tt9-master) ![GitHub Downloads (all assets, latest release)](https://img.shields.io/github/downloads/sspanak/tt9/latest/total)

Traditional T9 is a 12-key (T9) keyboard for devices with a numeric hardware keypad. It supports predictive text typing in [25+ languages](app/languages/definitions), configurable hotkeys, and an on-screen keypad for touchscreen phones, bringing an old-school Nokia experience to modern Android devices. And best of all, it doesn't spy on you.

This is a modernized version of the [original project](https://github.com/Clam-/TraditionalT9) by Clam-.

## 📷 Screenshots
<table>
    <tr>
        <td rowspan="2"> <img src="screenshots/3.png"> </td>
        <td> <img src="screenshots/1.png"> </td>
        <td> <img src="screenshots/5-1.png"> </td>
        <td rowspan="2"> <img src="screenshots/4.png"> </td>
    </tr>
    <tr>
        <td> <img src="screenshots/2.png"> </td>
        <td> <img src="screenshots/5-2.png"> </td>
    </tr>
</table>

## 📦 Install

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/app/io.github.sspanak.tt9)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
    alt="Get it on IzzyOnDroid"
    height="80">](https://apt.izzysoft.de/fdroid/index/apk/io.github.sspanak.tt9)
[<img src="!RAW/get-it-on-github-badge.png"
    alt="Get it on GitHub"
    height="80">](https://github.com/sspanak/tt9/releases/latest)

If your phone does not allow installing custom APKs, consult the [Installation Guide](docs/installation.md).

## ⚙️ System Requirements
- Android 4.4 or higher.
- A hardware keypad or a keyboard. For touchscreen-only devices, an on-screen keypad can be enabled in the Settings.
- Minimum 70 Mb of storage space. Extra space is needed for language dictionaries in Predictive Mode.
    - Very small languages (< 100k words; Kiswahili, Indonesian): 5-6 Mb per language.
    - Small languages (100k-400k words; e.g. English, Norwegian, Swedish, Finnish, German, French): 15-30 Mb per language.
    - Medium languages (400k-800k words; e.g. Danish, Hebrew, Italian, Greek, Portuguese): 40-75 Mb per language
    - Large languages (800k-1.5M words; e.g. Arabic, Bulgarian, Spanish, Romanian, Ukrainian, Russian): 100-165 Mb per language

_Storage usage depends on the word root count and the average word length in each language. Some languages will require more space, even if they have fewer words than others._

### ⚠️ Compatibility
If you own a phone with Android 2.2 up to 4.4, please refer to the original version of Traditional T9 from 2016.

TT9 may not work well on Kyocera phones, Sonim phones running Android 10+, and some other devices that run highly customized Android versions, where all apps are integrated and intended to work with the respective native keyboard. You may experience missing functionality or unexpected text/numbers appearing when you try to type.

Compatibility has been verified only on the following devices:
- Unihertz Atom L (Android 11)
- Qin F21 Pro+ (Android 11)
- Energizer H620SEU (Android 10)
- Sonim XP3800 (Android 8.1)
- Vodaphone VFD 500 (Android 6.0)

## 🤔 How to Use Traditional T9?
Before using Traditional T9 for the first time you need to configure it and load a dictionary. After that, you can start typing right away in one of the three modes: Predictive, ABC, or Numeric (123). And even if you have mastered the keypad back in the day, you will still find the Predictive mode now offers more powerful and smart new ways of typing with even fewer key presses.

So make sure to read the initial setup and the hotkey tips in the [user manual](docs/user-manual.md). Also, don't miss the convenient [compatibility options](docs/user-manual.md#compatibility-options--troubleshooting) aimed to improve the experience in some applications.

## ⌨ Contributing
As with many other open-source projects, this one is also maintained by its author in his free time. Any help in making Traditional T9 better will be highly appreciated. Here is how:
- Add [a new language](CONTRIBUTING.md#adding-a-new-language), [new UI translations](CONTRIBUTING.md#translating-the-ui) or simply fix a spelling mistake. The process is very simple and even with minimum technical knowledge, your skills as a native speaker will be of great use. Or, if you are not tech-savvy, just [open a new issue](https://github.com/sspanak/tt9/issues) and put the correct translations or words there. Correcting misspelled words or adding new ones is the best you can do to help. Processing millions of words in multiple languages is a very difficult task for a single person.
- Share your list of added words. Use the Export function in Settings → Languages → Added Words and upload the generated CSV file in a [new issue](https://github.com/sspanak/tt9/issues). You are also welcome to [open a PR](https://github.com/sspanak/tt9/pulls) if you have good technical knowledge and can split them by language.
- [Report bugs](https://github.com/sspanak/tt9/issues) or other unusual behavior on different phones. It is only possible to verify correct operation and compatibility on [a handful of phones](#%EF%B8%8F-compatibility), but Android behavior and appearance vary a lot across the millions of devices available out there.
- Experienced developers who are willing to fix a bug, or maybe create a brand new feature, see the [Contribution Guide](CONTRIBUTING.md).

Your PRs are welcome!

## 👍 Support
If you like Traditional T9, you could donate on [buymeacoffee.com](https://www.buymeacoffee.com/sspanak).

Or if you just want to show your appreciation, give the project a star. Higher-ranked projects on GitHub have the opportunity to use development tools for free and apply for funding more easily.

Thanks to your donations, a brand new testing device is available, a Sonim XP3800! This will result in much better compatibility with Sonim in the future. So, keep going!

## 🕮 License
- The source code, the logo image, and the icons are licensed under the conditions described in [LICENSE.txt](LICENSE.txt).
- The dictionaries are licensed under the licenses provided in the [respective readme files](docs/dictionaries), where applicable. Detailed information about the dictionaries is also available there.
- [Silver foil photo created by rawpixel.com - www.freepik.com](https://www.freepik.com/photos/silver-foil)
- "Negotiate" and "Vibrocentric" fonts are under [The Fontspring Desktop/Ebook Font End User License](docs/desktop-ebook-EULA-1.8.txt).

## 💪 Privacy Policy and Philosophy
- No ads, no premium or paid features. It's all free.
- No spying, no tracking, no telemetry or reports. No nothing!
- No network connectivity.
- It only does its job.
- Open-source, so you can verify all the above yourself.
- Created with help from the entire community.
