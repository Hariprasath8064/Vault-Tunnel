package com.example.myapplication.vpn

object VpnConstants {
    // Log tags
    const val TAG_VPN_SERVICE = "VPNService"
    const val TAG_DEBUG_TRACE = "Debugtrace"
    const val TAG_PACKET_VIEW = "Packetview"
    const val TAG_DNS_PROXY = "DNSproxy"
    const val TAG_SESSION = "Session"
    const val TAG_SEND_UDP_PACKET = "sendUDPPacket"

    // VPN configuration
    const val VPN_SESSION_NAME = "VPNtoSocket"
    const val VPN_MTU = 20000 // FIXME: LARGE MTU SIZE, change to 1500
    const val VPN_ADDRESS = "10.8.0.2"
    const val VPN_ADDRESS_PREFIX = 32
    const val VPN_ROUTE = "10.0.0.0"
    const val VPN_ROUTE_PREFIX = 8
    const val PACKET_BUFFER_SIZE = 20000

    // Actions
    const val ACTION_STOP_VPN = "STOP_VPN"

    // Protocols
    const val PROTOCOL_TCP = "TCP"
    const val PROTOCOL_UDP = "UDP"

    // Misc
    const val DNS_PORT = 53
    const val MIN_DNS_PACKET_SIZE = 12
    const val HEADER_LENGTH_UDP = 8

    const val LOG_STARTING = "VPNService starting..."
    const val LOG_STOP_COMMAND = "Received STOP_VPN command"
    const val LOG_ESTABLISHED = "VPN interface established"
    const val LOG_ESTABLISH_FAILED = "Failed to establish VPN interface"
    const val LOG_ON_DESTROY = "onDestroy called"
    const val LOG_CLEANUP_ERROR = "Cleanup error: %s"
    const val LOG_ERROR_VPN_JOB = "Error in VPN job: %s"
    const val LOG_READ_ERROR = "Read error"
    const val LOG_NO_SESSION = "NoSession: %s %s"
    const val LOG_SESSION_CREATED = "Session is created for %s:%d"
    const val LOG_NO_HOST = "No host name found: %s"
    const val LOG_DNS_BUFFER_SMALL = "DNS buffer too small, skipping"
    const val LOG_INVALID_DNS_SIZE = "Invalid DNS packet size, skipping"
    const val LOG_DNS_PARSE_ERROR = "Error parsing DNS packet"
    const val LOG_PACKET_SENT = "Packet to be sent back to vpn stream "
}

object VpnProxyConstants {
    // Log tags
    const val TAG_PROXY = "Proxy"
    const val TAG_TUNNEL = "Tunnel"

    // Log messages
    const val LOG_PROXY_STARTED = "Proxy started on port: %d"
    const val LOG_NEW_CONNECTION = "New connection from: %s"
    const val LOG_SERVER_SOCKET_ERROR = "Server socket error"
    const val LOG_SERVER_ERROR = "Server error"
    const val LOG_PROXY_STOPPED = "Proxy server stopped"
    const val LOG_NO_NAT_SESSION = "No NAT session found"
    const val LOG_SSL_HANDSHAKE_ATTEMPT = "Attempting SSL handshake with proxy %s:%d"
    const val LOG_SSL_HANDSHAKE_DONE = "SSL handshake completed with proxy"
    const val LOG_PROXY_CONN_ESTABLISHED = "Proxy connection established"
    const val LOG_CONN_ERROR = "Connection error"
    const val LOG_INVALID_PROXY_RESPONSE = "Invalid proxy response: %s"

    // HTTP/Proxy
    const val HTTP_CONNECT = "CONNECT"
    const val HTTP_VERSION = "HTTP/1.1"
    const val HEADER_HOST = "Host"
    const val HEADER_PROXY_CONNECTION = "Proxy-Connection"
    const val HEADER_CONNECTION = "Connection"
    const val HEADER_USER_AGENT = "User-Agent"
    const val HEADER_KEEP_ALIVE = "Keep-Alive"
    const val USER_AGENT_VALUE = "android-vpn-proxy"
    const val CRLF = "\r\n"

    // Certificate/Key
    const val CLIENT_CERT_ALIAS = "client"
    const val CERT_TYPE_X509 = "X.509"
    const val KEY_TYPE_RSA = "RSA"

    // Misc
    const val DEFAULT_SSL_PROTOCOL = "TLS"
    const val DEFAULT_CONNECT_TIMEOUT = 5000
    const val BUFFER_SIZE = 4096
    const val PROXY_RESPONSE_OK = "200"
}