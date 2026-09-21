import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../constants/app_constants.dart';

class HistoryItem {
  final String id;
  final String originalText;
  final String correctedText;
  final String action;
  final DateTime timestamp;

  HistoryItem({
    required this.id,
    required this.originalText,
    required this.correctedText,
    required this.action,
    required this.timestamp,
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'originalText': originalText,
    'correctedText': correctedText,
    'action': action,
    'timestamp': timestamp.toIso8601String(),
  };

  factory HistoryItem.fromJson(Map<String, dynamic> json) => HistoryItem(
    id: json['id'] as String,
    originalText: json['originalText'] as String,
    correctedText: json['correctedText'] as String,
    action: json['action'] as String,
    timestamp: DateTime.parse(json['timestamp'] as String),
  );
}

class StorageService {
  static SharedPreferences? _prefs;

  static Future<void> init() async {
    _prefs ??= await SharedPreferences.getInstance();
  }

  static bool isOnboardingCompleted() {
    return _prefs?.getBool(AppConstants.prefOnboardingCompleted) ?? false;
  }

  static Future<void> setOnboardingCompleted(bool completed) async {
    await _prefs?.setBool(AppConstants.prefOnboardingCompleted, completed);
  }

  // History is OFF by default as required by specification
  static bool isHistoryEnabled() {
    return _prefs?.getBool(AppConstants.prefHistoryEnabled) ?? false;
  }

  static Future<void> setHistoryEnabled(bool enabled) async {
    await _prefs?.setBool(AppConstants.prefHistoryEnabled, enabled);
  }

  static List<HistoryItem> getHistory() {
    if (!isHistoryEnabled()) return [];
    final String? raw = _prefs?.getString('local_history');
    if (raw == null || raw.isEmpty) return [];
    try {
      final List<dynamic> list = jsonDecode(raw) as List<dynamic>;
      return list.map((e) => HistoryItem.fromJson(e as Map<String, dynamic>)).toList();
    } catch (_) {
      return [];
    }
  }

  static Future<void> addHistoryItem(HistoryItem item) async {
    if (!isHistoryEnabled()) return;
    final history = getHistory();
    history.insert(0, item);
    // Retain maximum 100 entries locally
    if (history.length > 100) history.removeLast();
    final jsonStr = jsonEncode(history.map((e) => e.toJson()).toList());
    await _prefs?.setString('local_history', jsonStr);
  }

  static Future<void> deleteHistoryItem(String id) async {
    final history = getHistory();
    history.removeWhere((item) => item.id == id);
    final jsonStr = jsonEncode(history.map((e) => e.toJson()).toList());
    await _prefs?.setString('local_history', jsonStr);
  }

  static Future<void> clearHistory() async {
    await _prefs?.remove('local_history');
  }
}
