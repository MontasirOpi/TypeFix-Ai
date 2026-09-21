import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../../app/theme.dart';
import 'settings_controller.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GetBuilder<SettingsController>(
      init: SettingsController(),
      builder: (controller) {
        return Scaffold(
          appBar: AppBar(
            title: const Text('Correction Settings'),
          ),
          body: controller.isLoading
              ? const Center(child: CircularProgressIndicator())
              : ListView(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  children: [
                    const Text(
                      'Correction Engine',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: AppTheme.textSecondary,
                      ),
                    ),
                    const SizedBox(height: 8),

                    // Rule Engine Switch
                    Card(
                      child: SwitchListTile(
                        value: controller.ruleEngineEnabled,
                        onChanged: (val) => controller.toggleRuleEngine(val),
                        title: const Text('Deterministic Rule Engine'),
                        subtitle: const Text(
                          'Fast (<50ms) offline corrections for common typos, contractions, and capitalization.',
                          style: TextStyle(fontSize: 12, color: AppTheme.textSecondary),
                        ),
                      ),
                    ),

                    // Local AI Switch
                    Card(
                      child: SwitchListTile(
                        value: controller.localAiEnabled,
                        onChanged: (val) => controller.toggleLocalAi(val),
                        title: const Text('Local AI Model (ONNX)'),
                        subtitle: const Text(
                          'Runs on-device AI inference for deep grammar fixes, style rewrites, and tone improvements.',
                          style: TextStyle(fontSize: 12, color: AppTheme.textSecondary),
                        ),
                      ),
                    ),

                    const SizedBox(height: 16),
                    const Text(
                      'Privacy & History',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: AppTheme.textSecondary,
                      ),
                    ),
                    const SizedBox(height: 8),

                    // History Switch (OFF by default)
                    Card(
                      child: SwitchListTile(
                        value: controller.historyEnabled,
                        onChanged: (val) => controller.toggleHistory(val),
                        title: const Text('Correction History'),
                        subtitle: const Text(
                          'OFF by default. Stores your corrections purely locally on this device. Never sent to any server.',
                          style: TextStyle(fontSize: 12, color: AppTheme.textSecondary),
                        ),
                      ),
                    ),

                    const SizedBox(height: 16),
                    const Text(
                      'Default Writing Action',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: AppTheme.textSecondary,
                      ),
                    ),
                    const SizedBox(height: 8),

                    Card(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                        child: DropdownButtonFormField<String>(
                          initialValue: controller.defaultAction,
                          dropdownColor: AppTheme.darkSurface,
                          style: const TextStyle(color: AppTheme.textPrimary, fontSize: 14),
                          decoration: const InputDecoration(
                            border: InputBorder.none,
                            labelText: 'Default Toolbar Action',
                            labelStyle: TextStyle(color: AppTheme.textSecondary),
                          ),
                          items: const [
                            DropdownMenuItem(value: 'FIX', child: Text('✨ Fix (Grammar & Typos)')),
                            DropdownMenuItem(value: 'IMPROVE', child: Text('Improve (Clarity)')),
                            DropdownMenuItem(value: 'PROFESSIONAL', child: Text('Professional (Formal)')),
                            DropdownMenuItem(value: 'FRIENDLY', child: Text('Friendly (Casual)')),
                            DropdownMenuItem(value: 'SHORTEN', child: Text('Shorten (Concise)')),
                            DropdownMenuItem(value: 'EXPAND', child: Text('Expand (Elaborate)')),
                          ],
                          onChanged: (val) {
                            if (val != null) controller.setDefaultAction(val);
                          },
                        ),
                      ),
                    ),

                    const SizedBox(height: 24),
                    // About
                    const Center(
                      child: Column(
                        children: [
                          Text(
                            'TypeFix AI v1.0.0',
                            style: TextStyle(fontSize: 12, color: AppTheme.textSecondary),
                          ),
                          SizedBox(height: 4),
                          Text(
                            '100% Offline • Zero Telemetry • On-Device AI',
                            style: TextStyle(fontSize: 11, color: AppTheme.textSecondary),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
        );
      },
    );
  }
}
