import '../core/platform/native_bridge.dart';
import '../core/storage/storage_service.dart';

class SettingsService {
  Future<Map<String, dynamic>> loadSettings() async {
    final nativeSettings = await NativeBridge.getSettings();
    return {
      'historyEnabled': StorageService.isHistoryEnabled(),
      'ruleEngineEnabled': nativeSettings['ruleEngineEnabled'] ?? true,
      'localAiEnabled': nativeSettings['localAiEnabled'] ?? true,
      'activeLanguage': nativeSettings['activeLanguage'] ?? 'en',
      'defaultAction': nativeSettings['defaultAction'] ?? 'FIX',
    };
  }

  Future<void> setHistoryEnabled(bool enabled) async {
    await StorageService.setHistoryEnabled(enabled);
    await NativeBridge.updateSettings('history_enabled', enabled);
  }

  Future<void> setRuleEngineEnabled(bool enabled) async {
    await NativeBridge.updateSettings('rule_engine_enabled', enabled);
  }

  Future<void> setLocalAiEnabled(bool enabled) async {
    await NativeBridge.updateSettings('local_ai_enabled', enabled);
  }

  Future<void> setDefaultAction(String action) async {
    await NativeBridge.updateSettings('default_action', action);
  }
}
