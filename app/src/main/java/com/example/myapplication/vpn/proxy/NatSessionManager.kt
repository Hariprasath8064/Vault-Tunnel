package com.example.myapplication.vpn.proxy

import android.util.Log
import android.util.SparseArray
import com.example.myapplication.vpn.dnsproxy.DnsProxy
import com.example.myapplication.vpn.tcpip.CommonMethods

object NatSessionManager {

    private val sessions = SparseArray<NatSession>() //FIXME : SPARSE ARRAY IS NOT THREAD SAFE
    @Synchronized
    fun getSession(portKey: Short): NatSession? {
        val session = sessions[portKey.toInt()]
        session?.lastNanoTime = System.nanoTime()
        return session
    }

    @Synchronized
    fun removeSession(portKey: Short) {
        sessions.remove(portKey.toInt())
    }

    @Synchronized
    fun createSession(portKey: Short, remoteIP: Int, remotePort: Short): NatSession {
        val session = NatSession(
            remoteAddress = remoteIP,
            remotePort = remotePort,
            lastNanoTime = System.nanoTime()
        )
        if (session.remoteHost == null) {
            val ipStr = CommonMethods.ipIntToString(remoteIP)
            val domain = DnsProxy.Companion.dummyIptodomain[ipStr]

            if (domain != null) {
                Log.d("NatSessionManager", "Found domain $domain for IP $ipStr")
            } else {
                Log.d("NatSessionManager", "No domain mapping found for IP $ipStr")
            }

            session.remoteHost = domain ?: ipStr
        }
        sessions.put(portKey.toInt(), session)
        return session
    }

    data class NatSession(
        var remoteAddress: Int = 0,
        var remotePort: Short = 0,
        var remoteHost: String? = null,
        var bytesSent: Int = 0,
        var packetsSent: Int = 0,
        var lastNanoTime: Long = 0
    )
}