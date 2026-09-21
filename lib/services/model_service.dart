
import '../core/platform/native_bridge.dart';

class ModelInfo {
  final String id;
  final String name;
  final String language;
  final String version;
  final int sizeBytes;
  final bool isInstalled;
  final String description;

  ModelInfo({
    required this.id,
    required this.name,
    required this.language,
    required this.version,
    required this.sizeBytes,
    required this.isInstalled,
    required this.description,
  });

  String get sizeFormatted {
    final mb = sizeBytes / (1024 * 1024);
    return '${mb.toStringAsFixed(1)} MB';
  }

  factory ModelInfo.fromMap(Map<String, dynamic> map) {
    return ModelInfo(
      id: map['id'] as String? ?? '',
      name: map['name'] as String? ?? '',
      language: map['language'] as String? ?? '',
      version: map['version'] as String? ?? '',
      sizeBytes: (map['sizeBytes'] as num?)?.toInt() ?? 0,
      isInstalled: map['isInstalled'] as bool? ?? false,
      description: map['description'] as String? ?? '',
    );
  }
}

class ModelService {
  Future<List<ModelInfo>> getModels() async {
    final list = await NativeBridge.getInstalledModels();
    return list.map((m) => ModelInfo.fromMap(m)).toList();
  }

  Future<bool> deleteModel(String modelId) async {
    return await NativeBridge.deleteModel(modelId);
  }
}
