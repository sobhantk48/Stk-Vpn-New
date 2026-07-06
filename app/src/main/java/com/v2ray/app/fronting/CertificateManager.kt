package com.v2ray.app.fronting

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.Date
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

class CertificateManager(private val context: Context) {
    companion object {
        private const val CA_ALIAS = "v2rayez-ca"
        private const val KEYSTORE_PASSWORD = "v2rayez123"
        private const val CA_CERT_FILE = "v2rayez-ca.crt"
    }

    private val keyStore: KeyStore by lazy { loadKeyStore() }
    private val leafCache = ConcurrentHashMap<String, KeyStore.PrivateKeyEntry>()

    private fun loadKeyStore(): KeyStore {
        val ks = KeyStore.getInstance("PKCS12")
        val file = File(context.filesDir, "v2rayez-keystore.p12")
        if (file.exists()) {
            file.inputStream().use { ks.load(it, KEYSTORE_PASSWORD.toCharArray()) }
            return ks
        }
        ks.load(null, null)
        generateCA(ks)
        file.outputStream().use { ks.store(it, KEYSTORE_PASSWORD.toCharArray()) }
        return ks
    }

    private fun generateCA(ks: KeyStore) {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048, SecureRandom())
        val pair = keyGen.generateKeyPair()

        val issuer = X500Name("CN=V2RayEz Domain Fronting CA, O=V2RayEz, C=IR")
        val serial = BigInteger.valueOf(System.currentTimeMillis())
        val notBefore = Date(System.currentTimeMillis() - 86400000)
        val notAfter = Date(System.currentTimeMillis() + 365 * 86400000L)

        val builder = JcaX509v3CertificateBuilder(
            issuer, serial, notBefore, notAfter, issuer, pair.public
        )

        val signer = JcaContentSignerBuilder("SHA256WithRSA").build(pair.private)
        val cert = JcaX509CertificateConverter().getCertificate(builder.build(signer))

        ks.setKeyEntry(CA_ALIAS, pair.private, KEYSTORE_PASSWORD.toCharArray(), arrayOf(cert))
        saveCACert(cert)
    }

    private fun saveCACert(cert: X509Certificate) {
        val file = File(context.filesDir, CA_CERT_FILE)
        file.outputStream().use {
            it.write("-----BEGIN CERTIFICATE-----\n".toByteArray())
            it.write(android.util.Base64.encode(cert.encoded, android.util.Base64.DEFAULT))
            it.write("-----END CERTIFICATE-----\n".toByteArray())
        }
    }

    fun getCACertFile(): File = File(context.filesDir, CA_CERT_FILE)

    fun getLeafCertificate(host: String): KeyStore.PrivateKeyEntry {
        return leafCache.getOrPut(host) {
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(2048, SecureRandom())
            val pair = keyGen.generateKeyPair()

            val issuer = (keyStore.getCertificate(CA_ALIAS) as X509Certificate).subjectX500Principal
            val name = X500Name("CN=$host, O=V2RayEz, C=IR")
            val serial = BigInteger.valueOf(System.currentTimeMillis())
            val notBefore = Date(System.currentTimeMillis() - 3600000)
            val notAfter = Date(System.currentTimeMillis() + 720 * 3600000L)

            val builder = JcaX509v3CertificateBuilder(
                X500Name(issuer.name), serial, notBefore, notAfter, name, pair.public
            )

            val signer = JcaContentSignerBuilder("SHA256WithRSA")
                .build(keyStore.getKey(CA_ALIAS, KEYSTORE_PASSWORD.toCharArray()) as java.security.PrivateKey)
            val cert = JcaX509CertificateConverter().getCertificate(builder.build(signer))

            KeyStore.PrivateKeyEntry(pair.private, arrayOf(cert))
        }
    }

    fun getSSLContext(): SSLContext {
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray())

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)

        val ctx = SSLContext.getInstance("TLS")
        ctx.init(kmf.keyManagers, tmf.trustManagers, SecureRandom())
        return ctx
    }

    fun getTrustManager(): X509TrustManager {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)
        return tmf.trustManagers.firstOrNull { it is X509TrustManager } as X509TrustManager
    }
}
