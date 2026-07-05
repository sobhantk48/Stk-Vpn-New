package com.v2ray.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.v2ray.app.navigation.AppNavigation
import com.v2ray.app.ui.theme.V2rayAppTheme
import com.v2ray.app.utils.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.initialize(this)
        Logger.writeLog("MainActivity started")

        requestPermissions()

        setContent {
            V2rayAppTheme {
                AppNavigation()
            }
        }
        Logger.writeLog("Compose loaded")
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        // مجوز ذخیره‌سازی (Android 9 و پایین‌تر)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // مجوز نوتیفیکیشن (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // مجوز دوربین (برای اسکن QR)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        // مجوز مدیریت حافظه برای Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                // این مجوز باید از طریق Intent درخواست شود
                Logger.writeLog("MANAGE_EXTERNAL_STORAGE not granted")
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            grantResults.forEachIndexed { index, result ->
                if (result == PackageManager.PERMISSION_GRANTED) {
                    Logger.writeLog("Permission granted: ${permissions[index]}")
                    Toast.makeText(this, "Permission granted: ${permissions[index]}", Toast.LENGTH_SHORT).show()
                } else {
                    Logger.writeLog("Permission denied: ${permissions[index]}")
                }
            }
        }
    }
}
