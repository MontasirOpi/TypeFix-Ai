import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:typefix/app/app.dart';
import 'package:typefix/core/storage/storage_service.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  final calls = <String>[];
  setUp(() async {
    calls.clear();
    SharedPreferences.setMockInitialValues({});
    await StorageService.init();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      const MethodChannel('com.typefix.ai/bridge'), (call) async {
        calls.add(call.method);
        if (call.method == 'assistantEnabled') return false;
        return true;
      });
  });
  testWidgets('Assistant setup keeps existing keyboard and asks before opening access settings', (tester) async {
    await tester.pumpWidget(const TypeFixApp());
    await tester.pumpAndSettle();
    expect(find.text('Keep your keyboard'), findsOneWidget);
    expect(find.text('Enable Keyboard'), findsNothing);
    await tester.tap(find.text('Set up floating voice'));
    await tester.pumpAndSettle();
    expect(find.text('Allow focused-field access?'), findsOneWidget);
    expect(calls, isNot(contains('openAssistantSettings')));
    await tester.tap(find.text('Cancel'));
    await tester.pumpAndSettle();
    expect(calls, isNot(contains('openAssistantSettings')));
    await tester.tap(find.text('Set up floating voice'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Agree and open Settings'));
    await tester.pumpAndSettle();
    expect(calls, contains('openAssistantSettings'));
  });
}
