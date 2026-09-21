import 'package:get/get.dart';
import '../../core/storage/storage_service.dart';
import '../../services/keyboard_service.dart';

class OnboardingController extends GetxController {
  final KeyboardService keyboardService = KeyboardService();

  int currentPage = 0;
  bool isKeyboardEnabled = false;

  @override
  void onInit() {
    super.onInit();
    checkKeyboardStatus();
  }

  Future<void> checkKeyboardStatus() async {
    isKeyboardEnabled = await keyboardService.isEnabled();
    update();
  }

  void setPage(int page) {
    currentPage = page;
    update();
  }

  Future<void> openKeyboardSettings() async {
    await keyboardService.openSettings();
    await Future.delayed(const Duration(seconds: 1));
    await checkKeyboardStatus();
  }

  Future<void> completeOnboarding() async {
    await StorageService.setOnboardingCompleted(true);
    Get.offAllNamed('/home');
  }
}
