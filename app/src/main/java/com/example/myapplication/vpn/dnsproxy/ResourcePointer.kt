package com.example.myapplication.vpn.dnsproxy

import com.example.myapplication.vpn.tcpip.CommonMethods

class ResourcePointer(private val data: ByteArray, private val offset: Int) {

    companion object {
        const val OFFSET_DOMAIN: Short = 0
        const val OFFSET_TYPE: Short = 2
        const val OFFSET_CLASS: Short = 4
        const val OFFSET_TTL: Int = 6
        const val OFFSET_DATA_LENGTH: Short = 10
        const val OFFSET_IP: Int = 12
    }

    fun setDomain(value: Short) {
        CommonMethods.writeShort(data, offset + OFFSET_DOMAIN, value)
    }

    fun getType(): Short {
        return CommonMethods.readShort(data, offset + OFFSET_TYPE)
    }

    fun setType(value: Short) {
        CommonMethods.writeShort(data, offset + OFFSET_TYPE, value)
    }

    fun getClazz(): Short {
        return CommonMethods.readShort(data, offset + OFFSET_CLASS)
    }

    fun setClazz(value: Short) {
        CommonMethods.writeShort(data, offset + OFFSET_CLASS, value)
    }

    fun getTTL(): Int {
        return CommonMethods.readInt(data, offset + OFFSET_TTL)
    }

    fun setTTL(value: Int) {
        CommonMethods.writeInt(data, offset + OFFSET_TTL, value)
    }

    fun getDataLength(): Short {
        return CommonMethods.readShort(data, offset + OFFSET_DATA_LENGTH)
    }

    fun setDataLength(value: Short) {
        CommonMethods.writeShort(data, offset + OFFSET_DATA_LENGTH, value)
    }

    fun getIP(): Int {
        return CommonMethods.readInt(data, offset + OFFSET_IP)
    }

    fun setIP(value: Int) {
        CommonMethods.writeInt(data, offset + OFFSET_IP, value)
    }
}
