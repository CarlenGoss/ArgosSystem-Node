package com.argossystem.node.utils

import java.net.NetworkInterface

object NetworkUtils {
    fun getTailscaleIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        // ⚡ Agregamos el Elvis operator (?: "") para asegurar que no sea null
                        val sAddr = addr.hostAddress ?: ""

                        // Solo revisamos si es IPv4 (evita errores con IPv6)
                        if (!sAddr.contains(":") && sAddr.startsWith("100.")) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (ex: Exception) { ex.printStackTrace() }
        return "127.0.0.1"
    }
}