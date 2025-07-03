package com.example.myapplication.vpn.dnsproxy

import android.util.Log
import android.util.SparseArray
import androidx.core.util.size
import com.example.myapplication.vpn.VPNService
import com.example.myapplication.vpn.tcpip.CommonMethods
import com.example.myapplication.vpn.tcpip.UDPHeader
import com.example.myapplication.vpn.tcpip.IPHeader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class DnsProxy(private val service: VPNService) : Thread() {

    companion object{
        val dummyIptodomain = mutableMapOf<String, String>()
    }

    private val domainlist: List<String> = listOf(
        "example.com",
        "test.com",
        "mydomain.org"
    )
    private val nxdomainBlockList: List<String> = listOf(
        "blockeddomain.com",
        "reddit.com"
    )
    private val reserved10SeriesIps = mutableSetOf<String>()
    private val domaintodummyIP = mutableMapOf<String, String>()

    private val queryArray = SparseArray<QueryState>()
    private val querytimeoutns = 10_000_000_000L
    private var socket: DatagramSocket? = null
    private var queryId: Short = 0

    init {
        try {
            socket = DatagramSocket(0)
            service.protect(socket)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class QueryState {
        var clientQueryId: Short = 0
        var clientPort: Short = 0
        var remotePort: Short = 0
        var clientAddress: Int = 0
        var remoteAddress: Int = 0
        var queryNanoTime: Long = 0
    }

    override fun run() {
        try {
            val receivebuffer = ByteArray(2000)
            val ipHeader = IPHeader(receivebuffer, 0)
            ipHeader.setDefault()
            val udpHeader = UDPHeader(receivebuffer, 20)

            var dnsBuffer = ByteBuffer.wrap(receivebuffer)
            dnsBuffer.position(28)
            dnsBuffer = dnsBuffer.slice()

            val packet = DatagramPacket(receivebuffer, 28, receivebuffer.size - 28)

            while (socket != null && !socket!!.isClosed) {
                packet.length = receivebuffer.size - 28
                socket!!.receive(packet)  //FIXME : BLOCKING CALL

                dnsBuffer.clear()
                dnsBuffer.limit(packet.length)

                try {
                    val dnsPacket = DnsPacket.Companion.fromBytes(dnsBuffer)
                    if (dnsPacket != null) {
                        onDnsResponseReceived(ipHeader, udpHeader, dnsPacket)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            socket?.close()
        }
    }
    private fun onDnsResponseReceived(
        ipHeader: IPHeader,
        udpHeader: UDPHeader,
        dnsPacket: DnsPacket
    ) {
        Log.d("onDnsResponseReceived", "Received DNS response with ID: ${dnsPacket.header?.id}")
        prettyPrintUDPPacket(ipHeader, udpHeader, dnsPacket, "onDnsResponseReceived")

        dnsPacket.resources?.forEach { resource ->
            val ipBytes = resource.data
            if (ipBytes.size == 4) {
                val ip = InetAddress.getByAddress(ipBytes).hostAddress
                if (ip != null) {
                    if (ip.startsWith("10.")) {
                        reserved10SeriesIps.add(ip)
                        Log.d("DnsProxy", "Reserved system 10.x IP detected: $ip")
                    }
                }
            }
        }

        val state: QueryState?
        synchronized(queryArray) {
            state = queryArray.get(dnsPacket.header?.id?.toInt() ?: 0)
            if (state != null) {
                queryArray.remove(dnsPacket.header?.id?.toInt() ?: 0)
            }
        }

        state?.let {
            dnsPacket.header?.id = it.clientQueryId
            ipHeader.sourceIP      = it.remoteAddress
            ipHeader.destinationIP = it.clientAddress
            ipHeader.protocol      = IPHeader.Companion.UDP
            ipHeader.totalLength   = 20 + 8 + dnsPacket.size

            udpHeader.sourcePort      = it.remotePort
            udpHeader.destinationPort = it.clientPort
            udpHeader.totalLength     = 8 + dnsPacket.size

            val tempBuffer = ByteArray(2048)
            val wrapper = ByteBuffer.wrap(tempBuffer)
            dnsPacket.toBytes(wrapper)
            val packetSize = wrapper.position()
            System.arraycopy(
                tempBuffer,
                0,
                udpHeader.mData,
                udpHeader.mOffset + 8,
                packetSize
            )
            CommonMethods.computeUDPChecksum(ipHeader, udpHeader)
            Log.d("onDnsResponseReceived", "Sending following DNS response to client: ${CommonMethods.ipIntToString(it.clientAddress)}:${it.clientPort}")
            prettyPrintUDPPacket(ipHeader, udpHeader, dnsPacket, "DNS-Response-AfterRewrite")
            service.sendUDPPacket(ipHeader, udpHeader)
        }
    }
    private fun clearExpiredQueries() {
        val now = System.nanoTime()
        for (i in queryArray.size - 1 downTo 0) {
            val state = queryArray.valueAt(i)
            if (now - state.queryNanoTime > querytimeoutns) {
                queryArray.removeAt(i)
            }
        }
    }
fun onDnsRequestReceived(ipHeader: IPHeader, udpHeader: UDPHeader, dnsPacket: DnsPacket) {
    Log.d("DnsProxy", "Current domainlist: $domainlist")
    val domain = dnsPacket.questions?.get(0)?.domain ?: return

    if (nxdomainBlockList.contains(domain)) {
        Log.d("DNSproxy", "Blocking domain '$domain' with NXDOMAIN")
        val responsePacket = createDnsResponseWithNxDomain(dnsPacket)

        val originalSourceIP = ipHeader.sourceIP
        val originalDestinationIP = ipHeader.destinationIP
        val originalSourcePort = udpHeader.sourcePort

        ipHeader.sourceIP = originalDestinationIP
        ipHeader.destinationIP = originalSourceIP
        ipHeader.protocol = IPHeader.Companion.UDP

        udpHeader.sourcePort = 53
        udpHeader.destinationPort = originalSourcePort

        val tempBuffer = ByteBuffer.allocate(2048)
        responsePacket.toBytes(tempBuffer)
        val dnsSize = tempBuffer.position()
        val dnsBytes = ByteArray(dnsSize)
        tempBuffer.flip()
        tempBuffer.get(dnsBytes)

        udpHeader.totalLength = 8 + dnsBytes.size
        ipHeader.totalLength = 20 + udpHeader.totalLength

        System.arraycopy(dnsBytes, 0, udpHeader.mData, udpHeader.mOffset + 8, dnsBytes.size)

        CommonMethods.computeUDPChecksum(ipHeader, udpHeader)
        service.sendUDPPacket(ipHeader, udpHeader)
        return
    }

    if (domainlist.contains(domain)) {
        Log.d("DOMAIN FOUND","Following packet received . CASE IS DOMAIN FOUND  :")
        prettyPrintUDPPacket(ipHeader, udpHeader, dnsPacket, "onDnsRequestReceived")
        val dummyIp = domaintodummyIP.getOrPut(domain) {
            val ip = getDummyIpForDomain(domain)
            Log.d("DNSproxy", "Mapping domain '$domain' to dummy IP '$ip'")
            dummyIptodomain[ip] = domain
            ip
        }

        val responsePacket = createDnsResponseWithIp(dnsPacket, dummyIp)

        val originalSourceIP = ipHeader.sourceIP
        val originalDestinationIP = ipHeader.destinationIP
        val originalSourcePort = udpHeader.sourcePort

        ipHeader.sourceIP = originalDestinationIP
        ipHeader.destinationIP = originalSourceIP
        ipHeader.protocol = IPHeader.Companion.UDP

        udpHeader.sourcePort = 53
        udpHeader.destinationPort = originalSourcePort

        val tempBuffer = ByteBuffer.allocate(2048)
        responsePacket.toBytes(tempBuffer)
        val dnsSize = tempBuffer.position()
        Log.d("DNSproxy", "Allocating DNS buffer of size: $dnsSize")
        val dnsBytes = ByteArray(dnsSize)
        tempBuffer.flip()
        tempBuffer.get(dnsBytes)

        udpHeader.totalLength = 8 + dnsBytes.size
        ipHeader.totalLength = 20 + udpHeader.totalLength

        System.arraycopy(dnsBytes, 0, udpHeader.mData, udpHeader.mOffset + 8, dnsBytes.size)

        CommonMethods.computeUDPChecksum(ipHeader, udpHeader)
        Log.d("DOMAIN FOUND", "Packet looks Like this before calling sendUDPPacket:")
        prettyPrintUDPPacket(ipHeader, udpHeader, responsePacket, "DOMAIN FOUND")
        service.sendUDPPacket(ipHeader, udpHeader)
    }

    else{
        Log.d("DOMAIN NOT FOUND","Following packet received . CASE IS DOMAIN NOT FOUND IN LIST:")
        prettyPrintUDPPacket(ipHeader, udpHeader, dnsPacket, "DOMAIN NOT FOUND")
        val state = QueryState()
        state.clientQueryId = dnsPacket.header?.id ?: 0
        state.queryNanoTime = System.nanoTime()
        state.clientAddress = ipHeader.sourceIP
        state.clientPort = udpHeader.sourcePort

        val ipv4Dns = service.dnsHelper.currentDnsServers
            .filterIsInstance<Inet4Address>()
            .firstOrNull()

        if (ipv4Dns != null) {
            state.remoteAddress = CommonMethods.inet4AddressToInt(ipv4Dns)
            state.remotePort = 53
        } else {
            state.remoteAddress = ipHeader.destinationIP
            state.remotePort = udpHeader.destinationPort
        }

        queryId++
        dnsPacket.header?.id = queryId

        synchronized(queryArray) {
            clearExpiredQueries()
            queryArray.put(queryId.toInt(), state)
        }
        ipHeader.destinationIP = state.remoteAddress
        ipHeader.sourceIP      = state.clientAddress
        udpHeader.sourcePort      = state.clientPort
        udpHeader.destinationPort = state.remotePort
        ipHeader.protocol      = IPHeader.Companion.UDP
        ipHeader.totalLength   = 20 + 8 + dnsPacket.size
        udpHeader.totalLength  =  8 + dnsPacket.size

        val bufferSize = 1024
        val packet = DatagramPacket(ByteArray(bufferSize), bufferSize)
        val buffer = ByteBuffer.wrap(packet.data)

        try {
            dnsPacket.toBytes(buffer)
            val packetSize = buffer.position()
            buffer.flip()

            packet.setLength(packetSize)

            val remoteAddress = InetSocketAddress(
                CommonMethods.ipIntToInet4Address(state.remoteAddress),
                state.remotePort.toInt()
            )
            packet.socketAddress = remoteAddress

            Log.d("DOMAIN NOT FOUND", "Sending DNS request to upstream: $remoteAddress, ID: $queryId")
            prettyPrintUDPPacket(ipHeader, udpHeader, dnsPacket, "DOMAIN NOT FOUND")
            socket?.send(packet)
        } catch (e: Exception) {
            Log.e("DNSproxy", "Error sending DNS request", e)
            queryArray.remove(queryId.toInt())
        }
    }

}

    fun createDnsResponseWithIp(request: DnsPacket, ip: String): DnsPacket {
        val response = DnsPacket()
        response.header = request.header?.let { h ->
            DnsHeader(ByteArray(12), 0).apply {
                id = h.id
                flags = DnsFlags().apply {
                    QR = true
                    RA = true
                    RD = h.flags.RD == true
                }
                questionCount = 1
                resourceCount = 1
                aResourceCount = 0
                eResourceCount = 0
            }
        }

        response.questions = request.questions?.map { q ->
            Question().apply {
                domain = q.domain
                type = q.type
                clazz = q.clazz
            }
        }?.toTypedArray()

        val domain = request.questions?.firstOrNull()?.domain ?: ""
        val answer = Resource().apply {
            this.domain = domain
            this.type = 1 // A
            this.clazz = 1 // IN
            this.ttl = 60
            this.data = InetAddress.getByName(ip).address
        }
        response.resources = arrayOf(answer)
        response.aResources = emptyArray()
        response.eResources = emptyArray()

        return response
    }
    private fun createDnsResponseWithNxDomain(request: DnsPacket): DnsPacket {
        val response = DnsPacket()
        response.header = request.header?.let { h ->
            DnsHeader(ByteArray(12), 0).apply {
                id = h.id
                flags = DnsFlags().apply {
                    QR = true
                    RA = true
                    RD = h.flags.RD == true
                    Rcode = 3 // NXDOMAIN
                }
                questionCount = 1
                resourceCount = 0
                aResourceCount = 0
                eResourceCount = 0
            }
        }
        response.questions = request.questions?.map { q ->
            Question().apply {
                domain = q.domain
                type = q.type
                clazz = q.clazz
            }
        }?.toTypedArray()
        response.resources = emptyArray()
        response.aResources = emptyArray()
        response.eResources = emptyArray()
        return response
    }

    fun prettyPrintUDPPacket(
        ipHeader: IPHeader,
        udpHeader: UDPHeader,
        dnsPacket: DnsPacket?,
        tag: String = "UDPPacketView"
    ) {
        Log.d(tag, "----------------------------")
        Log.d(tag, "IP Packet Details:")
        Log.d(tag, "Source IP: ${CommonMethods.ipIntToString(ipHeader.sourceIP)}")
        Log.d(tag, "Destination IP: ${CommonMethods.ipIntToString(ipHeader.destinationIP)}")
        Log.d(tag, "Protocol: ${ipHeader.protocol}")
        Log.d(tag, "Total Length: ${ipHeader.totalLength}")
        Log.d(tag, "----------------------------")
        Log.d(tag, "UDP Segment Details:")
        Log.d(tag, "Source Port: ${udpHeader.sourcePort}")
        Log.d(tag, "Destination Port: ${udpHeader.destinationPort}")
        Log.d(tag, "Length: ${udpHeader.totalLength}")
        Log.d(tag, "----------------------------")
        if (dnsPacket != null) {
            Log.d(tag, "DNS Header: id=${dnsPacket.header?.id}, flags=${dnsPacket.header?.flags}")
            Log.d(tag, "Questions: ${dnsPacket.questions?.size ?: 0}")
            dnsPacket.questions?.forEachIndexed { i, q ->
                Log.d(tag, "Q[$i]: ${q.domain} type=${q.type} class=${q.clazz}")
            }
            Log.d(tag, "Answers: ${dnsPacket.resources?.size ?: 0}")
            dnsPacket.resources?.forEachIndexed { i, r ->
                Log.d(tag, "A[$i]: ${r.domain} type=${r.type} class=${r.clazz} ttl=${r.ttl} data=${r.data.contentToString()}")
            }
            Log.d(tag, "Authority: ${dnsPacket.aResources?.size ?: 0}")
            Log.d(tag, "Additional: ${dnsPacket.eResources?.size ?: 0}")
        } else {
            Log.d(tag, "No DNS data parsed.")
        }
        Log.d(tag, "----------------------------")
    }
}

private fun getDummyIpForDomain(domain: String): String {
    val hash = domain.hashCode()
    val b = (hash shr 16) and 0xFF
    val c = (hash shr 8) and 0xFF
    val d = hash and 0xFF
    return "10.$b.$c.$d"
}
