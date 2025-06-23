package com.example.myapplication.vpn.proxy.proxy_helpers

import com.example.myapplication.vpn.VpnProxyConstants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.InputStream
import java.io.OutputStream

internal object ProxyRelayHelper {
    suspend fun relay(clientIn: InputStream, clientOut: OutputStream, serverIn: InputStream, serverOut: OutputStream) = coroutineScope {
        val clientToServer = async {
            try {
                val buffer = ByteArray(VpnProxyConstants.BUFFER_SIZE)
                while (true) {
                    val len = withContext(Dispatchers.IO) { clientIn.read(buffer) }
                    if (len <= 0) break
                    serverOut.write(buffer, 0, len)
                    serverOut.flush()
                }
            } catch (_: Exception) {}
        }

        val serverToClient = async {
            try {
                val buffer = ByteArray(VpnProxyConstants.BUFFER_SIZE)
                while (true) {
                    val len = serverIn.read(buffer)
                    if (len <= 0) break
                    clientOut.write(buffer, 0, len)
                    clientOut.flush()
                }
            } catch (_: Exception) {}
        }

        try {
            clientToServer.await()
            serverToClient.await()
        } catch (_: CancellationException) {}
    }
}