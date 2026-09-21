package com.typefix.ai

import com.typefix.ai.bridge.FlutterBridge
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val channel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            FlutterBridge.CHANNEL_NAME
        )
        channel.setMethodCallHandler(FlutterBridge(context))
    }
}
