package com.example.myapplication.vpn.dnsproxy

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.util.Log
import java.net.InetAddress

class DnsHelper( val context: Context) {
    var currentDnsServers: List<InetAddress> = emptyList()

    fun retrieveAndStoreCurrentDns() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        if (activeNetwork != null) {
            val linkProperties: LinkProperties? = cm.getLinkProperties(activeNetwork)
            currentDnsServers = linkProperties?.dnsServers ?: emptyList()

            currentDnsServers.forEachIndexed { index, dns ->
                Log.d("DNSHelper", "DNS Server $index: ${dns.hostAddress} (${if (dns.address.size == 4) "IPv4" else "IPv6"})")
            }

            if (currentDnsServers.isEmpty()) {
                Log.d("DNSHelper", "No DNS servers found")
            }
        } else {
            Log.d("DNSHelper", "No active network")
        }
    }
}