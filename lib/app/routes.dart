import 'package:get/get.dart';
import '../features/onboarding/onboarding_screen.dart';
import '../features/home/home_screen.dart';
import '../features/models/model_screen.dart';
import '../features/settings/settings_screen.dart';
import '../features/privacy/privacy_screen.dart';
import '../features/history/history_screen.dart';

class AppRoutes {
  static const String onboarding = '/onboarding';
  static const String home = '/home';
  static const String keyboardSetup = '/keyboard-setup';
  static const String models = '/models';
  static const String settings = '/settings';
  static const String privacy = '/privacy';
  static const String history = '/history';

  static final List<GetPage> pages = [
    GetPage(
      name: onboarding,
      page: () => const OnboardingScreen(),
      transition: Transition.fadeIn,
    ),
    GetPage(
      name: home,
      page: () => const HomeScreen(),
      transition: Transition.fadeIn,
    ),
    GetPage(
      name: keyboardSetup,
      page: () => const HomeScreen(),
      transition: Transition.rightToLeft,
    ),
    GetPage(
      name: models,
      page: () => const ModelScreen(),
      transition: Transition.rightToLeft,
    ),
    GetPage(
      name: settings,
      page: () => const SettingsScreen(),
      transition: Transition.rightToLeft,
    ),
    GetPage(
      name: privacy,
      page: () => const PrivacyScreen(),
      transition: Transition.rightToLeft,
    ),
    GetPage(
      name: history,
      page: () => const HistoryScreen(),
      transition: Transition.rightToLeft,
    ),
  ];
}
