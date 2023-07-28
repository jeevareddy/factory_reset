import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.title});

  final String title;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const channel = "com.example.factory_reset/main";

  var platform = const MethodChannel(channel);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton.icon(
              onPressed: () async {
                try {
                  print("Factory Reset pressed");
                  await platform.invokeMethod('reset');
                  print("Factory Reset done");
                } on PlatformException catch (e) {
                  print("Failed to reset: '${e.message}'.");
                }
              },
              icon: const Icon(Icons.lock_reset),
              label: const Text("Factory Reset"),
            ),
          ],
        ),
      ),
    );
  }
}
