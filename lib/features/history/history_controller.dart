import 'package:get/get.dart';
import '../../core/storage/storage_service.dart';
import '../../services/settings_service.dart';

class HistoryController extends GetxController {
  final SettingsService settingsService = SettingsService();

  bool isHistoryEnabled = false;
  List<HistoryItem> items = [];

  @override
  void onInit() {
    super.onInit();
    loadHistory();
  }

  void loadHistory() {
    isHistoryEnabled = StorageService.isHistoryEnabled();
    items = StorageService.getHistory();
    update();
  }

  Future<void> toggleHistory(bool enabled) async {
    isHistoryEnabled = enabled;
    update();
    await settingsService.setHistoryEnabled(enabled);
    loadHistory();
  }

  Future<void> deleteItem(String id) async {
    await StorageService.deleteHistoryItem(id);
    loadHistory();
  }

  Future<void> clearAll() async {
    await StorageService.clearHistory();
    loadHistory();
  }
}
