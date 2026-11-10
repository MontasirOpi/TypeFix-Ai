import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../../core/storage/storage_service.dart';
import '../assistant/assistant_screen.dart';
class OnboardingScreen extends StatelessWidget {
  const OnboardingScreen({super.key});
  @override
  Widget build(BuildContext context) => AssistantScreen(onContinue: () async {
    await StorageService.setOnboardingCompleted(true);
    Get.offAllNamed('/home');
  });
}
