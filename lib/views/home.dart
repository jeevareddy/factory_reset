import 'package:factory_reset/controller/method_channel_controller.dart';
import 'package:flutter/material.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.title});

  final String title;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  double _maxRetries = 0.0;
  bool _isDeviceSecured = true;

  final _methodChannel = CustomMethodChannelController();

  @override
  void initState() {
    super.initState();
    fetchMaxPasswordRetries();
    fetchDeviceStatus();
  }

  void fetchDeviceStatus() async {
    final deviceStatus =
        await _methodChannel.invokeMethod(AndroidMethodsMain.isDeviceSecured);

    debugPrint("Device Security Status: $deviceStatus");

    if (mounted && deviceStatus != null && deviceStatus is bool) {
      setState(() => _isDeviceSecured = deviceStatus);
    }
  }

  void fetchMaxPasswordRetries() async {
    final currentMaxReties = await _methodChannel
        .invokeMethod(AndroidMethodsMain.getMaxPasswordRetries);

    debugPrint("Current Max Retries: $currentMaxReties");
    final parsedValue = double.tryParse(currentMaxReties.toString());
    if (mounted && parsedValue != null && parsedValue > 0.0) {
      setState(() => _maxRetries = parsedValue);
    }
  }

  void setMaxPasswordRetries() async {
    final success = await _methodChannel.invokeMethod(
        AndroidMethodsMain.setMaxPasswordRetries,
        args: {'maxRetries': _maxRetries.toInt()});
    if ( success ??  false) {
      debugPrint("Max Password Retries has been set: $_maxRetries ");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          children: [
            if (!_isDeviceSecured)
              const MaterialBanner(
                content: Text("Your device is not protected by a screen lock."),
                actions: [
                  Icon(
                    Icons.warning,
                    size: 32.0,
                    color: Colors.red,
                  )
                ],
              ),
            Expanded(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: <Widget>[
                  ElevatedButton.icon(
                    onPressed: () async {
                      final success = await _methodChannel
                          .invokeMethod(AndroidMethodsMain.enablePermission);
                      debugPrint("Admin Permission Status: $success");
                    },
                    icon: const Icon(Icons.lock_reset),
                    label: const Text("Enable Permission"),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Column(
                       crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.only(left:16.0),
                          child: Text("Unlock Attempts",
                              style: Theme.of(context).textTheme.titleLarge),
                        ),
                        Column(
                          children: [
                            Padding(
                              padding: const EdgeInsets.all(4.0),
                              child: Row(
                                children: [
                                  Expanded(
                                    child: Slider(
                                      value: _maxRetries,
                                      min: 0,
                                      max: 10,
                                      divisions: 10,
                                      onChanged: (val) => setState(() =>
                                          _maxRetries = val.round() * 1.0),
                                    ),
                                  ),
                                  Text(
                                    "$_maxRetries",
                                    style:
                                        Theme.of(context).textTheme.titleLarge,
                                  )
                                ],
                              ),
                            ),
                            ElevatedButton.icon(
                              onPressed: setMaxPasswordRetries,
                              icon: const Icon(Icons.restore_page),
                              label: const Text("Enable"),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  ElevatedButton.icon(
                    onPressed: () async {
                      await _methodChannel
                          .invokeMethod(AndroidMethodsMain.reset);
                    },
                    icon: const Icon(Icons.lock_reset),
                    label: const Text("Factory Reset Now"),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
