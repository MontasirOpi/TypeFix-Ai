# TypeFix AI — Product Roadmap

## V1.0 (Delivered)
- [x] Native Android `InputMethodService` (`TypeFixInputMethodService`)
- [x] Interactive on-screen key deck and candidate action toolbar
- [x] Fast deterministic `RuleEngine` (<50ms target) for typos, contractions, and punctuation
- [x] ONNX Runtime Mobile integration for local neural sequence inference
- [x] 6 writing action modes: `Fix`, `Improve`, `Professional`, `Friendly`, `Shorten`, `Expand`
- [x] `DiffEngine` and `SafetyValidator` enforcing meaning preservation and entity security
- [x] Password and secure field auto-detection and protection
- [x] Flutter management application with GetX `GetBuilder`
- [x] Privacy dashboard and local-only history (OFF by default)
- [x] On-device model manager architecture

## V1.1 (Upcoming)
- [ ] NNAPI hardware acceleration delegate toggle for compatible Snapdragon / Tensor chips
- [ ] User personal custom dictionary for custom jargon, names, and abbreviations
- [ ] Haptic feedback adjustments for key presses in the native key deck
- [ ] Offline spell-check dictionary expansion (50,000+ lemmas)

## V2.0 (Multilingual & Advanced Style)
- [ ] Bangla (`bn`) and Banglish offline grammar model support
- [ ] Custom writing style personas (Academic, Legal, Creative)
- [ ] Offline translation between supported local model languages
