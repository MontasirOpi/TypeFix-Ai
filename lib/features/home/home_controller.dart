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
  bool canDrawOverlays = false;
  bool isAccessibilityEnabled = false;
  bool isFloatingAssistantRunning = false;
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
    canDrawOverlays = await NativeBridge.canDrawOverlays();
    isAccessibilityEnabled = await NativeBridge.isAccessibilityEnabled();
    isFloatingAssistantRunning = await NativeBridge.isFloatingAssistantRunning();
    models = await modelService.getModels();
    privacyStatus = await NativeBridge.getPrivacyStatus();

    isCheckingStatus = false;
    update();
  }

  Future<void> toggleFloatingAssistant(bool enable) async {
    if (enable) {
      if (!canDrawOverlays) {
        await NativeBridge.requestOverlayPermission();
        return;
      }
      await NativeBridge.startFloatingAssistant();
    } else {
      await NativeBridge.stopFloatingAssistant();
    }
    await Future.delayed(const Duration(milliseconds: 500));
    await refreshAll();
  }

  Future<void> requestOverlayPermission() async {
    await NativeBridge.requestOverlayPermission();
    await Future.delayed(const Duration(seconds: 1));
    await refreshAll();
  }

  Future<void> openAccessibilitySettings() async {
    await NativeBridge.openAccessibilitySettings();
    await Future.delayed(const Duration(seconds: 1));
    await refreshAll();
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
