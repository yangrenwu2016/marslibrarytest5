import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:marslibrary/marslibrary.dart';

void main() {
  const MethodChannel channel = MethodChannel('marslibrary');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await Marslibrary.platformVersion, '42');
  });
}
