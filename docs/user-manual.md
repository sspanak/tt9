# Traditional T9
TT9 is an IME (Input Method Editor) for Android devices with hardware keypad. It supports multiple languages and predictive text typing. _NOTE: TT9 is not usable on touchscreen-only devices._

All source code, documentation and the privacy policy are available on Github: [https://github.com/sspanak/tt9](https://github.com/sspanak/tt9).

## Initial Setup
After installing, in order to use Traditional T9, you need to enable it as an Android keyboard. To do so:

- Go to Android Settings → System → Languages → Keyboards.
- Add Traditional T9 IME.

_The actual menu names may vary depending on your Android version and phone._

### Enabling Predictive Mode
With the default settings, it is only possible to type in 123 and ABC modes. In order to enable the Predictive mode, there are additional steps:

- Open the [Settings screen](#settings-screen).
- Select the desired languages.
- Load the dictionaries.

_If you don't do the above, there will be no suggestions when typing in Predictive mode._

#### Dictionary Tips
Once a dictionary is loaded, it will stay there until you use the Clear option. This means you can enable and disable languages without reloading their dictionaries every time. Just do it once, only the first time.

It also means that if you need to start using language X, you can safely disable all other languages, load only dictionary X (and save time!), then re-enable all languages you used before.

Have in mind reloading a dictionary will reset the suggestion popularity to the factory defaults _(your custom added words will not be affected)_. However, there should be nothing to worry about. For the most part, you will see little to no difference in the suggestion order, unless you oftenly use uncommon words.

## Hotkeys

#### D-pad Up (↑):
Select previous word/letter suggestion.

#### D-pad Down (↓):
Select next word/letter suggestion.

#### D-pad Right (→):
_Predictive mode only._

- **Single press**: Filter the suggestion list, leaving out only the ones that start with the current word. It doesn't matter if it is a complete word or not. For example, type "rewin" and press Right. It will leave out all words starting with "rewin": "rewin" itself, "rewind", "rewinds", "rewinded", "rewinding", and so on.
- **Double press**: Expand the filter to the full suggestion. For example, type "rewin" and press Right twice. It will first filter by "rewin", then expand the filter to "rewind". You can keep expanding the filter with Right, until you get to the longest suggestion in the list.

Filtering can also be used to type unknown words. Let's say you want to type "Anakin", which is not in the dictionary. Start with "A", then press Right to hide "B" and "C". Now press 6-key. Since the filter is on, in addition to the real dictionary words, it will provide all possible combinations for 6: "Am", "An", "Ao". Select "An" and press Right to confirm your selection. Now pressing 2-key, will provide "Ana", "Anb", "Anc". You can keep going, until you complete "Anakin".

When filtering is enabled, the base text will become bold and italicized.

#### D-pad Left (←):
_Predictive mode only._

- Clear the suggestion filter, if applied.
- When no filter is applied, accept the current word as-is, even if it does not fully match a suggestion, then jump before it.

#### 0-key:
- **In 123 mode:**
  - **Press:** type "0".
  - **Hold:** type "+".
- **In ABC mode:**
  - **Press:** type space, newline or special/math characters.
  - **Hold:** type "0".
- **In Predictive mode:**
  - **Press:** type space, newline or special/math characters.
  - **Double Press:** type the character assigned in Predictive mode settings. (Default: ".")
  - **Hold:** type "0".

#### 1- to 9-key:
- **In 123 mode:** type the respective number.
- **In ABC and Predictive mode:** type a letter/punctuation character or hold to type the respective number.

#### Add Word Key (Default: Press ✱):
Add a new word to the dictionary for the current language.

#### Backspace Key (Default: Press ↩ / Back):
Just deletes text.

_**NB:** Using "Back" as backspace does not work in all applications, most notably Firefox and Spotify. They are able to take full control of the key and redefine its function, meaning it will do whatever the app authors intended. Unfortunately, nothing can be done, because "Back" plays a special role in Android and its usage is restricted by the system._

_**NB 2:** Holding "Back" key will always trigger the default system action (i.e. show running applications list)._

_In these cases, you could assign another key (all other keys are fully usable), or use the on-screen backspace._

#### Next Input Mode Key (Default: Press #):
- **Press when there are no suggestions:** Cycle the input modes (abc → ABC → Predictive → 123). Note that only 123 mode is available in numeric fields and Predictive mode is not available in password fields.
- **Press while suggestions are on:** Toggle the suggestions between UPPERCASE and lowercase.
- **Number-only fields:** No special action. Type a "#" with the default key. Changing the mode is not possible in such fields.

#### Next Language Key (Default: Hold #):
Select the next language, when mulitple languages have been enabled from the Settings.

#### Settings Key (Default: Hold ✱):
Open the Configration screen.

## On-screen Soft keys
All functionality is available using the keypad, but for convenience, on touchscreen phones, you could also use the on-screen keys. If you instead prefer to have more screen space, disable them from the Settings.

#### Left Soft Key:
Open the [Settings screen](#settings-screen).

#### Right Soft Key:
Backspace.

## Settings Screen
On the Settings screen, you can choose languages for typing, configure the keypad hotkeys or change the application appearance.

To access it:
- Start typing in a text field to wake up TT9.
- Use the on-screen gear button or press the Settings Key.
