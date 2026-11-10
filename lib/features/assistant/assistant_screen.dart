import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class AssistantScreen extends StatefulWidget {
  const AssistantScreen({super.key, this.onContinue});
  final VoidCallback? onContinue;
  @override
  State<AssistantScreen> createState() => _AssistantScreenState();
}

class _AssistantScreenState extends State<AssistantScreen> with WidgetsBindingObserver {
  static const bridge = MethodChannel('com.typefix.ai/bridge');
  bool enabled = false;
  String? error;
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    refresh();
  }
  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) refresh();
  }
  Future<void> refresh() async {
    try {
      final value = await bridge.invokeMethod<bool>('assistantEnabled') ?? false;
      if (mounted) setState(() => enabled = value);
    } catch (_) {
      if (mounted) setState(() => error = 'Android assistant service is unavailable.');
    }
  }
  Future<void> settings() async {
    final agreed = await showDialog<bool>(context: context, builder: (context) => AlertDialog(
      title: const Text('Allow focused-field access?'),
      content: const SingleChildScrollView(child: Text(
        'TypeFix uses Android Accessibility to detect when your keyboard is open and '
        'show a floating microphone. When you tap Speak, it reads the focused '
        'non-password field and its cursor so it can insert your transcript there. '
        'Android grants broader screen-content access, but TypeFix only uses the '
        'focused editable field. Text and recordings are not saved or uploaded. '
        'Undo is kept in memory until the keyboard closes or the field changes. '
        'You can turn this off in Android Accessibility settings at any time. '
        'Google may restrict installation or enabling accessibility for sideloaded apps. '
        'Do not disable Play Protect.')),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
        FilledButton(onPressed: () => Navigator.pop(context, true), child: const Text('Agree and open Settings')),
      ],
    ));
    if (agreed == true) await invoke('openAssistantSettings');
  }
  Future<void> invoke(String method) async {
    try { await bridge.invokeMethod(method); }
    catch (_) { if (mounted) setState(() => error = 'Could not open Android settings.'); }
  }
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('TypeFix AI'), actions: [
      IconButton(onPressed: refresh, icon: const Icon(Icons.refresh), tooltip: 'Refresh status'),
    ]),
    body: ListView(padding: const EdgeInsets.all(24), children: [
      const Icon(Icons.mic_rounded, size: 64, color: Colors.lightBlueAccent),
      const SizedBox(height: 16),
      const Text('Keep your keyboard', style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
      const SizedBox(height: 12),
      const Text('A voice helper beside Gboard, Samsung Keyboard, or your preferred keyboard. '
          'TypeFix does not replace your keyboard.'),
      const SizedBox(height: 24),
      Card(child: Padding(padding: const EdgeInsets.all(18), child: Column(
        crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(enabled ? 'Floating voice is enabled' : 'Enable floating voice',
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
          const SizedBox(height: 10),
          const Text('In Android Accessibility, choose TypeFix floating voice. '
              'This lets the widget follow the keyboard and insert text into the focused field.'),
          const SizedBox(height: 14),
          FilledButton(onPressed: settings,
              child: Text(enabled ? 'Manage or disable access' : 'Set up floating voice')),
          const SizedBox(height: 8),
          OutlinedButton(onPressed: () => invoke('requestMicrophone'), child: const Text('Allow microphone')),
        ],
      ))),
      const SizedBox(height: 24),
      const Text('Speak anywhere you write', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w600)),
      const SizedBox(height: 12),
      const Text('1. Open a message or note using your usual keyboard.\n\n'
          '2. Tap Speak on the floating widget.\n\n'
          '3. Speak, then tap Finish. Your words go into that field at the cursor.\n\n'
          'Cancel discards a recording. Undo restores the last insertion while the field stays unchanged.'),
      const SizedBox(height: 24),
      const Text('Offline recognition needs Android 12+ and a supported installed language pack. '
          'Some apps do not expose editable fields to Accessibility. Password fields are excluded. '
          'No clipboard access, cloud recognition, or background recording.'),
      if (error != null) Padding(padding: const EdgeInsets.only(top: 16), child: Text(error!)),
      if (widget.onContinue != null) Padding(padding: const EdgeInsets.only(top: 24),
          child: FilledButton(onPressed: widget.onContinue, child: const Text('Continue to App'))),
    ]),
  );
}
