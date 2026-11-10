# TypeFix AI — floating voice assistant

Keep Gboard, Samsung Keyboard, or your preferred keyboard. TypeFix is an app with
a floating microphone, not a replacement keyboard. Version 1.2.1+4.

## Use

1. Open TypeFix and choose **Set up floating voice**. Read and accept the disclosure,
   then enable **TypeFix floating voice** in Android Accessibility settings.
2. Allow microphone access in TypeFix, or when you first tap Speak.
3. Open a message or note in another app with your usual keyboard. A floating
   Speak / Cancel / Undo widget appears above the keyboard.
4. Tap **Speak**, dictate, then **Finish**. The final transcript is inserted at the
   cursor (replacing the selection, if any). Cancel discards the session.
5. Undo restores the last insertion only if the same field, text and cursor remain.

Voice currently needs Android 12+, a supported on-device recognizer, and the device
language installed for offline recognition. There is no cloud fallback. Some apps,
WebViews, and custom text fields do not expose Accessibility editing operations;
those fields cannot be supported by this implementation. Hardware keyboards without
an on-screen IME window do not trigger the widget. Password fields are excluded.

## Access and privacy

Accessibility is required to detect the keyboard and insert into a focused field
while another keyboard remains selected. It grants broad OS capabilities; this
implementation only queries focused editable nodes and reads their text after an
explicit Speak action (or to validate that action/Undo). It does not log events,
store recordings/text, use the clipboard, or upload speech. A microphone foreground
notification is active during a recording/recognition session only. In-memory
session/Undo data is cleared when the keyboard closes or the field changes.

The widget uses TYPE_ACCESSIBILITY_OVERLAY, with no SYSTEM_ALERT_WINDOW or special-use
foreground service. The microphone foreground service is declared for recording.
The replacement IME is no longer registered, and setup never asks to switch keyboards.

**Play Protect:** accessibility for a sideloaded app can trigger installation or
restricted-settings blocks. This build cannot guarantee Google's approval. Do not
disable Play Protect; use Google's developer verification/appeal and legitimate Play
Store testing distribution. Production distribution also needs a stable signing key;
this repository currently uses debug signing for release test builds.

## Validation

`flutter pub get`, `flutter test`, `flutter build apk --release`, then
`cd android && ./gradlew :app:testDebugUnitTest`.

GitHub Actions builds and uploads the APK after tests and manifest checks. Native
unit tests cover text replacement/Undo. Flutter tests check that setup retains the
existing keyboard and requires consent before opening accessibility settings.

Physical-device validation is still required. See [device checks](docs/floating-voice-testing.md).

## Permission setup update

Setup shows microphone, accessibility, and optional recording notification status.
Use **Open app permissions** after a repeated denial. Notification access is optional
and requested separately on Android 13+. See [permission audit](docs/permission-audit.md).
This update does not establish or guarantee Play Protect approval.
