# Traditional T9
TT9 is an IME (Input Method Editor) for Android devices with hardware keypad. It supports multiple languages and predictive text typing. _NOTE: TT9 is not usable on touchscreen-only devices._

Source code and documentation are available on Github: [https://github.com/sspanak/tt9](https://github.com/sspanak/tt9).

## Initial Setup
TODO: Initial config, loading a dictionary...

## Hotkeys
#### D-pad Up (↑):
Select previous word suggestion

#### D-pad Down (↓):
Select next word suggestion

#### Left Soft Key:
Insert symbol or Add word depending on state and context. Add word only available in Predictive input mode.

#### Right Soft Key:
- **Short press:** Cycle input modes (Predictive → Abc → 123)
- **Long press:** Bring up the TT9 preference screen

#### Star (\*):
- **Short press:** Change case
- **Long press:**
    - When multiple languages are enabled: Change language
    - When single language is enabled: Bring up smiley insert dialog
    - Numeric mode: Insert a star

#### Hash/Pound (#):
- **Short press:** Space
- **Long press:**
    - New line
    - Numeric mode: Insert hash/pound (#)

#### Back (↩):
- **Short Press when there is text:** Usually, "backspace". However, some applications, most notably Firefox and Spotify, forbid this action in their search fields. This is due to the fact Android allows applications to take over control of the physical keypad and redefine what buttons do. Unfortunately, nothing can be done in such cases, "Back" will function as the application authors intended, instead of as backspace.
- **Short Press when there is no text:** System default, no special action (usually, go back)
- **Long Press:** System default, no special action

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