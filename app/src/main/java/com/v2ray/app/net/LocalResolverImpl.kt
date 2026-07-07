package com.v2ray.app.net

import android.net.DnsResolver
import android.os.Build
import android.os.CancellationSignal
import android.system.ErrnoException
import androidx.annotation.RequiresApi
import com.v2ray.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import libcore.ExchangeContext
import libcore.LocalDNSTransport
import java.net.InetAddress
import java.net.UnknownHostException

object LocalResolverImpl : LocalDNSTransport {

    private const val RCODE_NXDOMAIN = 3

    override fun raw(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    override fun networkHandle(): Long {
        // استفاده از شبکه پیش‌فرض
        return 0L
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun exchange(ctx: ExchangeContext, message: ByteArray) {
        val signal = CancellationSignal()
        ctx.onCancel { signal.cancel() }

        val callback = object : DnsResolver.Callback<ByteArray> {
            override fun onAnswer(answer: ByteArray, rcode: Int) {
                ctx.rawSuccess(answer)
            }

            override fun onError(error: DnsResolver.DnsException) {
                val cause = error.cause
                if (cause is ErrnoException) {
                    ctx.errnoCode(cause.errno)
                } else {
                    Logger.e("DNS exchange error", error)
                    ctx.errnoCode(114514)
                }
            }
        }

        DnsResolver.getInstance().rawQuery(
            null,
            message,
            DnsResolver.FLAG_NO_RETRY,
            Dispatchers.IO.asExecutor(),
            signal,
            callback
        )
    }

    override fun lookup(ctx: ExchangeContext, network: String, domain: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val signal = CancellationSignal()
            ctx.onCancel { signal.cancel() }

            val callback = object : DnsResolver.Callback<Collection<InetAddress>> {
                override fun onAnswer(answer: Collection<InetAddress>, rcode: Int) {
                    try {
                        if (rcode == 0) {
                            ctx.success(answer.mapNotNull { it.hostAddress }.joinToString("\n"))
                        } else {
                            ctx.errorCode(rcode)
                        }
                    } catch (e: Exception) {
                        Logger.e("DNS lookup answer error", e)
                        ctx.errnoCode(114514)
                    }
                }

                override fun onError(error: DnsResolver.DnsException) {
                    try {
                        val cause = error.cause
                        if (cause is ErrnoException) {
                            ctx.errnoCode(cause.errno)
                        } else {
                            Logger.e("DNS lookup error", error)
                            ctx.errnoCode(114514)
                        }
                    } catch (e: Exception) {
                        Logger.e("DNS lookup error handling", e)
                        ctx.errnoCode(114514)
                    }
                }
            }

            val type = when {
                network.endsWith("4") -> DnsResolver.TYPE_A
                network.endsWith("6") -> DnsResolver.TYPE_AAAA
                else -> null
            }
            if (type != null) {
                DnsResolver.getInstance().query(
                    null,
                    domain,
                    type,
                    DnsResolver.FLAG_NO_RETRY,
                    Dispatchers.IO.asExecutor(),
                    signal,
                    callback
                )
            } else {
                DnsResolver.getInstance().query(
                    null,
                    domain,
                    DnsResolver.FLAG_NO_RETRY,
                    Dispatchers.IO.asExecutor(),
                    signal,
                    callback
                )
            }
        } else {
            // Fallback برای اندروید قدیمی‌تر
            try {
                val answer = InetAddress.getAllByName(domain)
                ctx.success(answer.mapNotNull { it.hostAddress }.joinToString("\n"))
            } catch (e: UnknownHostException) {
                ctx.errorCode(RCODE_NXDOMAIN)
            } catch (e: Exception) {
                Logger.e("DNS lookup fallback error", e)
                ctx.errnoCode(114514)
            }
        }
    }
}
