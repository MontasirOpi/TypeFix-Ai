import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../../app/theme.dart';
import 'home_controller.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GetBuilder<HomeController>(
      init: HomeController(),
      builder: (controller) {
        return Scaffold(
          appBar: AppBar(
            title: const Row(
              children: [
                Icon(Icons.spellcheck, color: AppTheme.primaryBlueLight, size: 24),
                SizedBox(width: 10),
                Text('TypeFix AI'),
              ],
            ),
            actions: [
              IconButton(
                icon: const Icon(Icons.refresh),
                tooltip: 'Refresh Status',
                onPressed: () => controller.refreshAll(),
              ),
              IconButton(
                icon: const Icon(Icons.settings_outlined),
                tooltip: 'Settings',
                onPressed: () => Get.toNamed('/settings'),
              ),
            ],
          ),
          body: RefreshIndicator(
            onRefresh: () => controller.refreshAll(),
            child: SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // 1. Keyboard Status Card
                  _buildKeyboardStatusCard(controller),

                  const SizedBox(height: 12),

                  // Voice typing instructions
                  _buildVoiceTypingCard(),

                  const SizedBox(height: 12),

                  // 3. Local AI Model Status Card
                  _buildModelStatusCard(controller),

                  const SizedBox(height: 12),

                  // 3. Privacy Guarantee Card
                  _buildPrivacyCard(),

                  const SizedBox(height: 16),

                  // 4. Interactive Test Field
                  _buildInteractiveTestArea(controller),

                  const SizedBox(height: 16),

                  // 5. Quick Navigation Grid
                  _buildNavigationSection(),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildKeyboardStatusCard(HomeController controller) {
    final bool isEnabled = controller.isKeyboardEnabled;
    final bool isSelected = controller.isKeyboardSelected;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  isEnabled ? Icons.check_circle : Icons.warning_amber_rounded,
                  color: isEnabled ? AppTheme.successGreen : AppTheme.warningOrange,
                  size: 22,
                ),
                const SizedBox(width: 8),
                Text(
                  isEnabled ? 'Keyboard Enabled' : 'Keyboard Not Enabled',
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: AppTheme.textPrimary,
                  ),
                ),
                const Spacer(),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: isSelected
                        ? AppTheme.primaryBlue.withAlpha(40)
                        : AppTheme.darkBorder,
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(
                    isSelected ? 'ACTIVE' : 'STANDBY',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: isSelected ? AppTheme.primaryBlueLight : AppTheme.textSecondary,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Text(
              isEnabled
                  ? (isSelected
                      ? 'TypeFix AI is your active keyboard. Tap any text field to begin.'
                      : 'TypeFix AI is enabled in system settings. Switch to it using the IME switcher.')
                  : 'Enable TypeFix in Android Settings to start correcting text anywhere.',
              style: const TextStyle(fontSize: 13, color: AppTheme.textSecondary),
            ),
            const SizedBox(height: 14),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => controller.openKeyboardSettings(),
                    child: Text(isEnabled ? 'Manage Settings' : 'Enable Keyboard'),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => controller.openInputMethodPicker(),
                    child: const Text('Switch Keyboard'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildVoiceTypingCard() {
    return const Card(
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Speak, review, insert',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600,
                    color: AppTheme.textPrimary)),
            SizedBox(height: 8),
            Text('Select the TypeFix keyboard, tap Speak, then Finish. Review the '
                'transcript and choose Insert text or Cancel. Tap Undo to reverse '
                'your last insertion or correction before editing further.',
                style: TextStyle(color: AppTheme.textSecondary)),
            SizedBox(height: 8),
            Text('Offline voice requires Android 12+ and a supported language pack. '
                'Microphone permission is requested only when you tap Speak. '
                'No accessibility or floating-window access is needed.',
                style: TextStyle(fontSize: 12, color: AppTheme.textSecondary)),
          ],
        ),
      ),
    );
  }

  Widget _buildModelStatusCard(HomeController controller) {
    return Card(
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppTheme.primaryBlue.withAlpha(30),
            borderRadius: BorderRadius.circular(8),
          ),
          child: const Icon(Icons.memory, color: AppTheme.primaryBlueLight),
        ),
        title: const Text(
          'English AI (On-Device)',
          style: TextStyle(fontWeight: FontWeight.w600, color: AppTheme.textPrimary),
        ),
        subtitle: const Text(
          'Quantized ONNX Mobile • ~28 MB • Ready',
          style: TextStyle(fontSize: 12, color: AppTheme.textSecondary),
        ),
        trailing: TextButton(
          onPressed: () => Get.toNamed('/models'),
          child: const Text('Manage'),
        ),
      ),
    );
  }

  Widget _buildPrivacyCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.shield_outlined, color: AppTheme.successGreen, size: 20),
                SizedBox(width: 8),
                Text(
                  'Privacy & Security',
                  style: TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: AppTheme.textPrimary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            _buildPrivacyRow('✓ 100% on-device local execution'),
            _buildPrivacyRow('✓ No cloud processing or external AI APIs'),
            _buildPrivacyRow('✓ Password and PIN fields automatically ignored'),
            _buildPrivacyRow('✓ History is OFF by default and purely local'),
          ],
        ),
      ),
    );
  }

  Widget _buildPrivacyRow(String text) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Text(
        text,
        style: const TextStyle(fontSize: 13, color: AppTheme.textSecondary),
      ),
    );
  }

  Widget _buildInteractiveTestArea(HomeController controller) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.edit_note, color: AppTheme.primaryBlueLight, size: 20),
                SizedBox(width: 8),
                Text(
                  'Test Keyboard & Correction',
                  style: TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: AppTheme.textPrimary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            const Text(
              'Tap below to open the keyboard and test the ✨ Fix toolbar actions:',
              style: TextStyle(fontSize: 12, color: AppTheme.textSecondary),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: controller.testInputController,
              maxLines: 3,
              style: const TextStyle(color: AppTheme.textPrimary, fontSize: 14),
              decoration: InputDecoration(
                filled: true,
                fillColor: AppTheme.darkBg,
                hintText: 'Type text with mistakes...',
                hintStyle: const TextStyle(color: AppTheme.textSecondary),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(color: AppTheme.darkBorder),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(color: AppTheme.darkBorder),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: const BorderSide(color: AppTheme.primaryBlue),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNavigationSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Quick Settings',
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: AppTheme.textSecondary,
          ),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: _buildNavCard(
                icon: Icons.security,
                title: 'Privacy',
                onTap: () => Get.toNamed('/privacy'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: _buildNavCard(
                icon: Icons.history,
                title: 'History',
                onTap: () => Get.toNamed('/history'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: _buildNavCard(
                icon: Icons.tune,
                title: 'Preferences',
                onTap: () => Get.toNamed('/settings'),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildNavCard({
    required IconData icon,
    required String title,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          color: AppTheme.darkSurface,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: AppTheme.darkBorder),
        ),
        child: Column(
          children: [
            Icon(icon, color: AppTheme.primaryBlueLight, size: 22),
            const SizedBox(height: 6),
            Text(
              title,
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w500,
                color: AppTheme.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
