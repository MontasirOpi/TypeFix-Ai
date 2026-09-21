import '../core/platform/native_bridge.dart';

class KeyboardService {
  Future<bool> isEnabled() async {
    return await NativeBridge.isKeyboardEnabled();
  }

  Future<bool> isSelected() async {
    return await NativeBridge.isKeyboardSelected();
  }

  Future<bool> openSettings() async {
    return await NativeBridge.openKeyboardSettings();
  }

  Future<bool> openPicker() async {
    return await NativeBridge.openInputMethodPicker();
  }
}
