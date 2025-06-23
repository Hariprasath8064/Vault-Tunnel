package com.example.myapplication.vpn.tcpip

class UDPHeader(
    var mData: ByteArray,
    var mOffset: Int
) {
    companion object {
        const val offset_src_port: Short = 0
        const val offset_dest_port: Short = 2
        const val offset_tlen: Short = 4
        const val offset_crc: Short = 6
    }

    var sourcePort: Short
        get() = CommonMethods.readShort(mData, mOffset + offset_src_port)
        set(value) = CommonMethods.writeShort(mData, mOffset + offset_src_port, value)

    var destinationPort: Short
        get() = CommonMethods.readShort(mData, mOffset + offset_dest_port)
        set(value) = CommonMethods.writeShort(mData, mOffset + offset_dest_port, value)

    var totalLength: Int
        get() = CommonMethods.readShort(mData, mOffset + offset_tlen).toInt() and 0xFFFF
        set(value) = CommonMethods.writeShort(mData, mOffset + offset_tlen, value.toShort())

    var crc: Short
        get() = CommonMethods.readShort(mData, mOffset + offset_crc)
        set(value) = CommonMethods.writeShort(mData, mOffset + offset_crc, value)

    override fun toString(): String {
        return "%d->%d".format(sourcePort.toInt() and 0xFFFF, destinationPort.toInt() and 0xFFFF)
    }
}
