package com.example.myapplication.vpn.dnsproxy

import java.nio.ByteBuffer
import kotlin.text.iterator

class DnsPacket {
    var header: DnsHeader? = null
    var questions: Array<Question>? = null
    var resources: Array<Resource>? = null
    var aResources: Array<Resource>? = null
    var eResources: Array<Resource>? = null

    var size: Int = 0

    companion object {
        fun fromBytes(buffer: ByteBuffer): DnsPacket? {
            if (buffer.limit() < 12 || buffer.limit() > 512) return null

            val packet = DnsPacket()
            packet.size = buffer.limit()
            packet.header = DnsHeader.fromBytes(buffer)

            val h = packet.header!!


            if (h.questionCount > 2 || h.resourceCount > 50 || h.aResourceCount > 50 || h.eResourceCount > 50) return null

            packet.questions = Array(h.questionCount.toInt()) { Question.Companion.fromBytes(buffer) }
            packet.resources = Array(h.resourceCount.toInt()) { Resource.Companion.fromBytes(buffer) }
            packet.aResources = Array(h.aResourceCount.toInt()) { Resource.Companion.fromBytes(buffer) }
            packet.eResources = Array(h.eResourceCount.toInt()) { Resource.Companion.fromBytes(buffer) }

            return packet
        }


        fun readDomain(buffer: ByteBuffer, dnsHeaderOffset: Int): String {
            val sb = StringBuilder()
            var totalBytesRead = 0
            var len: Int

            while (buffer.hasRemaining()) {
                len = buffer.get().toInt() and 0xFF
                totalBytesRead++
                if (len == 0) break
                if ((len and 0xC0) == 0xC0) {
                    if (buffer.remaining() < 1) {
                        throw IllegalArgumentException("Insufficient bytes for DNS pointer")
                    }
                    val pointer = ((len and 0x3F) shl 8) or (buffer.get().toInt() and 0xFF)
                    totalBytesRead++
                    val newPosition = dnsHeaderOffset + pointer
                    if (newPosition >= buffer.array().size) {
                        throw IllegalArgumentException("Invalid DNS pointer offset")
                    }
                    val currentPos = buffer.position()
                    val newBuffer = ByteBuffer.wrap(
                        buffer.array(),
                        newPosition,
                        buffer.array().size - newPosition
                    )
                    sb.append(readDomain(newBuffer, dnsHeaderOffset))
                    buffer.position(currentPos)
                    return sb.toString()
                } else {
                    if (buffer.remaining() < len) {
                        throw IllegalArgumentException(
                            "Insufficient bytes for DNS label (expected $len, got ${buffer.remaining()})"
                        )
                    }
                    for (i in 0 until len) {
                        sb.append(buffer.get().toInt().toChar())
                        totalBytesRead++
                    }
                    sb.append('.')
                }
            }

            if (sb.isNotEmpty() && sb.last() == '.') {
                sb.deleteCharAt(sb.length - 1)
            }

            return sb.toString()
        }

        fun writeDomain(domain: String?, buffer: ByteBuffer) {
            if (domain.isNullOrEmpty()) {
                buffer.put(0)
                return
            }

            val parts = domain.split('.')
            for (part in parts) {
                buffer.put(part.length.toByte())
                for (ch in part) {
                    buffer.put(ch.code.toByte())
                }
            }
            buffer.put(0) // null terminator for domain
        }
    }

    fun toBytes(buffer: ByteBuffer) {
        val h = header ?: return

        h.questionCount = questions?.size?.toShort() ?: 0
        h.resourceCount = resources?.size?.toShort() ?: 0
        h.aResourceCount = aResources?.size?.toShort() ?: 0
        h.eResourceCount = eResources?.size?.toShort() ?: 0

        h.toBytes(buffer)

        questions?.forEach { it.toBytes(buffer) }
        resources?.forEach { it.toBytes(buffer) }
        aResources?.forEach { it.toBytes(buffer) }
        eResources?.forEach { it.toBytes(buffer) }
    }

}
