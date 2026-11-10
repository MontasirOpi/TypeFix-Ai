# Voice typing device checks

Build with Flutter and Android SDK installed: `flutter pub get`, `flutter analyze`,
`flutter test`, `flutter build apk --debug`. Install on a physical Android phone;
a microphone-enabled emulator with an offline recognizer can also be used.

- Android 12+ with offline recognition: allow mic, return to keyboard, tap Speak,
  dictate, Finish. Verify one insertion at the cursor; selected text is replaced.
- Enable airplane mode: verify supported downloaded language still works.
- Deny/revoke microphone permission: verify no recording or crash; grant from app
  settings if Android no longer offers the permission dialog.
- Missing recognizer/language and Android 8–11: verify explanatory status and
  normal typing/correction still work. No cloud fallback should occur.
- Silence, recognizer failure, repeated Finish taps, and 60-second timeout: verify
  a recoverable status and a usable Speak button.
- Close keyboard, switch app/editor, move cursor, or type during recognition:
  verify old results never insert into the changed editor.
- Password and PIN fields: verify Speak is disabled.
- Long-press Speak/Finish during recording/processing: verify no insertion.
- Test WhatsApp, Gmail, and Notes, including selected text and empty fields.
- Verify Fix/Improve controls are disabled during recording and restored afterward.

These checks require a real recognition provider; static checks cannot establish
microphone behavior, language availability, or OEM keyboard lifecycle behavior.
