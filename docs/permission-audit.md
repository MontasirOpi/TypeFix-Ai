# Permission audit — 1.2.1

The 1.2.0 floating build already declared RECORD_AUDIO, FOREGROUND_SERVICE,
FOREGROUND_SERVICE_MICROPHONE and the BIND_ACCESSIBILITY_SERVICE service permission.
It also declared the microphone foreground-service type, accessibility window-content
capability and recognition-service package query. No missing mandatory declaration
was found for those APIs. A declaration alone does not grant runtime access.

1.2.1 adds optional POST_NOTIFICATIONS for Android 13+ to show the microphone notice
in the notification drawer. It is requested separately and is not required to start
a microphone foreground session. Denial does not grant or revoke microphone access.

Setup now displays actual microphone, accessibility and notification status and
refreshes after returning from Android settings. Repeated microphone denials lead
to an explicit app-settings option rather than a request dialog that no longer opens.
Recognition permission errors and Android microphone startup restrictions now have
separate feedback instead of being shown as missing offline language support.

BIND_ACCESSIBILITY_SERVICE is a system binding protection on the service, not a
runtime permission the app can request. Users must explicitly enable the service in
Accessibility settings. FOREGROUND_SERVICE permissions are manifest permissions,
not runtime prompts. The system-managed accessibility overlay does not require
SYSTEM_ALERT_WINDOW. Extra SMS, contacts, storage or overlay permissions are not
needed and would not resolve the reported installation warning.

The release workflow checks required declarations in the packaged APK, the microphone
service type, accessibility binding and absence of a replacement IME. It does not
prove Google Play Protect approval or physical-device recording eligibility.

Device checks: deny/revoke/grant microphone; deny notifications and verify microphone
remains independently granted; deny microphone while notifications are granted;
permanently deny and follow the explicit app-settings route; return from settings
and verify statuses refresh; test recording with the notification drawer disabled;
confirm Play Protect installation decisions separately from runtime permission errors.
