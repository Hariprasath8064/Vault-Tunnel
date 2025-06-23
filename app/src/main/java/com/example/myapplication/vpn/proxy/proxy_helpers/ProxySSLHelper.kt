package com.example.myapplication.vpn.proxy.proxy_helpers

import android.util.Log
import com.example.myapplication.vpn.VpnProxyConstants
import com.example.myapplication.vpn.proxy.CustomTrustManager
import com.example.myapplication.vpn.VPNService
import java.net.InetSocketAddress
import java.net.Socket
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket

internal object ProxySSLHelper {
    fun createSSLContext(
//        clientCert: X509Certificate?,
//        privateKey: PrivateKey?,
//        serverCert: X509Certificate?
    ): SSLContext {
//        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
//        keyStore.load(null)
//        if (clientCert != null && privateKey != null) {
//            keyStore.setKeyEntry(
//                VpnProxyConstants.CLIENT_CERT_ALIAS, privateKey, null, arrayOf(clientCert)
//            )
//        }
//        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
//        kmf.init(keyStore, null)
        //val trustManagers = arrayOf(serverCert?.let { CustomTrustManager(it) })
//        val trustManagers = if (serverCert != null) {
//            arrayOf(CustomTrustManager(serverCert))
//        } else {
//            null
//        }
        val sslContext = SSLContext.getInstance(VpnProxyConstants.DEFAULT_SSL_PROTOCOL)
        Log.d(
            VpnProxyConstants.TAG_TUNNEL,
            "SSLContext init with default trust managers"
//            String.format(
//                VpnProxyConstants.LOG_SSL_INIT,
//                kmf.keyManagers.contentToString(),
//                trustManagers.contentToString()
//            )
        )
        //sslContext.init(kmf.keyManagers, trustManagers, SecureRandom())
        sslContext.init(null, null, SecureRandom())
        return sslContext
    }

    fun createSSLSocket(
        service: VPNService,
        proxyHost: String,
        proxyPort: Int,
        sslContext: SSLContext
    ): SSLSocket {
        val plainSocket = Socket()
        plainSocket.bind(InetSocketAddress(0))
        service.protect(plainSocket)
        plainSocket.connect(InetSocketAddress(proxyHost, proxyPort), VpnProxyConstants.DEFAULT_CONNECT_TIMEOUT)
        val sslSocket = sslContext.socketFactory.createSocket(
            plainSocket, proxyHost, proxyPort, true
        ) as SSLSocket
        val sslParams = sslSocket.sslParameters
        sslParams.serverNames = listOf(SNIHostName(proxyHost))
        sslSocket.sslParameters = sslParams
        Log.d(
            VpnProxyConstants.TAG_TUNNEL,
            String.format(VpnProxyConstants.LOG_SSL_HANDSHAKE_ATTEMPT, proxyHost, proxyPort)
        )
        sslSocket.startHandshake()
        Log.d(VpnProxyConstants.TAG_TUNNEL, VpnProxyConstants.LOG_SSL_HANDSHAKE_DONE)
        return sslSocket
    }
}