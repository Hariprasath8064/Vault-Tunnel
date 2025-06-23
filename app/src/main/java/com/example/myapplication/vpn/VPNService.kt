package com.example.myapplication.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.myapplication.vpn.dnsproxy.DnsHelper
import com.example.myapplication.vpn.dnsproxy.DnsProxy
import com.example.myapplication.vpn.proxy.Proxy
import com.example.myapplication.vpn.tcpip.CommonMethods
import com.example.myapplication.vpn.tcpip.IPHeader
import com.example.myapplication.vpn.tcpip.TCPHeader
import com.example.myapplication.vpn.tcpip.UDPHeader
import com.example.myapplication.vpn.vpn_helpers.PacketProcessingHelper
import com.example.myapplication.vpn.vpn_helpers.PrettyPrintHelper
import com.example.myapplication.vpn.vpn_helpers.UdpPacketSender
import com.example.myapplication.vpn.vpn_helpers.VpnInterfaceHelper
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

class VPNService : VpnService() {

    interface OnStatusChangedListener {
        fun onStatusChanged(status: Boolean)
    }

    companion object {
        var isRunning = false
        private val statusChangedListeners = mutableListOf<OnStatusChangedListener>()

        fun addOnStatusChangedListener(listener: OnStatusChangedListener) {
            if (!statusChangedListeners.contains(listener)) {
                statusChangedListeners.add(listener)
            }
        }

        fun prepare(context: Context): Intent? {
            return VpnService.prepare(context)
        }

        fun notifyStatusChanged(status: Boolean) {
            for (listener in statusChangedListeners) {
                listener.onStatusChanged(status)
            }
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val builder = Builder()
    private var localAddress = 0
    private var vpnOut: FileOutputStream? = null
    private var isStopping = false

    private lateinit var dnsProxy: DnsProxy
    private lateinit var proxy: Proxy
    internal lateinit var dnsHelper: DnsHelper
    private val packet = ByteArray(VpnConstants.PACKET_BUFFER_SIZE)
    private val ipHeader = IPHeader(packet, 0)
    private val tcpHeader = TCPHeader(packet, 20)
    private val udpHeader = UDPHeader(packet, 20)

    private var packetCount = 0
    private var vpnJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == VpnConstants.ACTION_STOP_VPN) {
            Log.d(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_STOP_COMMAND)
            kill()
            return START_NOT_STICKY
        }

        isRunning = true
        localAddress = CommonMethods.ipStringToInt(VpnConstants.VPN_ADDRESS)
        packetCount = 0
        Log.i(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_STARTING)

        proxy = Proxy(this, 0).also { it.start() }
        dnsProxy = DnsProxy(this).also { it.start() }
        dnsHelper = DnsHelper(this)
        dnsHelper.retrieveAndStoreCurrentDns()

        vpnJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                vpnInterface = VpnInterfaceHelper.establishVpnInterface(this@VPNService, builder, dnsHelper)
                if (vpnInterface == null) {
                    Log.e(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_ESTABLISH_FAILED)
                    stopSelf()
                    return@launch
                } else {
                    Log.i(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_ESTABLISHED)
                    notifyStatusChanged(true)
                }

                vpnInterface?.fileDescriptor?.let { fd ->
                    FileInputStream(fd).use { vpnIn ->
                        vpnOut = FileOutputStream(fd)
                        val inputBuffer = packet
                        while (isActive) {
                            try {
                                val size = vpnIn.read(inputBuffer)
                                if (size > 0) {
                                    packetCount++
                                    PacketProcessingHelper.processIPPacket(
                                        ipHeader, size, localAddress, packetCount, proxy, dnsProxy, dnsHelper, vpnOut, packet, tcpHeader, udpHeader,
                                        PrettyPrintHelper::prettyPrintTCPPacket
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_READ_ERROR, e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(VpnConstants.TAG_VPN_SERVICE, String.Companion.format(VpnConstants.LOG_ERROR_VPN_JOB, e.localizedMessage))
                e.printStackTrace()
            } finally {
                VpnInterfaceHelper.cleanupVpnInterface(vpnOut, vpnInterface)
                vpnInterface = null
                kill()
                isRunning = false
                notifyStatusChanged(false)
            }
        }

        return START_STICKY
    }

    fun sendUDPPacket(ipHeader: IPHeader, udpHeader: UDPHeader) {
        UdpPacketSender.sendUDPPacket(ipHeader, udpHeader, vpnOut)
    }

    override fun onDestroy() {
        Log.d(VpnConstants.TAG_VPN_SERVICE, VpnConstants.LOG_ON_DESTROY)
        kill()
        super.onDestroy()
    }

    fun kill() {
        if (isStopping) return
        isStopping = true

        vpnJob?.cancel()
        vpnJob = null

        if (::proxy.isInitialized) {
            proxy.interrupt()
        }

        if (::dnsProxy.isInitialized) {
            dnsProxy.interrupt()
        }

        stopSelf()
        notifyStatusChanged(false)
        isRunning = false
    }

    fun prettyPrintTCPPacket(packet: ByteArray) {
        PrettyPrintHelper.prettyPrintTCPPacket(packet)
    }
}