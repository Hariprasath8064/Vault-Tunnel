package com.example.myapplication.vpn.vpn_helpers

import android.util.Log
import com.example.myapplication.vpn.VpnConstants
import java.net.InetAddress
import java.nio.ByteBuffer

object PrettyPrintHelper {
    fun prettyPrintTCPPacket(packet: ByteArray) {
        val TAG = VpnConstants.TAG_PACKET_VIEW
        val buffer = ByteBuffer.wrap(packet)

        val versionAndIHL = buffer.get().toInt() and 0xFF
        val version = (versionAndIHL shr 4) and 0xF
        val headerLength = (versionAndIHL and 0xF) * 4
        val typeOfService = buffer.get().toInt() and 0xFF
        val totalLength = buffer.short.toInt() and 0xFFFF
        val identification = buffer.short.toInt() and 0xFFFF
        val flagsAndFragmentOffset = buffer.short.toInt() and 0xFFFF
        val ttl = buffer.get().toInt() and 0xFF
        val protocol = buffer.get().toInt() and 0xFF
        val checksum = buffer.short.toInt() and 0xFFFF
        val srcAddrBytes = ByteArray(4)
        buffer.get(srcAddrBytes)
        val srcAddr = InetAddress.getByAddress(srcAddrBytes)
        val destAddrBytes = ByteArray(4)
        buffer.get(destAddrBytes)
        val destAddr = InetAddress.getByAddress(destAddrBytes)

        val srcPort = buffer.short.toInt() and 0xFFFF
        val destPort = buffer.short.toInt() and 0xFFFF
        val sequenceNumber = buffer.int.toLong() and 0xFFFFFFFFL
        val acknowledgmentNumber = buffer.int.toLong() and 0xFFFFFFFFL
        val dataOffsetAndFlags = buffer.short.toInt() and 0xFFFF
        val dataOffset = (dataOffsetAndFlags shr 12) * 4
        val flags = dataOffsetAndFlags and 0x3F
        val windowSize = buffer.short.toInt() and 0xFFFF
        val tcpChecksum = buffer.short.toInt() and 0xFFFF
        val urgentPointer = buffer.short.toInt() and 0xFFFF

        val dataLength = totalLength - (headerLength + dataOffset)
        val data = ByteArray(if (dataLength > 0) dataLength else 0)
        if (data.isNotEmpty()) buffer.get(data)

        Log.d(TAG, "--------------------------------------------------------------")
        Log.d(TAG, "----------------------------")
        Log.d(TAG, "IP Packet Details:")
        Log.d(TAG, "----------------------------")
        Log.d(TAG, "Version: $version")
        Log.d(TAG, "Header Length: $headerLength bytes")
        Log.d(TAG, "Type of Service: 0x${Integer.toHexString(typeOfService)}")
        Log.d(TAG, "Total Length: $totalLength bytes")
        Log.d(TAG, "Identification: 0x${Integer.toHexString(identification)}")
        Log.d(TAG, "Flags: 0x${Integer.toHexString((flagsAndFragmentOffset shr 13) and 0x7)}")
        Log.d(TAG, "Fragment Offset: ${flagsAndFragmentOffset and 0x1FFF}")
        Log.d(TAG, "Time to Live (TTL): $ttl")
        Log.d(TAG, "Protocol: $protocol")
        Log.d(TAG, "Header Checksum: 0x${Integer.toHexString(checksum)}")
        Log.d(TAG, "Source IP: ${srcAddr.hostAddress}")
        Log.d(TAG, "Destination IP: ${destAddr.hostAddress}")
        Log.d(TAG, "----------------------------")
        Log.d(TAG, "\nTCP Segment Details:")
        Log.d(TAG, "----------------------------")
        Log.d(TAG, "Source Port: $srcPort")
        Log.d(TAG, "Destination Port: $destPort")
        Log.d(TAG, "Sequence Number: $sequenceNumber")
        Log.d(TAG, "Acknowledgment Number: $acknowledgmentNumber")
        Log.d(TAG, "Data Offset: $dataOffset bytes")
        Log.d(TAG, "Flags: 0x${Integer.toHexString(flags)}")
        Log.d(TAG, "Window Size: $windowSize")
        Log.d(TAG, "Checksum: 0x${Integer.toHexString(tcpChecksum)}")
        Log.d(TAG, "Urgent Pointer: $urgentPointer")

        if ((flags and 0x02) != 0) Log.d(TAG, "SYN flag is set - Connection request")
        if ((flags and 0x10) != 0) Log.d(TAG, "ACK flag is set - Acknowledgment field significant")
        if ((flags and 0x01) != 0) Log.d(TAG, "FIN flag is set - No more data from sender")
        if ((flags and 0x08) != 0) Log.d(TAG, "PSH flag is set - Push function")
        if ((flags and 0x20) != 0) Log.d(TAG, "URG flag is set - Urgent pointer field significant")
        if ((flags and 0x04) != 0) Log.d(TAG, "RST flag is set - Reset the connection")
        Log.d(TAG, "----------------------------")
        Log.d(TAG, "\nData (Payload):")
        Log.d(TAG, "----------------------------")
        Log.d(TAG, String(data))
        Log.d(TAG, "--------------------------------------------------------------")
    }
}
