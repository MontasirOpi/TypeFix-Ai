import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:typefix/app/app.dart';
import 'package:typefix/core/storage/storage_service.dart';

void main() {
  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    await StorageService.init();
  });

  testWidgets('TypeFixApp smoke test - displays onboarding', (WidgetTester tester) async {
    await tester.pumpWidget(const TypeFixApp());
    await tester.pumpAndSettle();

    // Verify app title and onboarding contents
    expect(find.text('TypeFix AI'), findsOneWidget);
    expect(find.text('Offline AI Writing Assistant'), findsOneWidget);
    expect(find.text('Continue to App'), findsOneWidget);
  });
}
