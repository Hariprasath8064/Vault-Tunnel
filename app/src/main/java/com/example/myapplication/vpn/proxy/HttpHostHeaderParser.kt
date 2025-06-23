package com.example.myapplication.vpn.proxy

import com.example.myapplication.vpn.tcpip.CommonMethods
import java.util.Locale
import kotlin.experimental.and

object HttpHostHeaderParser {

    fun parseHost(buffer: ByteArray, offset: Int, count: Int): String? {
        return try {
            when (buffer[offset].toInt().toChar()) {
                'G', 'H', 'P', 'D', 'O', 'T', 'C' -> getHttpHost(buffer, offset, count)
                0x16.toChar() -> getSNI(buffer, offset, count)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getHttpHost(buffer: ByteArray, offset: Int, count: Int): String? {
        val headerString = String(buffer, offset, count)
        val headerLines = headerString.split("\r\n")
        val requestLine = headerLines.firstOrNull() ?: return null

        if (requestLine.startsWith("GET") || requestLine.startsWith("POST") ||
            requestLine.startsWith("HEAD") || requestLine.startsWith("OPTIONS")) {
            for (line in headerLines.drop(1)) {
                val nameValue = line.split(":", limit = 2)
                if (nameValue.size == 2) {
                    val name = nameValue[0].lowercase(Locale.ENGLISH).trim()
                    val value = nameValue[1].trim()
                    if (name == "host") return value
                }
            }
        }
        return null
    }

    fun getSNI(buffer: ByteArray, offset: Int, count: Int): String? {
        var pos = offset
        val limit = offset + count
        if (count > 43 && buffer[offset] == 0x16.toByte()) {
            pos += 43
            if (pos + 1 > limit) return null
            val sessionIDLength = buffer[pos++].toInt() and 0xFF
            pos += sessionIDLength
            if (pos + 2 > limit) return null
            val cipherSuitesLength = CommonMethods.readShort(buffer, pos) and 0xFFFF.toShort()
            pos += 2 + cipherSuitesLength
            if (pos + 1 > limit) return null
            val compressionMethodLength = buffer[pos++].toInt() and 0xFF
            pos += compressionMethodLength
            if (pos == limit) return null
            if (pos + 2 > limit) return null
            val extensionsLength = CommonMethods.readShort(buffer, pos) and 0xFFFF.toShort()
            pos += 2
            if (pos + extensionsLength > limit) return null

            while (pos + 4 <= limit) {
                val type0 = buffer[pos++].toInt() and 0xFF
                val type1 = buffer[pos++].toInt() and 0xFF
                val length = CommonMethods.readShort(buffer, pos) and 0xFFFF.toShort()
                pos += 2
                if (type0 == 0x00 && type1 == 0x00 && length > 5) {
                    pos += 5
                    val sniLength = length - 5
                    if (pos + sniLength > limit) return null
                    return String(buffer, pos, sniLength)
                } else {
                    pos += length
                }
            }
            return null
        } else {
            return null
        }
    }
}