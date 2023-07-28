package com.example.factory_reset

import android.app.KeyguardManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.factory_reset/main"
    private lateinit var mDPM: DevicePolicyManager
    private lateinit var mDeviceAdminComponentName: ComponentName

    private val REQUEST_CODE_ENABLE_ADMIN = 1
    private val currentAPIVersion = Build.VERSION.SDK_INT
    private val TAG = "Method Channel"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mDeviceAdminComponentName = ComponentName(this, DeviceAdmin::class.java)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call,
                                 result ->
            Log.d(TAG, "Method Call Name: ${call.method}")
            try {
                when (call.method) {
                    "isDeviceSecured" -> {
                        val deviceStatus = isDeviceSecured()
                        result.success(deviceStatus)
                    }

                    "setMaxPasswordRetries" -> {
                        val maxRetries = call.argument("maxRetries") as Int?
                        setMaxFailedPasswordsForWipe(maxRetries)
                        result.success(true)
                    }

                    "getMaxPasswordRetries" -> {
                        val currentMaxRetries = getMaxFailedPasswordsForWipe()
                        result.success(currentMaxRetries)
                    }

                    "enablePermission" -> {
                        enablePermission()
                        result.success(true)
                    }

                    "reset" -> {
                        reset()
                        result.success(true)
                    }

                    else -> {
                        result.notImplemented()
                    }
                }
            } catch (e: Error) {
                Log.e(TAG, e.toString())
                result.error("Platform Error", e.message, null);
            }
        }
    }

    private fun isActiveAdmin(): Boolean = mDPM.isAdminActive(mDeviceAdminComponentName)

    private fun isDeviceSecured(): Boolean {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager //api 16+
        return if (currentAPIVersion >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceSecure
        } else keyguardManager.isKeyguardSecure
    }

    private fun setMaxFailedPasswordsForWipe(retries: Int?) {
        if (retries == null || retries <= 0) return
        enablePermission()
        mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdminComponentName, retries)
    }

    private fun getMaxFailedPasswordsForWipe(): Int {
//        enablePermission()
        return mDPM.getMaximumFailedPasswordsForWipe(mDeviceAdminComponentName)
    }

    private fun enablePermission() {
        if (currentAPIVersion >= Build.VERSION_CODES.FROYO) {
            // 2.2+
            Log.d(TAG, "Is Admin Active: ${isActiveAdmin()}")
            if (!isActiveAdmin()) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminComponentName)
                intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Process will remove user installed applications, settings, wallpaper and sound settings. Are you sure you want to wipe device?"
                )
                startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
            }
        }
    }

    private fun reset() {
        if (currentAPIVersion >= Build.VERSION_CODES.FROYO) {
            // 2.2+
            Log.d(TAG, "Is Admin Active: ${isActiveAdmin()}")
            if (!isActiveAdmin()) {
                enablePermission()
            } else {
                try {
                    mDPM.wipeData(0)
                } catch (e: Error) {
                    Log.e(TAG, e.toString())
                }
            }
        } else {
            // 2.1
            try {
                val foreignContext =
                    createPackageContext(
                        "com.android.settings",
                        CONTEXT_IGNORE_SECURITY or CONTEXT_INCLUDE_CODE
                    )
                val yourClass =
                    foreignContext.classLoader.loadClass("com.android.settings.MasterClear")
                val i = Intent(foreignContext, yourClass)
                startActivityForResult(i, REQUEST_CODE_ENABLE_ADMIN)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                    context,
                    context.getString(R.string.admin_receiver_status_enabled),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                Toast.makeText(context, "Admin Request denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class DeviceAdmin : DeviceAdminReceiver() {
    private val TAG = "Device Admin"

    private fun showToast(context: Context, msg: String) {
        Log.d(TAG, msg)
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onEnabled(context: Context, intent: Intent) =
        showToast(context, context.getString(R.string.admin_receiver_status_enabled))

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        context.getString(R.string.admin_receiver_status_disable_warning)

    override fun onDisabled(context: Context, intent: Intent) =
        showToast(context, context.getString(R.string.admin_receiver_status_disable_warning))
}
