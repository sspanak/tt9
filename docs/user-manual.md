# Traditional T9
TT9 is an IME (Input Method Editor) for Android devices with hardware keypad. It supports multiple languages and predictive text typing. _NOTE: TT9 is not usable on touchscreen-only devices._

Source code and documentation are available on Github: [https://github.com/sspanak/tt9](https://github.com/sspanak/tt9).

## Initial Setup
- Go to Android Settings → System → Languages → Keyboards.
- Add Traditional T9 IME.

With the default settings, it is only possible to type in 123 and ABC modes. In order to enable the Predictive mode, you must also do the following:

- Open TT9 preferences.
- Select the desired languages.
- Load the dictionaries.

_If you don't do the above, nothing will happen when you try to type in Predictive mode._

## Hotkeys
#### D-pad Up (↑):
Select previous word suggestion

#### D-pad Down (↓):
Select next word suggestion

#### Left Soft Key:
Open Traditional T9 Preferences screen.

#### Right Soft Key:
Backspace.

#### Text Mode Key (Hash/Pound/#):
- **Short press:** Cycle input modes (Predictive → Abc → 123)
- **Short press while typing a word:**: Change between UPPERCASE and lowercase.
- **Long press:** Select next language
- **Number-only fields:** Type a "#". Changing the mode is not possible in such fields.

#### Backspace Key (Back/↩):
- **Short Press when there is text:** Usually, "backspace". However, some applications, most notably Firefox and Spotify, forbid this action in their search fields. This is due to the fact Android allows applications to take over control of the physical keypad and redefine what buttons do. Unfortunately, nothing can be done in such cases, "Back" will function as the application authors intended, instead of as backspace.
- **Short Press when there is no text:** System default, no special action (usually, go back)
- **Long Press:** System default, no special action

#### Other Actions Key (Star/✱):
- **Short press:** Add a word to the dictionary.
- **Long press:** Open Traditional T9 Preferences screen.

## Configuration Options
TODO...

## License
- The source code, the logo image and the icons are licensed under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- The word lists / dictionaries are licensed under the licenses provided in the respective readme files found in the source code, where applicable.
- [Silver foil photo created by rawpixel.com - www.freepik.com](https://www.freepik.com/photos/silver-foil)
- "Roboto" font is under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- "Negotiate" and "Vibrocentric" fonts are under [The Fontspring Desktop/Ebook Font End User License](desktop-ebook-EULA-1.8.txt)

## Privacy Policy
Traditional T9 does not collect any information about you or about the way you are using using the application.
