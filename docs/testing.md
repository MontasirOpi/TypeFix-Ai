# TypeFix AI — Testing Matrix & Verification Guide

## 1. Test Suite Overview

TypeFix AI employs a multi-tiered testing strategy:
- **Native Unit Tests (Kotlin)**: Validate deterministic rules, typo mappings, tokenizer, diff metrics, entity extraction, and safety boundaries.
- **Flutter Unit & Widget Tests**: Validate GetX GetBuilder controllers, persistent storage, and navigation transitions.
- **App Compatibility Testing**: Validate `InputMethodService` behavior across standard Android applications.

## 2. Running Automated Tests

### Native Android Unit Tests
```bash
cd android
./gradlew :app:testDebugUnitTest
```
Tests verified:
- `RuleEngineTest`: Validates 40+ typo corrections, contraction fixes, proper casing preservation, and sentence punctuation.
- `TextPreprocessorTest`: Validates URL, email, `@mention`, `#hashtag`, and numeric entity identification.
- `SafetyValidatorTest`: Validates rejection of creative drift in `Fix` mode and entity corruption prevention.

### Flutter Tests & Analysis
```bash
flutter analyze
flutter test
```

## 3. Application Verification Matrix

| Target Application | Test Scenario | Verified Behavior |
|---|---|---|
| **WhatsApp** | Sentence typo: `i am goign to office tomorow` | Tap `✨ Fix` -> Replaced with `I am going to office tomorrow.` |
| **Google Chrome** | Selected text correction | Only the selected substring is replaced; cursor moves cleanly to end |
| **Gmail** | Professional rewrite mode | Casual greeting converted to polite formal wording |
| **Google Keep / Notes** | Multiline text extraction | Line boundaries and bullet points preserved |
| **Any Password Field** | Sensitive input detection | Service status displays `🔒 Password field ignored for privacy`; no text read |
| **URLs & Emails** | Entity protection | `Send to dev@typefix.ai at https://typefix.ai` -> Entities unchanged |
| **Flight/Booking Numbers** | Number preservation | `Flight 421 departs at 5pm` -> Numbers 421 and 5 strictly preserved |
| **System Settings** | IME enable & switch flow | TypeFix toggle and system IME picker trigger reliably |
