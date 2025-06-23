package com.example.myapplication.vpn.proxy.proxy_helpers

import java.net.Socket

internal object ProxySocketHelper {
    fun quickClose(sock: Socket?) {
        try {
            sock?.close()
        } catch (_: Exception) {}
    }
}