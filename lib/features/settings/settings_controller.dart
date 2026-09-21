import 'package:get/get.dart';
import '../../services/settings_service.dart';

class SettingsController extends GetxController {
  final SettingsService settingsService = SettingsService();

  bool isLoading = true;
  bool historyEnabled = false;
  bool ruleEngineEnabled = true;
  bool localAiEnabled = true;
  String activeLanguage = 'en';
  String defaultAction = 'FIX';

  @override
  void onInit() {
    super.onInit();
    loadSettings();
  }

  Future<void> loadSettings() async {
    isLoading = true;
    update();

    final settings = await settingsService.loadSettings();
    historyEnabled = settings['historyEnabled'] as bool? ?? false;
    ruleEngineEnabled = settings['ruleEngineEnabled'] as bool? ?? true;
    localAiEnabled = settings['localAiEnabled'] as bool? ?? true;
    activeLanguage = settings['activeLanguage'] as String? ?? 'en';
    defaultAction = settings['defaultAction'] as String? ?? 'FIX';

    isLoading = false;
    update();
  }

  Future<void> toggleHistory(bool value) async {
    historyEnabled = value;
    update();
    await settingsService.setHistoryEnabled(value);
  }

  Future<void> toggleRuleEngine(bool value) async {
    ruleEngineEnabled = value;
    update();
    await settingsService.setRuleEngineEnabled(value);
  }

  Future<void> toggleLocalAi(bool value) async {
    localAiEnabled = value;
    update();
    await settingsService.setLocalAiEnabled(value);
  }

  Future<void> setDefaultAction(String action) async {
    defaultAction = action;
    update();
    await settingsService.setDefaultAction(action);
  }
}
