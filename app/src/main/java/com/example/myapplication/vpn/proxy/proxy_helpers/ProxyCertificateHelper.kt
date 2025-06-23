package com.example.myapplication.vpn.proxy.proxy_helpers

import android.util.Base64
import com.example.myapplication.vpn.VpnProxyConstants
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec

internal object ProxyCertificateHelper {
    fun parseCertificate(pem: String): X509Certificate {
        val factory = CertificateFactory.getInstance(VpnProxyConstants.CERT_TYPE_X509)
        val cleanedPem = pem
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replace("\\s".toRegex(), "")
        val decoded = Base64.decode(cleanedPem, Base64.DEFAULT)
        return factory.generateCertificate(ByteArrayInputStream(decoded)) as X509Certificate
    }

    fun parsePrivateKey(base64: String): PrivateKey {
        val keyBytes = Base64.decode(base64, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance(VpnProxyConstants.KEY_TYPE_RSA).generatePrivate(keySpec)
    }
}