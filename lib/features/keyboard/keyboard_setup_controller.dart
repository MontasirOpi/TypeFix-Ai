import 'package:get/get.dart';
import '../../services/keyboard_service.dart';

class KeyboardSetupController extends GetxController {
  final KeyboardService keyboardService = KeyboardService();

  bool isKeyboardEnabled = false;
  bool isKeyboardSelected = false;

  @override
  void onInit() {
    super.onInit();
    checkStatus();
  }

  Future<void> checkStatus() async {
    isKeyboardEnabled = await keyboardService.isEnabled();
    isKeyboardSelected = await keyboardService.isSelected();
    update();
  }

  Future<void> openKeyboardSettings() async {
    await keyboardService.openSettings();
    await Future.delayed(const Duration(seconds: 1));
    await checkStatus();
  }

  Future<void> openInputMethodPicker() async {
    await keyboardService.openPicker();
    await Future.delayed(const Duration(seconds: 1));
    await checkStatus();
  }
}
