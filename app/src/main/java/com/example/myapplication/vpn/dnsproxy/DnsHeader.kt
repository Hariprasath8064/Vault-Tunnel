package com.example.myapplication.vpn.dnsproxy

import java.nio.ByteBuffer

class DnsHeader(
    var data: ByteArray,
    var offset: Int
) {
    var id: Short = 0
    var flags: DnsFlags = DnsFlags()
    var questionCount: Short = 0
    var resourceCount: Short = 0
    var aResourceCount: Short = 0
    var eResourceCount: Short = 0

    companion object {
        fun fromBytes(buffer: ByteBuffer): DnsHeader {
            val header = DnsHeader(buffer.array(), buffer.arrayOffset() + buffer.position())
            header.id = buffer.short
            header.flags = DnsFlags.parse(buffer.short)
            header.questionCount = buffer.short
            header.resourceCount = buffer.short
            header.aResourceCount = buffer.short
            header.eResourceCount = buffer.short
            return header
        }
    }
    fun toBytes(buffer: ByteBuffer) {
        buffer.putShort(id)
        buffer.putShort(flags.toShort())
        buffer.putShort(questionCount)
        buffer.putShort(resourceCount)
        buffer.putShort(aResourceCount)
        buffer.putShort(eResourceCount)
    }

}