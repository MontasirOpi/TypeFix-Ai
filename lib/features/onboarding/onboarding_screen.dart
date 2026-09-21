import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../../app/theme.dart';
import 'onboarding_controller.dart';

class OnboardingScreen extends StatelessWidget {
  const OnboardingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GetBuilder<OnboardingController>(
      init: OnboardingController(),
      builder: (controller) {
        return Scaffold(
          body: SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 20),
                  // Logo / Header
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: AppTheme.primaryBlue.withAlpha(40),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: AppTheme.primaryBlue.withAlpha(80)),
                        ),
                        child: const Icon(
                          Icons.spellcheck,
                          color: AppTheme.primaryBlueLight,
                          size: 28,
                        ),
                      ),
                      const SizedBox(width: 14),
                      const Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'TypeFix AI',
                            style: TextStyle(
                              fontSize: 22,
                              fontWeight: FontWeight.bold,
                              color: AppTheme.textPrimary,
                            ),
                          ),
                          Text(
                            'Offline AI Writing Assistant',
                            style: TextStyle(
                              fontSize: 13,
                              color: AppTheme.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 36),

                  // Content Body
                  Expanded(
                    child: SingleChildScrollView(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          _buildBenefitCard(
                            icon: Icons.offline_bolt_outlined,
                            title: '100% Offline & Private',
                            description:
                                'All grammar correction and rewriting runs directly on your phone using local open-source models. No text is ever uploaded.',
                          ),
                          const SizedBox(height: 16),
                          _buildBenefitCard(
                            icon: Icons.touch_app_outlined,
                            title: 'User-Controlled Fixing',
                            description:
                                'No silent overwrites. Type normally anywhere, then tap ✨ Fix when you want your text polished.',
                          ),
                          const SizedBox(height: 16),
                          _buildBenefitCard(
                            icon: Icons.security_outlined,
                            title: 'Security Guaranteed',
                            description:
                                'Password and PIN fields are automatically ignored. No telemetry or keystroke logging.',
                          ),
                          const SizedBox(height: 24),

                          // Enable Keyboard Action Card
                          Container(
                            padding: const EdgeInsets.all(18),
                            decoration: BoxDecoration(
                              color: AppTheme.darkSurface,
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                color: controller.isKeyboardEnabled
                                    ? AppTheme.successGreen
                                    : AppTheme.darkBorder,
                              ),
                            ),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Icon(
                                      controller.isKeyboardEnabled
                                          ? Icons.check_circle
                                          : Icons.keyboard_outlined,
                                      color: controller.isKeyboardEnabled
                                          ? AppTheme.successGreen
                                          : AppTheme.primaryBlueLight,
                                      size: 22,
                                    ),
                                    const SizedBox(width: 10),
                                    Text(
                                      controller.isKeyboardEnabled
                                          ? 'TypeFix Keyboard Enabled'
                                          : 'Enable Keyboard in Settings',
                                      style: const TextStyle(
                                        fontSize: 15,
                                        fontWeight: FontWeight.w600,
                                        color: AppTheme.textPrimary,
                                      ),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 10),
                                Text(
                                  controller.isKeyboardEnabled
                                      ? 'TypeFix AI is activated as an input method on this device.'
                                      : 'To use TypeFix AI in any app, toggle it on in Android System Settings.',
                                  style: const TextStyle(
                                    fontSize: 13,
                                    color: AppTheme.textSecondary,
                                  ),
                                ),
                                const SizedBox(height: 14),
                                SizedBox(
                                  width: double.infinity,
                                  child: controller.isKeyboardEnabled
                                      ? OutlinedButton(
                                          onPressed: () => controller.checkKeyboardStatus(),
                                          child: const Text('Refresh Status'),
                                        )
                                      : ElevatedButton(
                                          onPressed: () => controller.openKeyboardSettings(),
                                          child: const Text('Open Keyboard Settings'),
                                        ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  // Bottom Action
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: ElevatedButton(
                      onPressed: () => controller.completeOnboarding(),
                      child: const Text(
                        'Continue to App',
                        style: TextStyle(fontSize: 16),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildBenefitCard({
    required IconData icon,
    required String title,
    required String description,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppTheme.darkSurface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppTheme.darkBorder),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: AppTheme.primaryBlueLight, size: 24),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: AppTheme.textPrimary,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  description,
                  style: const TextStyle(
                    fontSize: 13,
                    color: AppTheme.textSecondary,
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
