package com.v2ray.app.v2ray

import android.content.Context
import com.v2ray.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class V2RayManager(private val context: Context) {
    private var process: Process? = null
    private var running = false

    suspend fun startV2Ray(configJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (running) return@withContext Result.success(Unit)

            val appDir = context.filesDir
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            // کپی فایل‌ها از assets به appDir
            copyAssetToFile("xray", File(appDir, "xray"))
            copyAssetToFile("geoip.dat", File(appDir, "geoip.dat"))
            copyAssetToFile("geosite.dat", File(appDir, "geosite.dat"))

            val xrayFile = File(appDir, "xray")
            if (!xrayFile.exists()) {
                return@withContext Result.failure(Exception("Xray binary not found in ${appDir.absolutePath}"))
            }

            // تنظیم مجوز اجرا (با دو روش برای اطمینان)
            try {
                // روش اول: setExecutable
                val chmodSuccess = xrayFile.setExecutable(true, false)
                Logger.writeLog("setExecutable result: $chmodSuccess for ${xrayFile.absolutePath}")

                // روش دوم: chmod از طریق shell (برای اطمینان بیشتر)
                val chmodProcess = Runtime.getRuntime().exec(arrayOf("chmod", "755", xrayFile.absolutePath))
                val exitCode = chmodProcess.waitFor()
                Logger.writeLog("chmod 755 exit code: $exitCode for ${xrayFile.absolutePath}")

                if (exitCode != 0) {
                    Logger.writeLog("chmod failed with exit code $exitCode, but setExecutable was already called")
                }
            } catch (e: Exception) {
                Logger.writeError("Failed to set executable permissions", e)
                // حتی با خطا هم ادامه می‌دیم
            }

            // ذخیره کانفیگ در appDir
            val configFile = File(appDir, "v2ray_config.json")
            configFile.writeText(configJson)

            // ساخت دستور اجرا
            val command = arrayOf(
                "sh",
                "-c",
                "${xrayFile.absolutePath} run -config ${configFile.absolutePath} -format json"
            )

            Logger.writeLog("Starting Xray with command: ${command.joinToString(" ")}")

            val processBuilder = ProcessBuilder(*command)
            processBuilder.redirectErrorStream(true)
            processBuilder.directory(appDir)
            processBuilder.environment()["PATH"] = "${System.getenv("PATH")}:${appDir.absolutePath}"

            process = processBuilder.start()

            // خواندن خروجی استاندارد
            Thread {
                try {
                    val reader = process?.inputStream?.bufferedReader()
                    reader?.forEachLine { line ->
                        Logger.writeLog("[Xray] $line")
                    }
                } catch (_: Exception) { }
            }.start()

            // خواندن خطاها
            Thread {
                try {
                    val reader = process?.errorStream?.bufferedReader()
                    reader?.forEachLine { line ->
                        Logger.writeLog("[Xray-ERR] $line")
                    }
                } catch (_: Exception) { }
            }.start()

            running = true
            Logger.writeLog("Xray started successfully from ${appDir.absolutePath}")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.writeError("Xray start failed", e)
            Result.failure(e)
        }
    }

    private fun copyAssetToFile(assetName: String, targetFile: File) {
        try {
            if (targetFile.exists()) return
            context.assets.open(assetName).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            Logger.writeLog("Copied asset: $assetName to ${targetFile.absolutePath}")
        } catch (e: Exception) {
            Logger.writeError("Failed to copy asset: $assetName", e)
        }
    }

    suspend fun stopV2Ray(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            process?.destroy()
            process?.waitFor()
            process = null
            running = false
            Logger.writeLog("Xray stopped")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.writeError("Xray stop failed", e)
            Result.failure(e)
        }
    }

    fun isRunning(): Boolean = running

    fun getStats(): V2RayStats {
        return V2RayStats(0, 0, 0)
    }
}

data class V2RayStats(
    val uplink: Long,
    val downlink: Long,
    val connectionCount: Int
)
