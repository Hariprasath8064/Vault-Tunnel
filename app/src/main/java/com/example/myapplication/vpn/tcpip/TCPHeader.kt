package com.example.myapplication.vpn.tcpip

class TCPHeader(
    var mData: ByteArray,
    var mOffset: Int
) {

    companion object {
        const val FIN = 1
        const val SYN = 2
        const val RST = 4
        const val PSH = 8
        const val ACK = 16
        const val URG = 32

        const val offset_src_port: Short = 0
        const val offset_dest_port: Short = 2
        const val offset_seq: Int = 4
        const val offset_ack: Int = 8
        const val offset_lenres: Byte = 12
        const val offset_flag: Byte = 13
        const val offset_win: Short = 14
        const val offset_crc: Short = 16
        const val offset_urp: Short = 18
    }

    val headerLength: Int
        get() {
            val lenres = mData[mOffset + offset_lenres] .toInt() and 0xFF
            return (lenres shr 4) * 4
        }

    var sourcePort: Short
        get() = CommonMethods.readShort(mData, mOffset + offset_src_port)
        set(value) = CommonMethods.writeShort(mData, mOffset + offset_src_port, value)

    var destinationPort: Short
        get() = CommonMethods.readShort(mData, mOffset + offset_dest_port)
        set(value) = CommonMethods.writeShort(mData, mOffset + offset_dest_port, value)

    val flags: Byte
        get() = mData[mOffset + offset_flag]

    var crc: Short
        get() = CommonMethods.readShort(mData, mOffset + offset_crc)
        set(value) = CommonMethods.writeShort(mData, mOffset + offset_crc, value)

    val seqID: Int
        get() = CommonMethods.readInt(mData, mOffset + offset_seq)

    val ackID: Int
        get() = CommonMethods.readInt(mData, mOffset + offset_ack)

    override fun toString(): String {
        val flagStr = buildString {
            if ((flags.toInt() and SYN) == SYN) append("SYN ")
            if ((flags.toInt() and ACK) == ACK) append("ACK ")
            if ((flags.toInt() and PSH) == PSH) append("PSH ")
            if ((flags.toInt() and RST) == RST) append("RST ")
            if ((flags.toInt() and FIN) == FIN) append("FIN ")
            if ((flags.toInt() and URG) == URG) append("URG ")
        }
        return "%s%d->%d %d:%d".format(
            flagStr,
            sourcePort.toInt() and 0xFFFF,
            destinationPort.toInt() and 0xFFFF,
            seqID,
            ackID
        )
    }
}