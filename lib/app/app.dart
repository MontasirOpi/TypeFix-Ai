import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../core/storage/storage_service.dart';
import 'routes.dart';
import 'theme.dart';

class TypeFixApp extends StatelessWidget {
  const TypeFixApp({super.key});

  @override
  Widget build(BuildContext context) {
    final bool completedOnboarding = StorageService.isOnboardingCompleted();

    return GetMaterialApp(
      title: 'TypeFix AI',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.darkTheme,
      initialRoute: completedOnboarding ? AppRoutes.home : AppRoutes.onboarding,
      getPages: AppRoutes.pages,
    );
  }
}
