package com.argossystem.node

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.argossystem.node.ui.theme.ArgosSystemNodeTheme
import com.argossystem.node.utils.NetworkUtils
import com.argossystem.node.utils.QrGenerator
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArgosSystemNodeTheme {
                NodeConfigScreen()
            }
        }
    }
}

@Composable
fun NodeConfigScreen() {
    val deviceName = Build.MODEL // Ejemplo: "SM-G973F" (S10)
    val ipAddress = remember { NetworkUtils.getTailscaleIp() }

    // Creamos el JSON que el Hub espera recibir
    val qrData = remember {
        JSONObject().apply {
            put("deviceName", "ArgosNode - $deviceName")
            put("ip", ipAddress)
        }.toString()
    }

    // Generamos el Bitmap del QR
    val qrBitmap = remember { QrGenerator.generateQrCode(qrData) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Configuración del Nodo 🎥",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Escanea este código desde el Hub",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mostramos el QR si se generó correctamente
            qrBitmap?.let { bitmap ->
                Card(
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code de Configuración",
                        modifier = Modifier.size(280.dp).padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info de red para debug
            Text(text = "Nombre: $deviceName", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "IP Tailscale: $ipAddress",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}