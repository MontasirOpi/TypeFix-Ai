# Floating voice device checks

Test on Android 12+ with downloaded on-device recognition, keeping Play Protect on.

- Confirm TypeFix is not listed as an enabled/available replacement keyboard.
- Setup: cancel disclosure and verify accessibility settings do not open; agree
  and enable the service. Grant/deny/revoke microphone permission.
- Gboard and Samsung Keyboard: in Notes, WhatsApp and Gmail open/close the keyboard;
  widget must appear/disappear and not steal field focus or change your keyboard.
- Tap Speak: microphone indicator and foreground notice appear. Tap Finish:
  transcript inserts at the cursor, preserving surrounding text. Test selection
  replacement, an empty field, emoji, multiline text, and right-to-left content.
- Cancel while listening/processing: no insertion, recording stops. Let the
  60-second timeout fire: recording stops, widget remains usable.
- Switch apps, move cursor, edit text, close the keyboard, or enter a password
  field mid-recording: session is cancelled, late results never insert elsewhere.
- Undo: restore original text/selection once. After typing, changing focus or
  closing the keyboard: no stale Undo can change another field.
- Airplane mode with installed language: dictation works. Missing recognizer or
  unsupported language: explanatory status, no cloud fallback.
- Test floating/landscape keyboards and rotation. Check widget stays reachable.
- Disable accessibility: widget and recording disappear; service releases resources.
- Apps without ACTION_SET_TEXT or reliable cursor offsets: explanatory feedback,
  no clipboard fallback or mutation of a different field.

Automated tests/builds do not validate actual OS accessibility-window events,
OEM microphone foreground restrictions, recognition providers, or Play Protect.
