import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../../services/keyboard_service.dart';
import '../../services/model_service.dart';
import '../../core/platform/native_bridge.dart';

class HomeController extends GetxController {
  final KeyboardService keyboardService = KeyboardService();
  final ModelService modelService = ModelService();

  bool isKeyboardEnabled = false;
  bool isKeyboardSelected = false;
  bool isCheckingStatus = false;
  List<ModelInfo> models = [];
  Map<String, dynamic> privacyStatus = {};

  final TextEditingController testInputController = TextEditingController(
    text: 'i am goign to office tomorow',
  );

  @override
  void onInit() {
    super.onInit();
    refreshAll();
  }

  @override
  void onClose() {
    testInputController.dispose();
    super.onClose();
  }

  Future<void> refreshAll() async {
    isCheckingStatus = true;
    update();

    isKeyboardEnabled = await keyboardService.isEnabled();
    isKeyboardSelected = await keyboardService.isSelected();
    models = await modelService.getModels();
    privacyStatus = await NativeBridge.getPrivacyStatus();

    isCheckingStatus = false;
    update();
  }

  Future<void> openKeyboardSettings() async {
    await keyboardService.openSettings();
    await Future.delayed(const Duration(seconds: 1));
    await refreshAll();
  }

  Future<void> openInputMethodPicker() async {
    await keyboardService.openPicker();
    await Future.delayed(const Duration(seconds: 1));
    await refreshAll();
  }
}
