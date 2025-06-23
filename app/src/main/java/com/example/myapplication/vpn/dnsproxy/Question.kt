package com.example.myapplication.vpn.dnsproxy

import java.nio.ByteBuffer

class Question {
    var domain: String = ""
    var type: Short = 0
    var clazz: Short = 0

    private var offset: Int = 0
    fun offset(): Int = offset

    private var length: Int = 0
    fun length(): Int = length

    companion object {
        fun fromBytes(buffer: ByteBuffer): Question {
            val q = Question()
            q.offset = buffer.position()
            q.domain = DnsPacket.readDomain(buffer, 0)  // dnsHeaderOffset=0
            q.type = buffer.short
            q.clazz = buffer.short
            q.length = buffer.position() - q.offset
            return q
        }

    }

    fun toBytes(buffer: ByteBuffer) {
        offset = buffer.position()
        DnsPacket.writeDomain(domain, buffer)
        buffer.putShort(type)
        buffer.putShort(clazz)
        length = buffer.position() - offset
    }
}
