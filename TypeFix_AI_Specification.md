# TypeFix AI --- Product & Technical Specification

## 1. Project Overview

**TypeFix AI** is an Android-first, privacy-first, fully offline AI
writing assistant that helps users correct grammar, spelling,
punctuation, capitalization, and wording anywhere they type.

The application is designed as a **Flutter + native Android Kotlin
hybrid**:

-   Flutter: application UI, onboarding, settings, model management,
    privacy dashboard, history.
-   Kotlin: Android `InputMethodService`, keyboard/input integration,
    text extraction/replacement, correction pipeline.
-   ONNX Runtime Mobile: local AI inference.
-   Open-source/locally packaged models: no paid AI API and no cloud
    text processing.

### Core promise

> Fix what you type, anywhere, privately and offline.

## 2. Product Principles

1.  **100% offline by default**
2.  **No AI API dependency**
3.  **No account required**
4.  **No typed text uploaded to a server**
5.  **User-controlled correction**
6.  **Fast interaction**
7.  **Small and battery-conscious**
8.  **Modular model architecture**
9.  **Android-first**
10. **Flutter used only where it adds value**

## 3. Target Platform

### Primary

-   Android phones
-   Android 8.0+ / API 26+
-   Modern ARM64 devices as the primary target

### Future

-   Additional languages
-   Optional iOS companion/app if technically appropriate

## 4. High-Level Architecture

``` text
                    ┌───────────────────────────┐
                    │       Flutter App         │
                    │                           │
                    │ Onboarding                │
                    │ Home                      │
                    │ Settings                  │
                    │ Model Manager             │
                    │ Privacy                   │
                    │ History                   │
                    └─────────────┬─────────────┘
                                  │
                         Flutter MethodChannel
                                  │
                    ┌─────────────▼─────────────┐
                    │     Native Android        │
                    │                           │
                    │ InputMethodService       │
                    │ Keyboard/Input UI        │
                    │ Text Extraction          │
                    │ Text Replacement         │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │    Correction Pipeline    │
                    │                           │
                    │ 1. Text preprocessing     │
                    │ 2. Rule engine            │
                    │ 3. Spell checker          │
                    │ 4. Local AI model         │
                    │ 5. Post-processing        │
                    │ 6. Diff validation        │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │      Local AI Runtime     │
                    │                           │
                    │       ONNX Runtime        │
                    │            │              │
                    │       Quantized Model     │
                    └───────────────────────────┘
```

## 5. Why Native Android Is Required

The main typing pipeline must not be implemented as a continuously
running Flutter background service.

Use Android's `InputMethodService` as the core integration layer.

The keyboard should:

-   Receive text context from the active editor.
-   Read selected text when available.
-   Read a limited amount of text around the cursor when appropriate.
-   Show correction actions.
-   Replace selected/current text.
-   Work independently of the Flutter UI.

Flutter should not receive every keystroke.

### Correct data flow

``` text
User types
    ↓
Android InputMethodService
    ↓
Native correction engine
    ↓
Local model if required
    ↓
Result
    ↓
Android text editor
```

## 6. Main User Experience

### 6.1 First Launch

Show a simple onboarding:

1.  What TypeFix AI does
2.  Privacy explanation
3.  Enable keyboard
4.  Choose language
5.  Download/enable local model if needed
6.  Test the keyboard

### 6.2 Main Screen

Display:

-   Keyboard status
-   Current language
-   Installed model
-   AI status
-   Privacy status
-   Quick settings

Example:

``` text
TYPEFIX AI

✓ Keyboard Enabled

English AI
Installed

Privacy
✓ On-device
✓ No cloud processing

[ Test Keyboard ]
[ Settings ]
```

## 7. Keyboard Experience

The keyboard should initially prioritize a compact correction toolbar.

Example:

``` text
┌────────────────────────────────────┐
│ ✨ Fix   Improve   Professional  ⋯ │
└────────────────────────────────────┘
```

The normal Android keyboard can remain responsible for normal typing.

The correction layer should not unnecessarily recreate a complete
keyboard in V1 unless required by the chosen implementation.

## 8. Core Correction Actions

### Fix

Only correct:

-   Grammar
-   Spelling
-   Capitalization
-   Punctuation
-   Common contractions
-   Obvious typing mistakes

Example:

``` text
i am goign to office tomorow

→

I am going to the office tomorrow.
```

### Improve

Improve clarity and natural wording without changing the user's intent.

### Professional

Convert text into professional wording.

### Friendly

Make wording natural and friendly.

### Shorten

Reduce unnecessary words while preserving meaning.

### Expand

Expand short text into clearer wording.

## 9. Critical UX Rule

Never silently rewrite user text by default.

The default behavior should be:

``` text
User text
    ↓
Suggestion
    ↓
User taps Fix
    ↓
Text replaced
```

Do not automatically change text without user confirmation in V1.

## 10. Correction Engine

The correction engine should be modular.

``` text
CorrectionEngine
│
├── TextPreprocessor
├── RuleEngine
├── SpellChecker
├── GrammarModel
├── RewriteModel
├── PostProcessor
├── DiffEngine
└── SafetyValidator
```

### 10.1 Text Preprocessor

Responsibilities:

-   Normalize whitespace
-   Detect sentence boundaries
-   Preserve emojis
-   Preserve URLs
-   Preserve usernames
-   Preserve numbers
-   Preserve code-like text
-   Preserve intentional capitalization
-   Detect selected text vs surrounding context

Do not unnecessarily modify:

-   URLs
-   Email addresses
-   File paths
-   Code
-   Password fields
-   Numbers
-   Usernames/handles

## 11. Rule Engine

Use deterministic rules for cheap, fast corrections.

Examples:

``` text
teh → the
recieve → receive
definately → definitely
dont → don't
cant → can't
```

The rule engine should run before AI inference.

Advantages:

-   Very fast
-   No model inference
-   Low battery usage
-   Predictable behavior

## 12. AI Model Layer

The AI model must be replaceable.

Do not hard-code the entire application around one model.

Use an interface similar to:

``` text
LocalModelEngine
    ├── loadModel()
    ├── unloadModel()
    ├── isLoaded()
    ├── correctText()
    ├── rewriteText()
    └── getModelInfo()
```

### Initial model investigation

Benchmark small open-source text-to-text/grammar-correction models.

A practical candidate for early prototyping is a small FLAN-T5-family
model exported to ONNX.

Do not assume the first model tested is the final production model.

Benchmark:

-   Grammar accuracy
-   Spelling accuracy
-   Meaning preservation
-   Latency
-   RAM
-   Model size
-   Battery impact
-   License compatibility

## 13. ONNX Runtime

Use ONNX Runtime Mobile for local inference.

Recommended pipeline:

``` text
Open-source model
       ↓
ONNX export
       ↓
Validation
       ↓
Quantization
       ↓
Android integration
       ↓
ONNX Runtime Mobile
```

Prefer quantized models when quality remains acceptable.

## 14. Model Management

Do not necessarily bundle every model into the base APK.

Support:

``` text
Model Manager

English
✓ Installed

Bangla
○ Not installed

Future languages
○ Available later
```

Model manager responsibilities:

-   Model metadata
-   Version
-   Size
-   Language
-   Installed state
-   Load/unload
-   Delete model
-   Validate model
-   Model update mechanism

For V1, model installation can be packaged locally if remote model
distribution would complicate the first release. The production
architecture should still keep the model layer modular.

## 15. Privacy Architecture

Default policy:

``` text
User Text
   ↓
Device
   ↓
Local Correction Engine
   ↓
Local AI
   ↓
Corrected Text
```

No typed content should be transmitted to a server.

Do not collect:

-   Typed text
-   Selected text
-   Clipboard contents
-   Passwords
-   Private messages

Avoid analytics that contain text content.

### Password fields

Do not process password/secure fields.

### Privacy dashboard

Display:

``` text
Privacy

✓ AI runs on this device
✓ No account required
✓ No text uploaded
✓ No cloud AI required
✓ Password fields ignored
```

## 16. History

History should be:

**OFF by default.**

If enabled, store data locally.

Possible structure:

``` text
CorrectionHistory
-----------------
id
timestamp
originalText
correctedText
action
language
```

Provide:

-   Enable/disable history
-   Clear all history
-   Individual deletion
-   Automatic retention option

## 17. Data Storage

Use local storage for settings.

Possible Flutter choices:

-   SharedPreferences for simple settings
-   SQLite/Drift for structured history

Do not use a remote database for core functionality.

## 18. Flutter ↔ Kotlin Bridge

Use `MethodChannel` only for application-level communication.

Example calls:

``` text
isKeyboardEnabled
openKeyboardSettings
getInstalledModels
installModel
deleteModel
getModelStatus
getPrivacyStatus
getSettings
updateSettings
```

Do not send every keystroke through the MethodChannel.

## 19. Suggested Project Structure

``` text
typefix_ai/
│
├── android/
│   └── app/
│       └── src/main/
│           ├── AndroidManifest.xml
│           └── kotlin/com/typefix/ai/
│               ├── keyboard/
│               │   ├── TypeFixInputMethodService.kt
│               │   ├── KeyboardController.kt
│               │   ├── TextContextReader.kt
│               │   ├── TextReplacer.kt
│               │   └── SuggestionController.kt
│               │
│               ├── correction/
│               │   ├── CorrectionEngine.kt
│               │   ├── TextPreprocessor.kt
│               │   ├── RuleEngine.kt
│               │   ├── SpellChecker.kt
│               │   ├── PostProcessor.kt
│               │   ├── DiffEngine.kt
│               │   └── SafetyValidator.kt
│               │
│               ├── ai/
│               │   ├── LocalModelEngine.kt
│               │   ├── OnnxModelEngine.kt
│               │   ├── ModelManager.kt
│               │   ├── Tokenizer.kt
│               │   └── ModelMetadata.kt
│               │
│               └── bridge/
│                   └── FlutterBridge.kt
│
├── lib/
│   ├── main.dart
│   │
│   ├── app/
│   │   ├── app.dart
│   │   ├── routes.dart
│   │   └── theme.dart
│   │
│   ├── core/
│   │   ├── constants/
│   │   ├── storage/
│   │   ├── platform/
│   │   ├── errors/
│   │   └── utils/
│   │
│   ├── features/
│   │   ├── onboarding/
│   │   ├── home/
│   │   ├── keyboard/
│   │   ├── models/
│   │   ├── settings/
│   │   ├── privacy/
│   │   └── history/
│   │
│   └── services/
│       ├── keyboard_service.dart
│       ├── model_service.dart
│       ├── settings_service.dart
│       └── storage_service.dart
│
├── models/
│   └── README.md
│
├── docs/
│   ├── architecture.md
│   ├── model-selection.md
│   ├── privacy.md
│   ├── testing.md
│   └── roadmap.md
│
└── README.md
```

## 20. Development Phases

### Phase 1 --- Android Input System

Goal: prove text interaction.

Implement:

-   InputMethodService
-   Keyboard enable flow
-   Text selection
-   Text extraction
-   Text replacement
-   Basic correction toolbar

No AI.

### Phase 2 --- Rule Engine

Implement:

-   Common spelling mistakes
-   Capitalization
-   Punctuation
-   Contractions
-   Basic grammar rules

### Phase 3 --- Local AI

Implement:

-   ONNX Runtime
-   Model loading
-   Tokenization
-   Inference
-   Output decoding
-   Model lifecycle
-   Error handling

### Phase 4 --- Writing Actions

Add:

-   Fix
-   Improve
-   Professional
-   Friendly
-   Shorten
-   Expand

### Phase 5 --- Model Manager

Add:

-   Model installation
-   Model deletion
-   Model versioning
-   Model validation
-   Model status

### Phase 6 --- Privacy and Performance

Test:

-   Battery
-   RAM
-   Cold start
-   Warm inference
-   Model loading
-   Large text
-   Long sessions

### Phase 7 --- Production Polish

Add:

-   Dark mode
-   Accessibility
-   Animations
-   Error states
-   Onboarding
-   Help
-   Privacy explanation
-   Settings

## 21. Performance Targets

Set engineering targets:

### Rule correction

Target:

``` text
< 50 ms
```

### AI correction

Target:

``` text
Ideally < 1 second
```

The exact target depends on the device and selected model.

### Battery

Do not continuously run AI inference.

Only run inference when:

-   User taps Fix
-   User explicitly requests a writing action
-   An optional future suggestion mode is enabled

## 22. Error Handling

If AI inference fails:

``` text
AI unavailable
    ↓
Try rule-based correction
    ↓
If still unavailable:
show original text unchanged
```

Never destroy or replace user text when the correction result is
invalid.

## 23. Diff Validation

Before replacing text:

1.  Compare original and generated text.
2.  Detect excessive changes.
3.  Reject suspiciously large changes.
4.  Preserve URLs, numbers and special tokens.
5.  Require confirmation for high-change rewrites.

Example:

``` text
Original:
I need the file tomorrow.

Generated:
I am writing to formally request that you kindly provide
the document at your earliest convenience.

Action:
REJECT for Fix mode
```

Because the meaning/style changed too much.

## 24. Security Rules

Never process:

-   Password fields
-   PIN fields
-   Sensitive authentication fields

Avoid logging raw text.

Development logs must use:

``` text
Text processed successfully
```

instead of:

``` text
Original text: ...
```

## 25. Testing Strategy

Test across:

-   WhatsApp
-   Messenger
-   Facebook
-   Chrome
-   Gmail
-   Telegram
-   Notes
-   Google Docs
-   Different Android keyboards/apps

Test:

-   Normal text
-   Selected text
-   Long text
-   Multiline text
-   Emojis
-   URLs
-   Emails
-   Numbers
-   Mixed languages
-   Empty text
-   Secure fields

## 26. V1 Feature List

### Must have

-   Android
-   Flutter settings app
-   Native InputMethodService
-   English
-   Grammar correction
-   Spelling correction
-   Punctuation
-   Capitalization
-   Local rule engine
-   Local AI model
-   ONNX Runtime
-   Fix button
-   Text replacement
-   Privacy-first design
-   No account
-   No paid API

### Should have

-   Improve
-   Professional
-   Friendly
-   Shorten
-   Model manager
-   Dark mode
-   Local history

### Later

-   Bangla
-   Banglish
-   Translation
-   More local models
-   Custom correction preferences
-   Personal dictionary
-   Custom writing styles

## 27. Non-Goals for V1

Do not initially build:

-   Cloud AI backend
-   User accounts
-   Social features
-   Chatbot
-   Large LLM
-   Continuous background monitoring
-   Automatic replacement of every sentence
-   iOS version
-   Complex subscription system

## 28. Definition of Done

The V1 is complete when a user can:

1.  Install the application.
2.  Enable the TypeFix keyboard/input service.
3.  Open WhatsApp or another normal text app.
4.  Type a sentence containing mistakes.
5.  Tap Fix.
6.  Receive a locally generated correction.
7.  Replace the original text.
8.  Perform the same action without internet.
9.  Confirm that the app does not upload typed content.
10. Disable the keyboard cleanly from Android settings.

## 29. Product Direction

The long-term goal is not to create another generic AI chatbot.

The product should feel like:

> **A tiny offline AI layer that lives wherever the user types.**

The user should rarely need to open the main TypeFix application.

The main app exists for configuration; the keyboard/input layer is the
actual product.
