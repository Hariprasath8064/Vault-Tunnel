package com.example.myapplication.vpn.vpn_helpers

import android.util.Log
import com.example.myapplication.vpn.VpnConstants
import com.example.myapplication.vpn.tcpip.CommonMethods
import com.example.myapplication.vpn.tcpip.IPHeader
import com.example.myapplication.vpn.tcpip.UDPHeader
import java.io.FileOutputStream

object UdpPacketSender {
    fun sendUDPPacket(ipHeader: IPHeader, udpHeader: UDPHeader, vpnOut: FileOutputStream?) {
        try {
            CommonMethods.computeUDPChecksum(ipHeader, udpHeader)
            Log.d(VpnConstants.TAG_SEND_UDP_PACKET, VpnConstants.LOG_PACKET_SENT)
            vpnOut?.write(ipHeader.mData, ipHeader.mOffset, ipHeader.totalLength)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}