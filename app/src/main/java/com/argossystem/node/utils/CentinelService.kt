package com.argossystem.node.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.argossystem.node.R

class CentinelService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        // Construimos la notificación inamovible
        val notification = NotificationCompat.Builder(this, "CENTINEL_CHANNEL")
            .setContentTitle("ArgosSystem Node")
            .setContentText("Centinela Activo - Transmitiendo video 🎥")
            .setSmallIcon(R.mipmap.ic_launcher) // Usamos el icono de tu app
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Iniciamos el servicio atado a la notificación (ID 1)
        startForeground(1, notification)

        // START_STICKY le dice a Android que si por alguna razón extrema lo cierra, lo vuelva a abrir
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CENTINEL_CHANNEL",
                "Servicio de Centinela",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}