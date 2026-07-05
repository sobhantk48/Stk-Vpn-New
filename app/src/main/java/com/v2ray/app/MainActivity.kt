package com.v2ray.app

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.v2ray.app.navigation.AppNavigation
import com.v2ray.app.ui.theme.V2rayAppTheme
import com.v2ray.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Launcher برای دریافت مجوز VPN
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // مجوز گرفته شد - ادامه‌ی کار
        } else {
            // کاربر مجوز نداد
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            V2rayAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = hiltViewModel()

                    // پاس دادن launcher به ViewModel
                    LaunchedEffect(Unit) {
                        viewModel.setVpnPermissionLauncher { intent ->
                            vpnPermissionLauncher.launch(intent)
                        }
                    }

                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // اگر مجوز قبلاً گرفته شده، می‌تونیم ادامه بدیم
    }
}
