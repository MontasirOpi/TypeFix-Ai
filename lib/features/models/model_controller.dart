import 'package:get/get.dart';
import '../../services/model_service.dart';

class ModelController extends GetxController {
  final ModelService modelService = ModelService();

  bool isLoading = true;
  List<ModelInfo> models = [];

  @override
  void onInit() {
    super.onInit();
    loadModels();
  }

  Future<void> loadModels() async {
    isLoading = true;
    update();

    models = await modelService.getModels();

    isLoading = false;
    update();
  }

  Future<void> deleteModel(String modelId) async {
    final success = await modelService.deleteModel(modelId);
    if (success) {
      await loadModels();
    }
  }
}
