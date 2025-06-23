package com.example.myapplication.vpn.tcpip

import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.text.toInt

object CommonMethods {

    fun ipIntToInet4Address(ip: Int): InetAddress? {
        val ipAddress = ByteArray(4)
        writeInt(ipAddress, 0, ip)
        return try {
            Inet4Address.getByAddress(ipAddress)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            null
        }
    }
//    fun inet4AddressToInt(addr: InetAddress): Int {
//        val bytes = addr.address
//        require(bytes.size == 4) { "Not an IPv4 address" }
//        return ((bytes[0].toInt() and 0xFF) shl 24) or
//                ((bytes[1].toInt() and 0xFF) shl 16) or
//                ((bytes[2].toInt() and 0xFF) shl 8) or
//                (bytes[3].toInt() and 0xFF)
//    }
fun inet4AddressToInt(address: Any): Int {
    return when (address) {
        is InetAddress -> {
            val bytes = address.address
            require(bytes.size == 4) { "Not an IPv4 address" }
            (bytes[0].toInt() and 0xFF shl 24) or
                    (bytes[1].toInt() and 0xFF shl 16) or
                    (bytes[2].toInt() and 0xFF shl 8) or
                    (bytes[3].toInt() and 0xFF)
        }
        is String -> {
            val parts = address.split('.')
            require(parts.size == 4) { "Invalid IPv4 address" }
            parts[0].toInt() shl 24 or
                    parts[1].toInt() shl 16 or
                    parts[2].toInt() shl 8 or
                    parts[3].toInt()
        }
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

    fun ipIntToString(ip: Int): String {
        return "${(ip shr 24) and 0xFF}.${(ip shr 16) and 0xFF}.${(ip shr 8) and 0xFF}.${ip and 0xFF}"
    }

    fun ipBytesToString(ip: ByteArray): String {
        return "${ip[0].toInt() and 0xFF}.${ip[1].toInt() and 0xFF}.${ip[2].toInt() and 0xFF}.${ip[3].toInt() and 0xFF}"
    }

    fun ipStringToInt(ip: String): Int {
        val parts = ip.split(".")
        return (parts[0].toInt() shl 24) or
                (parts[1].toInt() shl 16) or
                (parts[2].toInt() shl 8) or
                parts[3].toInt()
    }

    fun readInt(data: ByteArray, offset: Int): Int {
        return ((data[offset].toInt() and 0xFF) shl 24) or
                ((data[offset + 1].toInt() and 0xFF) shl 16) or
                ((data[offset + 2].toInt() and 0xFF) shl 8) or
                (data[offset + 3].toInt() and 0xFF)
    }

    fun readShort(data: ByteArray, offset: Int): Short {
        return (((data[offset].toInt() and 0xFF) shl 8) or
                (data[offset + 1].toInt() and 0xFF)).toShort()
    }

    fun writeInt(data: ByteArray, offset: Int, value: Int) {
        data[offset] = (value shr 24).toByte()
        data[offset + 1] = (value shr 16).toByte()
        data[offset + 2] = (value shr 8).toByte()
        data[offset + 3] = value.toByte()
    }

    fun writeShort(data: ByteArray, offset: Int, value: Short) {
        data[offset] = (value.toInt() shr 8).toByte()
        data[offset + 1] = value.toByte()
    }

    fun htons(u: Short): Short {
        val v = u.toInt() and 0xFFFF
        return (((v shl 8) and 0xFF00) or (v shr 8)).toShort()
    }

    fun ntohs(u: Short): Short = htons(u)

    fun hton(u: Int): Int {
        return ((u shr 24) and 0xFF) or
                ((u shr 8) and 0xFF00) or
                ((u shl 8) and 0xFF0000) or
                ((u shl 24) and -0x1000000)
    }

    fun ntoh(u: Int): Int = hton(u)

    fun checksum(sum: Long, buf: ByteArray, offset: Int, len: Int): Short {
        var s = sum + getsum(buf, offset, len)
        while (s shr 16 != 0L) {
            s = (s and 0xFFFF) + (s shr 16)
        }
        return s.inv().toShort()
    }

    fun getsum(buf: ByteArray, offset: Int, len: Int): Long {
        var sum = 0L
        var i = offset
        var l = len

        while (l > 1) {
            sum += (readShort(buf, i).toInt() and 0xFFFF).toLong()
            i += 2
            l -= 2
        }

        if (l > 0) {
            sum += ((buf[i].toInt() and 0xFF) shl 8).toLong()
        }

        return sum
    }

    fun computeIPChecksum(ipHeader: IPHeader): Boolean {
        val oldCrc = ipHeader.crc
        ipHeader.crc = 0
        val newCrc = checksum(0, ipHeader.mData, ipHeader.mOffset, ipHeader.headerLength)
        ipHeader.crc = newCrc
        return oldCrc == newCrc
    }

    fun computeTCPChecksum(ipHeader: IPHeader, tcpHeader: TCPHeader): Boolean {
        computeIPChecksum(ipHeader)
        val ipDataLen = ipHeader.totalLength - ipHeader.headerLength
        if (ipDataLen <= 0) return false

        // Use the correct offset for source IP in your IPHeader class
        val offsetSrcIp = IPHeader.Companion.offset_src_ip // Make sure this exists in your IPHeader
        var sum = getsum(ipHeader.mData, ipHeader.mOffset + offsetSrcIp, 8)
        sum += ipHeader.protocol.toLong() and 0xFF
        sum += ipDataLen.toLong()

        val oldCrc = tcpHeader.crc
        tcpHeader.crc = 0
        val newCrc = checksum(sum, tcpHeader.mData, tcpHeader.mOffset, ipDataLen)
        tcpHeader.crc = newCrc

        return oldCrc == newCrc
    }

    fun computeUDPChecksum(ipHeader: IPHeader, udpHeader: UDPHeader): Boolean {
        computeIPChecksum(ipHeader)
        val ipDataLen = ipHeader.totalLength - ipHeader.headerLength
        if (ipDataLen <= 0) {
            return false
        }

        val offsetSrcIp = IPHeader.Companion.offset_src_ip // Make sure this exists in your IPHeader
        var sum = getsum(ipHeader.mData, ipHeader.mOffset + offsetSrcIp, 8)
        sum += ipHeader.protocol.toLong() and 0xFF
        sum += ipDataLen.toLong()

        val oldCrc = udpHeader.crc
        udpHeader.crc = 0
        val newCrc = checksum(sum, udpHeader.mData, udpHeader.mOffset, ipDataLen)
        udpHeader.crc = newCrc

        return oldCrc == newCrc
    }
}