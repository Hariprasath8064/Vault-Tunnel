package com.example.myapplication.vpn.dnsproxy

class DnsFlags {
    var QR: Boolean = false // 1 bit
    var OpCode: Int = 0     // 4 bits
    var AA: Boolean = false // 1 bit
    var TC: Boolean = false // 1 bit
    var RD: Boolean = false // 1 bit
    var RA: Boolean = false // 1 bit
    var Zero: Int = 0       // 3 bits
    var Rcode: Int = 0      // 4 bits

    companion object {
        fun parse(value: Short): DnsFlags {
            val mFlags = value.toInt() and 0xFFFF
            return DnsFlags().apply {
                QR = (mFlags shr 7 and 0x01) == 1
                OpCode = (mFlags shr 3) and 0x0F
                AA = (mFlags shr 2 and 0x01) == 1
                TC = (mFlags shr 1 and 0x01) == 1
                RD = (mFlags and 0x01) == 1
                RA = (mFlags shr 15) == 1
                Zero = (mFlags shr 12) and 0x07
                Rcode = (mFlags shr 8) and 0x0F
            }
        }
    }

    fun toShort(): Short {
        var mFlags = 0
        mFlags = mFlags or ((if (QR) 1 else 0) shl 7)
        mFlags = mFlags or ((OpCode and 0x0F) shl 3)
        mFlags = mFlags or ((if (AA) 1 else 0) shl 2)
        mFlags = mFlags or ((if (TC) 1 else 0) shl 1)
        mFlags = mFlags or (if (RD) 1 else 0)
        mFlags = mFlags or ((if (RA) 1 else 0) shl 15)
        mFlags = mFlags or ((Zero and 0x07) shl 12)
        mFlags = mFlags or ((Rcode and 0x0F) shl 8)
        return mFlags.toShort()
    }
}