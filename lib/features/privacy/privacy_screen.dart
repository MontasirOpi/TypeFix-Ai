import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../../app/theme.dart';
import 'privacy_controller.dart';

class PrivacyScreen extends StatelessWidget {
  const PrivacyScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GetBuilder<PrivacyController>(
      init: PrivacyController(),
      builder: (controller) {
        return Scaffold(
          appBar: AppBar(
            title: const Text('Privacy Dashboard'),
          ),
          body: ListView(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            children: [
              // Header Card
              Container(
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: AppTheme.darkSurface,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppTheme.darkBorder),
                ),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: AppTheme.successGreen.withAlpha(30),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(Icons.verified_user, color: AppTheme.successGreen, size: 28),
                    ),
                    const SizedBox(width: 14),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Privacy-First Architecture',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: AppTheme.textPrimary,
                            ),
                          ),
                          SizedBox(height: 4),
                          Text(
                            'TypeFix AI is engineered so that your text never leaves your device.',
                            style: TextStyle(fontSize: 12, color: AppTheme.textSecondary),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 20),
              const Text(
                'Guarantees & Policies',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: AppTheme.textSecondary,
                ),
              ),
              const SizedBox(height: 10),

              _buildPolicyItem(
                icon: Icons.cloud_off,
                title: 'Zero Cloud AI APIs',
                description:
                    'No OpenAI, Anthropic, Gemini, Firebase, or external cloud inference. All models execute locally using ONNX Runtime Mobile.',
              ),
              _buildPolicyItem(
                icon: Icons.no_accounts,
                title: 'No Account Required',
                description:
                    'You do not need to register, sign in, or provide personal details to use TypeFix AI.',
              ),
              _buildPolicyItem(
                icon: Icons.lock_outline,
                title: 'Password & PIN Protection',
                description:
                    'The native InputMethodService automatically detects password and sensitive text fields and ignores them completely.',
              ),
              _buildPolicyItem(
                icon: Icons.visibility_off_outlined,
                title: 'No Keystroke Logging',
                description:
                    'Individual typing events are handled strictly in the active Android text view. TypeFix does not log or persist raw keystrokes.',
              ),
              _buildPolicyItem(
                icon: Icons.history_toggle_off,
                title: 'History is OFF by Default',
                description:
                    'Correction history is disabled by default. If enabled by choice, it is stored in encrypted on-device storage and can be deleted anytime.',
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildPolicyItem({
    required IconData icon,
    required String title,
    required String description,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, color: AppTheme.primaryBlueLight, size: 22),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: AppTheme.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    description,
                    style: const TextStyle(fontSize: 12, color: AppTheme.textSecondary, height: 1.4),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
