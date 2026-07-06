package com.v2ray.app.desync

import java.io.OutputStream
import java.net.Socket
import kotlinx.coroutines.*

/**
 * مدیریت تکه‌تکه کردن ClientHello برای دور زدن DPI
 * معادل desync.FragmentWrites و desync.WrapConn
 */
object FragmentManager {

    data class Config(
        val enableFragment: Boolean = false,
        val sniChunk: Int = 3,          // تعداد بایت‌های SNI در هر تکه (0 = کل SNI یکجا)
        val fragmentDelay: Long = 500   // میلی‌ثانیه بین تکه‌ها
    )

    /**
     * پیدا کردن SNI در ClientHello
     * معادل FindSNI در Go
     */
    fun findSNI(data: ByteArray): Triple<Int, Int, String>? {
        if (!isClientHello(data)) return null

        // رد شدن از header record (5) + handshake header (4)
        var p = 5 + 4
        if (p + 2 + 32 > data.size) return null

        p += 2 + 32 // client_version + random
        if (p >= data.size) return null

        val sidLen = data[p].toInt() and 0xFF
        p += 1 + sidLen
        if (p + 2 > data.size) return null

        val csLen = ((data[p].toInt() and 0xFF) shl 8) or (data[p + 1].toInt() and 0xFF)
        p += 2 + csLen
        if (p >= data.size) return null

        val compLen = data[p].toInt() and 0xFF
        p += 1 + compLen
        if (p + 2 > data.size) return null

        val extTotal = ((data[p].toInt() and 0xFF) shl 8) or (data[p + 1].toInt() and 0xFF)
        p += 2
        val extEnd = minOf(p + extTotal, data.size)

        while (p + 4 <= extEnd) {
            val etype = ((data[p].toInt() and 0xFF) shl 8) or (data[p + 1].toInt() and 0xFF)
            val elen = ((data[p + 2].toInt() and 0xFF) shl 8) or (data[p + 3].toInt() and 0xFF)
            val body = p + 4
            if (body + elen > data.size) break

            if (etype == 0x0000) { // server_name
                var q = body
                if (q + 2 > data.size) break
                q += 2 // server_name_list length
                if (q + 3 > data.size) break
                // name_type(1) + name_length(2)
                val nameLen = ((data[q + 1].toInt() and 0xFF) shl 8) or (data[q + 2].toInt() and 0xFF)
                val start = q + 3
                val end = start + nameLen
                if (end > data.size) break
                val host = String(data.sliceArray(start until end))
                return Triple(start, end, host)
            }
            p = body + elen
        }
        return null
    }

    private fun isClientHello(data: ByteArray): Boolean {
        return data.size >= 6 &&
                data[0] == 0x16.toByte() &&
                data[1] == 0x03.toByte() &&
                data[5] == 0x01.toByte()
    }

    /**
     * تکه‌تکه کردن ClientHello بر اساس SNI
     * معادل FragmentWrites در Go
     */
    fun fragmentWrites(data: ByteArray, sniChunk: Int): List<ByteArray> {
        val sniInfo = findSNI(data)
        if (sniInfo == null) {
            // اگر SNI پیدا نشد، از وسط تقسیم کن
            if (data.size < 2) return listOf(data)
            val mid = data.size / 2
            return listOf(
                data.sliceArray(0 until mid),
                data.sliceArray(mid until data.size)
            )
        }

        val (start, end, _) = sniInfo
        val bounds = mutableSetOf(start, end)
        if (sniChunk > 0) {
            var i = start + sniChunk
            while (i < end) {
                bounds.add(i)
                i += sniChunk
            }
        }

        val cuts = mutableListOf(0, data.size)
        cuts.addAll(bounds.filter { it in 1 until data.size })
        cuts.sort()

        val result = mutableListOf<ByteArray>()
        for (i in 0 until cuts.size - 1) {
            val from = cuts[i]
            val to = cuts[i + 1]
            if (to > from) {
                result.add(data.sliceArray(from until to))
            }
        }
        return if (result.isEmpty()) listOf(data) else result
    }

    /**
     * تابع ارسال با تکه‌تکه کردن
     * برای استفاده در Socket
     */
    suspend fun writeWithFragment(
        outputStream: OutputStream,
        data: ByteArray,
        config: Config
    ): Int = withContext(Dispatchers.IO) {
        if (!config.enableFragment) {
            outputStream.write(data)
            outputStream.flush()
            return@withContext data.size
        }

        val chunks = fragmentWrites(data, config.sniChunk)
        var total = 0
        for ((index, chunk) in chunks.withIndex()) {
            outputStream.write(chunk)
            outputStream.flush()
            total += chunk.size
            if (index < chunks.size - 1 && config.fragmentDelay > 0) {
                delay(config.fragmentDelay)
            }
        }
        total
    }
}
