# TypeFix AI — Architecture & Engineering Specification

## 1. System Overview

TypeFix AI is a hybrid mobile application engineered for Android. It marries a high-performance native Kotlin `InputMethodService` typing and correction pipeline with a Flutter-based management interface.

```
┌─────────────────────────────────────────────────────────────┐
│                    Flutter Application                      │
│                                                             │
│   • Onboarding         • Home Dashboard   • Settings        │
│   • Privacy Dashboard  • Model Manager    • Local History   │
└──────────────────────────────┬──────────────────────────────┘
                               │
                      Flutter MethodChannel
                     (App-level control ONLY)
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    Native Android Kotlin                    │
│                                                             │
│   • TypeFixInputMethodService                               │
│   • KeyboardController                                      │
│   • TextContextReader                                       │
│   • TextReplacer                                            │
│   • SuggestionController                                    │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                     Correction Pipeline                     │
│                                                             │
│   1. TextPreprocessor  (Extract entities & tokenization)    │
│   2. RuleEngine        (High-speed deterministic fixes)     │
│   3. SpellChecker      (Levenshtein typo correction)        │
│   4. LocalModelEngine  (ONNX Runtime Mobile quantized AI)   │
│   5. PostProcessor     (Restore whitespace & styles)        │
│   6. DiffEngine        (Word & character diff metrics)      │
│   7. SafetyValidator   (Entity check & drift rejection)     │
└─────────────────────────────────────────────────────────────┘
```

## 2. Key Architecture Principles

1. **No Keystroke Streaming**: Individual keystrokes are processed strictly inside the native editor process. Keystrokes are never routed over Flutter's `MethodChannel` or transmitted to any background daemon.
2. **User-Controlled Invocations**: In V1, TypeFix AI does not automatically rewrite user text in the background. Corrections run only when the user explicitly triggers an action (e.g. tapping `✨ Fix`, `Improve`, `Professional`, `Friendly`, `Shorten`, or `Expand`).
3. **Deterministic First**: In Fix mode, cheap deterministic rules execute first (<50ms target) to eliminate common typos, punctuation faults, contraction mistakes, and capitalization issues before invoking ONNX neural inference.
4. **Safety & Meaning Preservation**:
   - Protected entities (URLs, email addresses, phone/numbers, `@mentions`, `#hashtags`, and code tokens) are strictly validated.
   - If the candidate output corrupts a protected entity or exceeds the allowable edit drift threshold in `Fix` mode, the output is rejected and the system falls back safely to original text or safe rule output.
5. **No Accessibility Scraping**: TypeFix AI relies exclusively on official Android `InputMethodService` APIs (`InputConnection`), eliminating fragile accessibility permissions and background overhead.
