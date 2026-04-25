import 'package:flutter_test/flutter_test.dart';
import 'package:smart_parking_mobile/main.dart';

void main() {
  testWidgets('App smoke test — renders without crash', (WidgetTester tester) async {
    await tester.pumpWidget(const SmartParkingApp());
    expect(find.byType(SmartParkingApp), findsOneWidget);
  });
}
