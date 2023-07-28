import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

enum AndroidMethodsMain {
  reset,
  enablePermission,
  setMaxPasswordRetries,
  getMaxPasswordRetries,
  isDeviceSecured,
}

class CustomMethodChannelController {
  static const channel = "com.example.factory_reset/main";

  final _platform = const MethodChannel(channel);

  dynamic invokeMethod(AndroidMethodsMain method, {Map? args}) async {
    try {
      return (await _platform.invokeMethod(method.name, args));
    } on PlatformException catch (e) {
      debugPrint("Platform Exception has occurred: '${e.message}'.");
    }
    return null;
  }
}
