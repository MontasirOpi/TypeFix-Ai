import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../../app/theme.dart';
import 'model_controller.dart';

class ModelScreen extends StatelessWidget {
  const ModelScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GetBuilder<ModelController>(
      init: ModelController(),
      builder: (controller) {
        return Scaffold(
          appBar: AppBar(
            title: const Text('Local AI Models'),
          ),
          body: controller.isLoading
              ? const Center(child: CircularProgressIndicator())
              : Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'On-Device AI Models',
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: AppTheme.textSecondary,
                        ),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'Models execute locally on your device using ONNX Runtime Mobile. No data is sent over the network.',
                        style: TextStyle(fontSize: 12, color: AppTheme.textSecondary),
                      ),
                      const SizedBox(height: 16),
                      Expanded(
                        child: ListView.builder(
                          itemCount: controller.models.length,
                          itemBuilder: (context, index) {
                            final model = controller.models[index];
                            return Card(
                              margin: const EdgeInsets.only(bottom: 12),
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        Icon(
                                          model.isInstalled
                                              ? Icons.check_circle
                                              : Icons.cloud_download_outlined,
                                          color: model.isInstalled
                                              ? AppTheme.successGreen
                                              : AppTheme.textSecondary,
                                          size: 20,
                                        ),
                                        const SizedBox(width: 8),
                                        Text(
                                          model.name,
                                          style: const TextStyle(
                                            fontSize: 16,
                                            fontWeight: FontWeight.w600,
                                            color: AppTheme.textPrimary,
                                          ),
                                        ),
                                        const Spacer(),
                                        Container(
                                          padding: const EdgeInsets.symmetric(
                                              horizontal: 8, vertical: 3),
                                          decoration: BoxDecoration(
                                            color: model.isInstalled
                                                ? AppTheme.successGreen.withAlpha(30)
                                                : AppTheme.darkBorder,
                                            borderRadius: BorderRadius.circular(6),
                                          ),
                                          child: Text(
                                            model.isInstalled
                                                ? 'INSTALLED'
                                                : 'COMING SOON',
                                            style: TextStyle(
                                              fontSize: 11,
                                              fontWeight: FontWeight.bold,
                                              color: model.isInstalled
                                                  ? AppTheme.successGreen
                                                  : AppTheme.textSecondary,
                                            ),
                                          ),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 8),
                                    Text(
                                      model.description,
                                      style: const TextStyle(
                                        fontSize: 13,
                                        color: AppTheme.textSecondary,
                                      ),
                                    ),
                                    const SizedBox(height: 12),
                                    Row(
                                      children: [
                                        Text(
                                          'Size: ${model.sizeFormatted}',
                                          style: const TextStyle(
                                            fontSize: 12,
                                            color: AppTheme.textSecondary,
                                          ),
                                        ),
                                        const SizedBox(width: 14),
                                        Text(
                                          'Language: ${model.language}',
                                          style: const TextStyle(
                                            fontSize: 12,
                                            color: AppTheme.textSecondary,
                                          ),
                                        ),
                                        const Spacer(),
                                        if (model.isInstalled && model.id != 'en_grammar_v1')
                                          OutlinedButton(
                                            style: OutlinedButton.styleFrom(
                                              foregroundColor: Colors.redAccent,
                                            ),
                                            onPressed: () => controller.deleteModel(model.id),
                                            child: const Text('Delete'),
                                          ),
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    ],
                  ),
                ),
        );
      },
    );
  }
}
