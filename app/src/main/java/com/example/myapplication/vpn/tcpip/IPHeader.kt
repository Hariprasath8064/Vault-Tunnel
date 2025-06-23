package com.example.myapplication.vpn.tcpip

import kotlin.experimental.and

class IPHeader(
    var mData: ByteArray,
    var mOffset: Int
) {
    companion object {
        const val IP: Short = 0x0800
        const val ICMP: Byte = 1
        const val TCP: Byte = 6
        const val UDP: Byte = 17

        const val offset_ver_ihl: Byte = 0
        const val offset_tos: Byte = 1
        const val offset_tlen: Short = 2
        const val offset_identification: Short = 4
        const val offset_flags_fo: Short = 6
        const val offset_ttl: Byte = 8
        const val offset_proto: Byte = 9
        const val offset_crc: Short = 10
        const val offset_src_ip: Int = 12
        const val offset_dest_ip: Int = 16
        const val offset_op_pad: Int = 20
    }

    fun setDefault() {
        headerLength = 20
        tos = 0
        totalLength = 0
        identification = 0
        flagsAndOffset = 0
        ttl = 64
    }

    val dataLength: Int
        get() = totalLength - headerLength

    var headerLength: Int
        get() = (mData[mOffset + offset_ver_ihl] and 0x0F).toInt() * 4
        set(value) {
            mData[mOffset + offset_ver_ihl] = ((4 shl 4) or (value / 4)).toByte()
        }

    var tos: Byte
        get() = mData[mOffset + offset_tos]
        set(value) {
            mData[mOffset + offset_tos] = value
        }

    var totalLength: Int
        get() = CommonMethods.readShort(mData, mOffset + offset_tlen).toInt() and 0xFFFF
        set(value) {
            CommonMethods.writeShort(mData, mOffset + offset_tlen, value.toShort())
        }

    var identification: Int
        get() = CommonMethods.readShort(mData, mOffset + offset_identification).toInt() and 0xFFFF
        set(value) {
            CommonMethods.writeShort(mData, mOffset + offset_identification, value.toShort())
        }

    var flagsAndOffset: Short
        get() = CommonMethods.readShort(mData, mOffset + offset_flags_fo)
        set(value) {
            CommonMethods.writeShort(mData, mOffset + offset_flags_fo, value)
        }

    var ttl: Byte
        get() = mData[mOffset + offset_ttl]
        set(value) {
            mData[mOffset + offset_ttl] = value
        }

    var protocol: Byte
        get() = mData[mOffset + offset_proto]
        set(value) {
            mData[mOffset + offset_proto] = value
        }

    var crc: Short
        get() = CommonMethods.readShort(mData, mOffset + offset_crc)
        set(value) {
            CommonMethods.writeShort(mData, mOffset + offset_crc, value)
        }

    var sourceIP: Int
        get() = CommonMethods.readInt(mData, mOffset + offset_src_ip)
        set(value) {
            CommonMethods.writeInt(mData, mOffset + offset_src_ip, value)
        }

    var destinationIP: Int
        get() = CommonMethods.readInt(mData, mOffset + offset_dest_ip)
        set(value) {
            CommonMethods.writeInt(mData, mOffset + offset_dest_ip, value)
        }

    override fun toString(): String {
        return "${CommonMethods.ipIntToString(sourceIP)}->${CommonMethods.ipIntToString(destinationIP)} " +
                "Pro=$protocol,HLen=$headerLength"
    }
}
