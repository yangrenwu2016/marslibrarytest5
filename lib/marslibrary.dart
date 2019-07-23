import 'dart:async';

import 'package:flutter/services.dart';
import 'package:path/path.dart';

class Marslibrary {
  static const MethodChannel _channel =
      const MethodChannel('marslibrary');

  static Future<String> get platformVersion async{
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
  static Future<String>  sayHello(String  context) async{
    final String version = await _channel.invokeMethod('sayHello',<String,dynamic>{'context':context});
    return version;
  }
}
