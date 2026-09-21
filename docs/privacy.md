# TypeFix AI — Privacy & Security Architecture

## 1. Core Privacy Promise

> **Fix what you type, anywhere, privately and offline.**

TypeFix AI is intentionally engineered to operate with zero cloud reliance, zero external telemetry, and zero user account dependencies.

## 2. Technical Privacy Guarantees

### 1. 100% On-Device Execution
- AI inference runs locally using ONNX Runtime Mobile binaries.
- The rule engine executes entirely in memory within the local process.
- No network requests are initiated for grammar checks, spelling checks, or style rewrites.

### 2. Password & Secure Field Immunity
- The `TextContextReader` inspects the active `EditorInfo.inputType`.
- Any field flagged as `TYPE_TEXT_VARIATION_PASSWORD`, `TYPE_TEXT_VARIATION_VISIBLE_PASSWORD`, `TYPE_TEXT_VARIATION_WEB_PASSWORD`, or `TYPE_NUMBER_VARIATION_PASSWORD` is automatically ignored.
- The correction toolbar displays `🔒 Password field ignored for privacy` and disables correction actions on secure fields.

### 3. No Keystroke Logging
- TypeFix does not capture, store, or transmit individual keystrokes.
- Text is read as a transient snapshot only when the user taps an action button (e.g. `✨ Fix`).

### 4. History is OFF by Default
- In compliance with privacy-by-design standards, history recording is disabled upon initial installation.
- If enabled by the user, history is stored in local encrypted `SharedPreferences` on the device.
- Users can clear individual items or perform a complete wipe at any time.

### 5. Protected Entity Preservation
- URLs, email addresses, numbers, `@mentions`, and `#hashtags` are detected by `TextPreprocessor` and protected by `SafetyValidator`.
- Corrections that corrupt or alter protected entities are immediately rejected to prevent data loss.
