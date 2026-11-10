# TypeFix AI

> **Fix what you type, anywhere, privately and offline.**

TypeFix AI is a privacy-first, fully offline AI writing assistant for Android. It operates everywhere you type—WhatsApp, Chrome, Gmail, Notes, and docs—without sending a single keystroke or character to any external server.

---

## Voice typing (Android)

Enable TypeFix in Android keyboard settings and select it as your keyboard. Open an
editable field, tap **🎤 Speak**, grant microphone access, then return and tap Speak
again. Speak in your device's default language and tap **Finish** (or wait for the
recognizer to finish). The transcript appears in a scrollable preview. Tap **Insert text** to insert it
at the cursor or replace the selection, or **Cancel** to discard it. Tap **Fix**
afterward for corrections. **Undo** restores the last insertion or correction
while the field contents and cursor still match that edit.
Cancel is available during listening, processing, and preview. Long-press
Speak/Finish also cancels without inserting anything.

Voice requires **Android 12+**, an on-device recognition provider, and an installed
language pack. Availability varies by phone and language. Older devices can still
use the normal keyboard and correction tools. There is no cloud recognition fallback,
no microphone use before an explicit tap, and TypeFix does not save recordings.
Closing the keyboard, switching editors, or moving the selection cancels dictation
and discards any preview. Undo history stays only in memory and is cleared when
the keyboard closes or the editor changes. Editors that cannot provide a reliable
text snapshot (up to 20,000 characters) cannot use safe insertion/correction.
A session times out after 60 seconds. This version supports dictation; spoken
assistant commands and automatic AI rewriting are not implemented.

See [voice testing](docs/voice-testing.md) for device validation.

## Permissions and installation

This keyboard-only version requests microphone access for explicitly started voice
typing. Accessibility, floating overlays, background foreground services, and their
notification permission have been removed, including the related setup controls.
The optional Android text-selection action remains available.

Removing unused sensitive capabilities reduces access, but does not guarantee a
Play Protect verdict. Do not disable Play Protect. If Google still blocks a build,
the developer should use Google's official app-verification/appeal process.
For distribution, configure a stable developer signing key and prefer Play testing.
The current release build still uses the project's debug signing configuration;
GitHub-hosted runs may use different debug keys, so updates may fail signature checks.
Do not uninstall an existing app without considering local settings/models/history.

## 🌟 Key Highlights

- **100% Offline by Design**: Powered by on-device deterministic rules and ONNX Runtime Mobile inference. Zero cloud API calls (no OpenAI, Anthropic, Gemini, or Firebase).
- **Hybrid Native Architecture**:
  - **Native Android (Kotlin)**: Core `InputMethodService` typing engine, candidate toolbar, text extraction, deterministic rule engine, and ONNX inference.
  - **Flutter UI**: Clean, utility-focused management application (Onboarding, Home, Model Manager, Preferences, Privacy Dashboard, and Local History).
- **User-Controlled Invocations**: No silent auto-rewriting in V1. You type normally and tap **`✨ Fix`** only when you want text corrected.
- **Strict Meaning Preservation**: Features a `DiffEngine` and `SafetyValidator` that prevents creative hallucinations, protects numbers, URLs, emails, `@mentions`, and rejects excessive drift in Fix mode.
- **Zero Keystroke Telemetry**: No account required. No background accessibility screen scraping. Password fields are automatically detected and ignored.
- **History OFF by Default**: History recording is disabled upon install. If toggled on, data is kept strictly on-device and can be cleared with one tap.

---

## 🏗️ Architecture

```
Android Text View (WhatsApp / Chrome / Notes)
       │
       ▼
InputMethodService (TypeFixInputMethodService)
       │
       ▼
TextContextReader (Extracts selection / active sentence; ignores passwords)
       │
       ▼
CorrectionEngine
 ├── TextPreprocessor (Tokenizes, extracts protected entities)
 ├── RuleEngine (<50ms deterministic typo & contraction correction)
 ├── SpellChecker (Levenshtein distance lookup)
 ├── LocalModelEngine (ONNX Runtime Mobile quantized neural inference)
 ├── PostProcessor (Re-attaches original whitespace & styles)
 ├── DiffEngine (Word & character diff metrics)
 └── SafetyValidator (Rejects entity loss or creative drift)
       │
       ▼
TextReplacer (Atomically commits replacement text)
```

Flutter interacts with native Android only through a controlled `MethodChannel` (`com.typefix.ai/bridge`) for application-level preferences and model management—never for individual keystrokes.

---

## 🚀 Quick Start & Build

### Prerequisites
- **Flutter SDK**: 3.24+
- **Android SDK**: API 26+ (Android 8.0+)
- **Java**: JDK 17

### Building the APK
```bash
# Get dependencies
flutter pub get

# Run Android unit tests
cd android
./gradlew :app:testDebugUnitTest

# Run Flutter analyze & test
cd ..
flutter analyze
flutter test

# Build debug or release APK
flutter build apk --debug
```

---

## 🛠️ Project Structure

```
typefix/
├── android/
│   └── app/src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/ (layout/keyboard_view.xml, xml/method.xml, values/)
│       │   └── kotlin/com/typefix/ai/
│       │       ├── MainActivity.kt
│       │       ├── keyboard/
│       │       │   ├── TypeFixInputMethodService.kt
│       │       │   ├── KeyboardController.kt
│       │       │   ├── TextContextReader.kt
│       │       │   ├── TextReplacer.kt
│       │       │   └── SuggestionController.kt
│       │       ├── correction/
│       │       │   ├── CorrectionEngine.kt
│       │       │   ├── TextPreprocessor.kt
│       │       │   ├── RuleEngine.kt
│       │       │   ├── SpellChecker.kt
│       │       │   ├── PostProcessor.kt
│       │       │   ├── DiffEngine.kt
│       │       │   └── SafetyValidator.kt
│       │       ├── ai/
│       │       │   ├── LocalModelEngine.kt
│       │       │   ├── OnnxModelEngine.kt
│       │       │   ├── ModelManager.kt
│       │       │   ├── Tokenizer.kt
│       │       │   └── ModelMetadata.kt
│       │       └── bridge/
│       │           └── FlutterBridge.kt
│       └── test/kotlin/com/typefix/ai/
│           ├── RuleEngineTest.kt
│           ├── TextPreprocessorTest.kt
│           └── SafetyValidatorTest.kt
│
├── lib/
│   ├── main.dart
│   ├── app/ (app.dart, routes.dart, theme.dart)
│   ├── core/ (constants, storage, platform)
│   ├── features/
│   │   ├── onboarding/
│   │   ├── home/
│   │   ├── keyboard/
│   │   ├── models/
│   │   ├── settings/
│   │   ├── privacy/
│   │   └── history/
│   └── services/
│
├── docs/
│   ├── architecture.md
│   ├── model-selection.md
│   ├── privacy.md
│   ├── testing.md
│   └── roadmap.md
└── README.md
```

---

## 🧪 Testing Verification

All unit tests pass:
- `RuleEngineTest`: Validates 40+ high-frequency typos, contractions, casing preservation, and capitalization.
- `TextPreprocessorTest`: Validates URL, email, `@mention`, `#hashtag`, code, and numeric entity extraction.
- `SafetyValidatorTest`: Validates rejection of extreme creative drift in `Fix` mode and entity preservation.
- `Flutter Widget Test`: Validates onboarding flow, GetX `GetBuilder` state rendering, and navigation.

---

## 🔒 Privacy & Safety Guarantee

TypeFix AI is committed to 100% offline data integrity:
- **No Keystroke Logging**
- **No Cloud AI APIs**
- **Password & PIN Fields Ignored**
- **History OFF by Default**
- **Meaning Preservation Enforced**

---

## 📄 License

Apache License 2.0. See [LICENSE](LICENSE) for details.
