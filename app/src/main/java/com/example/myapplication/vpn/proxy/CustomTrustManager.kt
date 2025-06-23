package com.example.myapplication.vpn.proxy

import android.annotation.SuppressLint
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@SuppressLint("CustomX509TrustManager")
class CustomTrustManager(serverCert: X509Certificate) : X509TrustManager {
    private val trustManager: X509TrustManager

    init {
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null)
        ks.setCertificateEntry("server", serverCert)
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)
        trustManager = tmf.trustManagers.first { it is X509TrustManager } as X509TrustManager
    }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        // Not used for client-side
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        trustManager.checkServerTrusted(chain, authType)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = trustManager.acceptedIssuers
}