package com.example.myapplication.vpn.vpn_helpers

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.myapplication.vpn.VpnConstants
import java.net.Inet4Address
import com.example.myapplication.vpn.dnsproxy.DnsHelper
import java.io.FileOutputStream

object VpnInterfaceHelper {
    fun establishVpnInterface(
        service: VpnService,
        builder: VpnService.Builder,
        dnsHelper: DnsHelper
    ): ParcelFileDescriptor? {
        val ipv4Dns = dnsHelper.currentDnsServers.filterIsInstance<Inet4Address>().firstOrNull()
        Log.d(VpnConstants.TAG_VPN_SERVICE, "ipv4dns is  $ipv4Dns")
        Log.d(VpnConstants.TAG_VPN_SERVICE, "ipv4dns.hostaddress is  ${ipv4Dns?.hostAddress}")
        val dnsIpParts = ipv4Dns?.hostAddress?.split(".")
        val subnet = "${dnsIpParts?.get(0)}.${dnsIpParts?.get(1)}.${dnsIpParts?.get(2)}.0"
        Log.d(VpnConstants.TAG_VPN_SERVICE, "Subnet for DNS is $subnet")
        return builder.setSession(VpnConstants.VPN_SESSION_NAME)
            .setMtu(VpnConstants.VPN_MTU)
            .addAddress(VpnConstants.VPN_ADDRESS, VpnConstants.VPN_ADDRESS_PREFIX)
            .addRoute(VpnConstants.VPN_ROUTE, VpnConstants.VPN_ROUTE_PREFIX)
            .addRoute(subnet, 24)
            .addDnsServer(ipv4Dns?.hostAddress.toString())
            .establish()
    }

    fun cleanupVpnInterface(vpnOut: FileOutputStream?, vpnInterface: ParcelFileDescriptor?) {
        try {
            vpnOut?.close()
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(VpnConstants.TAG_VPN_SERVICE, String.format(VpnConstants.LOG_CLEANUP_ERROR, e.localizedMessage))
        }
    }
}