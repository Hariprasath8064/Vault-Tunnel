package com.example.myapplication.vpn.dnsproxy

import java.nio.ByteBuffer

class Resource {
    var domain: String = ""
    var type: Short = 0
    var clazz: Short = 0
    var ttl: Int = 0
    var dataLength: Short = 0
    var data: ByteArray = byteArrayOf()

    private var offset: Int = 0
    fun offset(): Int = offset

    private var length: Int = 0
    fun length(): Int = length

    companion object {
        fun fromBytes(buffer: ByteBuffer): Resource {
            val r = Resource()
            r.offset = buffer.arrayOffset() + buffer.position()
            r.domain = DnsPacket.readDomain(buffer, buffer.arrayOffset())
            r.type = buffer.short
            r.clazz = buffer.short
            r.ttl = buffer.int
            r.dataLength = buffer.short
            r.data = ByteArray(r.dataLength.toInt() and 0xFFFF)
            buffer.get(r.data)
            r.length = buffer.arrayOffset() + buffer.position() - r.offset
            return r
        }
    }

    fun toBytes(buffer: ByteBuffer) {
        if (data.isEmpty()) {
            data = byteArrayOf()
        }

        dataLength = data.size.toShort()
        offset = buffer.position()

        DnsPacket.writeDomain(domain, buffer)
        buffer.putShort(type)
        buffer.putShort(clazz)
        buffer.putInt(ttl)
        buffer.putShort(dataLength)
        buffer.put(data)

        length = buffer.position() - offset
    }
}
