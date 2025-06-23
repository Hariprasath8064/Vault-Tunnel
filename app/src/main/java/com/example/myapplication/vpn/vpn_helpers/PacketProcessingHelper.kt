package com.example.myapplication.vpn.vpn_helpers

import android.util.Log
import com.example.myapplication.vpn.VpnConstants
import com.example.myapplication.vpn.proxy.HttpHostHeaderParser
import com.example.myapplication.vpn.proxy.NatSessionManager
import com.example.myapplication.vpn.proxy.Proxy
import com.example.myapplication.vpn.dnsproxy.DnsPacket
import com.example.myapplication.vpn.dnsproxy.DnsProxy
import com.example.myapplication.vpn.dnsproxy.DnsHelper
import com.example.myapplication.vpn.tcpip.CommonMethods
import com.example.myapplication.vpn.tcpip.IPHeader
import com.example.myapplication.vpn.tcpip.TCPHeader
import com.example.myapplication.vpn.tcpip.UDPHeader
import java.io.FileOutputStream
import java.nio.ByteBuffer

object PacketProcessingHelper {
    fun processIPPacket(
        ipHeader: IPHeader,
        size: Int,
        localAddress: Int,
        packetCount: Int,
        proxy: Proxy,
        dnsProxy: DnsProxy,
        dnsHelper: DnsHelper,
        vpnOut: FileOutputStream?,
        packet: ByteArray,
        tcpHeader: TCPHeader,
        udpHeader: UDPHeader,
        prettyPrintTCPPacket: (ByteArray) -> Unit
    ) {
        when (ipHeader.protocol) {
            IPHeader.TCP -> processTCPPacket(
                ipHeader, size, localAddress, packetCount, proxy, vpnOut, packet, tcpHeader, prettyPrintTCPPacket
            )
            IPHeader.UDP -> processUDPPacket(
                ipHeader, udpHeader, localAddress, dnsProxy, packet
            )
        }
    }

    private fun processTCPPacket(
        ipHeader: IPHeader,
        size: Int,
        localAddress: Int,
        packetCount: Int,
        proxy: Proxy,
        vpnOut: FileOutputStream?,
        packet: ByteArray,
        tcpHeader: TCPHeader,
        prettyPrintTCPPacket: (ByteArray) -> Unit
    ) {
        tcpHeader.mOffset = ipHeader.headerLength

        if (ipHeader.sourceIP == localAddress) {
            if (tcpHeader.sourcePort == proxy.port) {
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "Entered If part of VPN service on packet count $packetCount")
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "The packet details are  $ipHeader")
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "The TCP  details are  $tcpHeader")
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "The packet before modification is :")
                prettyPrintTCPPacket(packet)

                val session = NatSessionManager.getSession(tcpHeader.destinationPort)
                if (session != null) {
                    ipHeader.sourceIP = ipHeader.destinationIP
                    tcpHeader.sourcePort = session.remotePort
                    ipHeader.destinationIP = localAddress

                    CommonMethods.computeTCPChecksum(ipHeader, tcpHeader)
                    Log.d(VpnConstants.TAG_PACKET_VIEW, "\nThe after modification packet count $packetCount")
                    prettyPrintTCPPacket(packet)

                    vpnOut?.write(ipHeader.mData, ipHeader.mOffset, size)
                } else {
                    Log.d(VpnConstants.TAG_DEBUG_TRACE, String.format(VpnConstants.LOG_NO_SESSION, ipHeader, tcpHeader))
                }
            } else {
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "Entered the else part of the ippacket filter on packet Count $packetCount")
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "The packet details are  $ipHeader")
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "The TCP  details are  $tcpHeader")
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "The packet before modification is :")
                prettyPrintTCPPacket(packet)

                var session = NatSessionManager.getSession(tcpHeader.sourcePort)
                if (session == null || session.remoteAddress != ipHeader.destinationIP || session.remotePort != tcpHeader.destinationPort) {
                    session = NatSessionManager.createSession(
                        tcpHeader.sourcePort,
                        ipHeader.destinationIP,
                        tcpHeader.destinationPort
                    )
                    Log.d(
                        VpnConstants.TAG_SESSION,
                        String.format(
                            VpnConstants.LOG_SESSION_CREATED,
                            CommonMethods.ipIntToString(session.remoteAddress),
                            session.remotePort
                        )
                    )
                }

                session.lastNanoTime = System.nanoTime()
                session.packetsSent++

                val tcpDataSize = ipHeader.dataLength - tcpHeader.headerLength
                if (session.packetsSent == 2 && tcpDataSize == 0) {
                    Log.d(VpnConstants.TAG_DEBUG_TRACE, "Entered the size 2 case ")
                    return
                }

                if (session.bytesSent == 0 && tcpDataSize > 10) {
                    val dataOffset = tcpHeader.mOffset + tcpHeader.headerLength
                    val host = HttpHostHeaderParser.parseHost(tcpHeader.mData, dataOffset, tcpDataSize)
                    if (host != null) {
                        session.remoteHost = host
                    } else {
                        Log.d(VpnConstants.TAG_SESSION, String.format(VpnConstants.LOG_NO_HOST, session.remoteHost))
                    }
                }

                ipHeader.sourceIP = ipHeader.destinationIP
                ipHeader.destinationIP = localAddress
                tcpHeader.destinationPort = proxy.port

                Log.d(VpnConstants.TAG_DEBUG_TRACE, "The packet details after update are  $ipHeader")
                Log.d(VpnConstants.TAG_DEBUG_TRACE, "The TCP  details after update are  $tcpHeader")
                CommonMethods.computeTCPChecksum(ipHeader, tcpHeader)

                Log.d(VpnConstants.TAG_PACKET_VIEW, "\nThe after modification packet count $packetCount")
                prettyPrintTCPPacket(packet)

                vpnOut?.write(ipHeader.mData, ipHeader.mOffset, size)
                session.bytesSent + tcpDataSize
            }
        } else {
            Log.d(VpnConstants.TAG_DEBUG_TRACE, "packet is received but not redirected")
        }
    }

    private fun processUDPPacket(
        ipHeader: IPHeader,
        udpHeader: UDPHeader,
        localAddress: Int,
        dnsProxy: DnsProxy,
        packet: ByteArray
    ) {
        udpHeader.mOffset = ipHeader.headerLength

        if (ipHeader.sourceIP == localAddress && udpHeader.destinationPort.toInt() == VpnConstants.DNS_PORT) {
            try {
                val udpPayloadOffset = ipHeader.headerLength + VpnConstants.HEADER_LENGTH_UDP
                val dnsPayloadLength = udpHeader.totalLength - VpnConstants.HEADER_LENGTH_UDP
                if (dnsPayloadLength >= VpnConstants.MIN_DNS_PACKET_SIZE && udpPayloadOffset + dnsPayloadLength <= packet.size) {
                    val dnsBuffer = ByteBuffer.wrap(packet, udpPayloadOffset, dnsPayloadLength)
                    if (dnsBuffer.remaining() >= VpnConstants.MIN_DNS_PACKET_SIZE) {
                        val dnsPacket = DnsPacket.fromBytes(dnsBuffer)
                        if (dnsPacket != null && (dnsPacket.header?.questionCount ?: 0) > 0) {
                            Log.d(VpnConstants.TAG_DNS_PROXY, "Entering DNS Proxy")
                            dnsProxy.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket)
                        }
                    } else {
                        Log.w(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_DNS_BUFFER_SMALL)
                    }
                } else {
                    Log.w(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_INVALID_DNS_SIZE)
                }
            } catch (e: Exception) {
                Log.e(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_DNS_PARSE_ERROR, e)
            }
        }
    }
}