import 'package:get/get.dart';
import '../../core/platform/native_bridge.dart';

class PrivacyController extends GetxController {
  bool isLoading = true;
  Map<String, dynamic> privacyData = {};

  @override
  void onInit() {
    super.onInit();
    loadPrivacyStatus();
  }

  Future<void> loadPrivacyStatus() async {
    isLoading = true;
    update();

    privacyData = await NativeBridge.getPrivacyStatus();

    isLoading = false;
    update();
  }
}
