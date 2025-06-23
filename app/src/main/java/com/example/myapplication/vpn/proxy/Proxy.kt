package com.example.myapplication.vpn.proxy

import android.util.Log
import com.example.myapplication.vpn.VPNService
import com.example.myapplication.vpn.VpnProxyConstants
import com.example.myapplication.vpn.proxy.proxy_helpers.ProxyRelayHelper
import com.example.myapplication.vpn.proxy.proxy_helpers.ProxyRequestHelper
import com.example.myapplication.vpn.proxy.proxy_helpers.ProxySSLHelper
import com.example.myapplication.vpn.proxy.proxy_helpers.ProxySocketHelper
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import javax.net.ssl.SSLSocket

class Proxy(private val service: VPNService, port: Int) : Thread() {

    var port: Short = port.toShort()
    private var portKey: Int = 0
    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun run() {
        try {
            serverSocket = ServerSocket(port.toInt()).apply {
                port = (localPort and 0xFFFF).toShort()
                Log.d(VpnProxyConstants.TAG_PROXY, String.format(VpnProxyConstants.LOG_PROXY_STARTED, localPort))
            }

            while (!isInterrupted) {
                try {
                    val socket = serverSocket!!.accept()
                    Log.d(VpnProxyConstants.TAG_PROXY, String.format(VpnProxyConstants.LOG_NEW_CONNECTION, socket.inetAddress))
                    scope.launch {
                        Tunnel(socket).run()
                    }
                } catch (e: SocketException) {
                    if (isInterrupted) break
                    Log.e(VpnProxyConstants.TAG_PROXY, VpnProxyConstants.LOG_SERVER_SOCKET_ERROR, e)
                }
            }
        } catch (e: Exception) {
            Log.e(VpnProxyConstants.TAG_PROXY, VpnProxyConstants.LOG_SERVER_ERROR, e)
        } finally {
            serverSocket?.close()
            scope.cancel()
            Log.d(VpnProxyConstants.TAG_PROXY, VpnProxyConstants.LOG_PROXY_STOPPED)
        }
    }

    override fun interrupt() {
        super.interrupt()
        serverSocket?.close()
    }

    inner class Tunnel(private val socket: Socket) {
        private lateinit var clientIn: InputStream
        private lateinit var clientOut: OutputStream
        private lateinit var serverIn: InputStream
        private lateinit var serverOut: OutputStream
        private var proxySocket: SSLSocket? = null

        suspend fun run() = withContext(Dispatchers.IO) {
            try {
                portKey = socket.port
                val session = NatSessionManager.getSession(socket.port.toShort()) ?: run {
                    Log.e(VpnProxyConstants.TAG_TUNNEL, VpnProxyConstants.LOG_NO_NAT_SESSION)
                    return@withContext
                }

                clientIn = socket.getInputStream()
                clientOut = socket.getOutputStream()


                val proxyHost = "192.168.29.209"
                val proxyPort = 8443

                val connectRequest = ProxyRequestHelper.buildConnectRequest(
                    session.remoteHost.toString(),
                    session.remotePort,
                )
//                val clientCert = clientCertPem?.let { ProxyCertificateHelper.parseCertificate(it) }
//                Log.d(VpnProxyConstants.TAG_TUNNEL, String.format(VpnProxyConstants.LOG_CLIENT_CERT_PARSED, clientCert))
//                val serverCert = serverCertPem?.let { ProxyCertificateHelper.parseCertificate(it) }
//                Log.d(VpnProxyConstants.TAG_TUNNEL, String.format(VpnProxyConstants.LOG_SERVER_CERT_PARSED, serverCert))

//                val privateKey = privateKeyBase64?.let { ProxyCertificateHelper.parsePrivateKey(it) }

//                val sslContext = ProxySSLHelper.createSSLContext(clientCert, privateKey, serverCert)
                val sslContext = ProxySSLHelper.createSSLContext()
                proxySocket = ProxySSLHelper.createSSLSocket(service, proxyHost, proxyPort, sslContext)

                proxySocket?.let { sslSock ->
                    serverIn = sslSock.inputStream
                    serverOut = sslSock.outputStream
                } ?: throw IOException("proxySocket is null after SSL handshake")

                serverOut.write(connectRequest.toByteArray(Charsets.UTF_8))
                serverOut.flush()

                val response = ByteArray(1024)
                val size = serverIn.read(response)
                val responseStr = if (size > 0) String(response, 0, size) else ""
                if (!responseStr.contains(VpnProxyConstants.PROXY_RESPONSE_OK)) {
                    throw IOException(String.format(VpnProxyConstants.LOG_INVALID_PROXY_RESPONSE, responseStr))
                }
                Log.d(VpnProxyConstants.TAG_TUNNEL, VpnProxyConstants.LOG_PROXY_CONN_ESTABLISHED)

                ProxyRelayHelper.relay(clientIn, clientOut, serverIn, serverOut)

            } catch (e: Exception) {
                Log.e(VpnProxyConstants.TAG_TUNNEL, VpnProxyConstants.LOG_CONN_ERROR, e)
            } finally {
                ProxySocketHelper.quickClose(socket)
                ProxySocketHelper.quickClose(proxySocket)
                NatSessionManager.removeSession(portKey.toShort())
            }
        }
    }
}