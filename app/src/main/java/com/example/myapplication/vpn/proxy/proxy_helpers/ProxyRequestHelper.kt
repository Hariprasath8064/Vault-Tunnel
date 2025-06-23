package com.example.myapplication.vpn.proxy.proxy_helpers

import com.example.myapplication.vpn.VpnProxyConstants

internal object ProxyRequestHelper {
    fun buildConnectRequest(
        remoteHost: String,
        remotePort: Short,
    ): String = buildString {
        append("${VpnProxyConstants.HTTP_CONNECT} $remoteHost:$remotePort ${VpnProxyConstants.HTTP_VERSION}${VpnProxyConstants.CRLF}")
        append("${VpnProxyConstants.HEADER_HOST}: $remoteHost:$remotePort${VpnProxyConstants.CRLF}")
        append("${VpnProxyConstants.HEADER_PROXY_CONNECTION}: ${VpnProxyConstants.HEADER_KEEP_ALIVE}${VpnProxyConstants.CRLF}")
        append("${VpnProxyConstants.HEADER_CONNECTION}: ${VpnProxyConstants.HEADER_KEEP_ALIVE}${VpnProxyConstants.CRLF}")
        append("${VpnProxyConstants.HEADER_USER_AGENT}: ${VpnProxyConstants.USER_AGENT_VALUE}${VpnProxyConstants.CRLF}")
        append(VpnProxyConstants.CRLF)
    }
}