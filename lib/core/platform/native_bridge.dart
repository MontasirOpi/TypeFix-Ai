import 'package:flutter/services.dart';
import '../constants/app_constants.dart';

class NativeBridge {
  static const MethodChannel _channel = MethodChannel(AppConstants.methodChannelName);

  static Future<bool> isKeyboardEnabled() async {
    try {
      final bool? result = await _channel.invokeMethod<bool>('isKeyboardEnabled');
      return result ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<bool> isKeyboardSelected() async {
    try {
      final bool? result = await _channel.invokeMethod<bool>('isKeyboardSelected');
      return result ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<bool> openKeyboardSettings() async {
    try {
      final bool? result = await _channel.invokeMethod<bool>('openKeyboardSettings');
      return result ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<bool> openInputMethodPicker() async {
    try {
      final bool? result = await _channel.invokeMethod<bool>('openInputMethodPicker');
      return result ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<List<Map<String, dynamic>>> getInstalledModels() async {
    try {
      final List<dynamic>? result = await _channel.invokeListMethod('getInstalledModels');
      if (result == null) return [];
      return result.map((e) => Map<String, dynamic>.from(e as Map)).toList();
    } catch (_) {
      return [];
    }
  }

  static Future<bool> deleteModel(String modelId) async {
    try {
      final bool? result = await _channel.invokeMethod<bool>('deleteModel', {'modelId': modelId});
      return result ?? false;
    } catch (_) {
      return false;
    }
  }

  static Future<Map<String, dynamic>> getPrivacyStatus() async {
    try {
      final Map<dynamic, dynamic>? result = await _channel.invokeMethod('getPrivacyStatus');
      if (result == null) return {};
      return Map<String, dynamic>.from(result);
    } catch (_) {
      return {
        'isOffline': true,
        'noCloudApi': true,
        'noKeystrokeLogging': true,
        'ignorePasswordFields': true,
        'historyDefaultOff': true,
      };
    }
  }

  static Future<Map<String, dynamic>> getSettings() async {
    try {
      final Map<dynamic, dynamic>? result = await _channel.invokeMethod('getSettings');
      if (result == null) return {};
      return Map<String, dynamic>.from(result);
    } catch (_) {
      return {};
    }
  }

  static Future<bool> updateSettings(String key, dynamic value) async {
    try {
      final bool? result = await _channel.invokeMethod<bool>('updateSettings', {
        'key': key,
        'value': value,
      });
      return result ?? false;
    } catch (_) {
      return false;
    }
  }
}
