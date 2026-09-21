import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../../app/theme.dart';
import 'keyboard_setup_controller.dart';

class KeyboardSetupScreen extends StatelessWidget {
  const KeyboardSetupScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GetBuilder<KeyboardSetupController>(
      init: KeyboardSetupController(),
      builder: (controller) {
        return Scaffold(
          appBar: AppBar(
            title: const Text('Keyboard Setup'),
          ),
          body: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildStepCard(
                  stepNumber: '1',
                  title: 'Enable in Android Settings',
                  description: 'Turn on TypeFix AI under Available virtual keyboards.',
                  isDone: controller.isKeyboardEnabled,
                  actionLabel: controller.isKeyboardEnabled ? 'Enabled' : 'Open Settings',
                  onAction: () => controller.openKeyboardSettings(),
                ),
                const SizedBox(height: 14),
                _buildStepCard(
                  stepNumber: '2',
                  title: 'Select Active Keyboard',
                  description: 'Choose TypeFix AI as your current input method.',
                  isDone: controller.isKeyboardSelected,
                  actionLabel: controller.isKeyboardSelected ? 'Active' : 'Switch Keyboard',
                  onAction: () => controller.openInputMethodPicker(),
                ),
                const SizedBox(height: 14),
                _buildStepCard(
                  stepNumber: '3',
                  title: 'Test Correction Anywhere',
                  description:
                      'Open WhatsApp, Chrome, or Notes, type any sentence, and tap ✨ Fix on the toolbar.',
                  isDone: false,
                  actionLabel: 'Return Home',
                  onAction: () => Get.back(),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildStepCard({
    required String stepNumber,
    required String title,
    required String description,
    required bool isDone,
    required String actionLabel,
    required VoidCallback onAction,
  }) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 26,
                  height: 26,
                  decoration: BoxDecoration(
                    color: isDone ? AppTheme.successGreen : AppTheme.primaryBlue,
                    shape: BoxShape.circle,
                  ),
                  child: Center(
                    child: isDone
                        ? const Icon(Icons.check, size: 16, color: Colors.white)
                        : Text(
                            stepNumber,
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 13,
                            ),
                          ),
                  ),
                ),
                const SizedBox(width: 12),
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: AppTheme.textPrimary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              description,
              style: const TextStyle(fontSize: 13, color: AppTheme.textSecondary),
            ),
            const SizedBox(height: 14),
            SizedBox(
              width: double.infinity,
              child: isDone
                  ? OutlinedButton(
                      onPressed: onAction,
                      child: Text(actionLabel),
                    )
                  : ElevatedButton(
                      onPressed: onAction,
                      child: Text(actionLabel),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
