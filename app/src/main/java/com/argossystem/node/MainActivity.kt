package com.argossystem.node

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.argossystem.node.ui.camera.CameraPreviewScreen
import com.argossystem.node.ui.theme.ArgosSystemNodeTheme
import com.argossystem.node.utils.NetworkUtils
import com.argossystem.node.utils.QrGenerator
import org.json.JSONObject

// 1. Definimos las pantallas posibles
sealed class NodeScreen {
    object Config : NodeScreen()
    object Preview : NodeScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArgosSystemNodeTheme {
                // 2. Estado para controlar qué pantalla mostrar
                var currentScreen by remember { mutableStateOf<NodeScreen>(NodeScreen.Config) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        is NodeScreen.Config -> {
                            NodeConfigScreen(
                                onStartCentinel = { currentScreen = NodeScreen.Preview }
                            )
                        }
                        is NodeScreen.Preview -> {
                            // Esta es la pantalla que creamos en el archivo CameraPreview.kt
                            CameraPreviewScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NodeConfigScreen(onStartCentinel: () -> Unit) {
    val context = LocalContext.current

    // ⚡ BLINDAJE DE BATERÍA
    LaunchedEffect(Unit) {
        val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
    val deviceName = Build.MODEL
    val ipAddress = remember { NetworkUtils.getTailscaleIp() }

    // ⚡ NUEVO: Generamos un token de 8 caracteres
    val securityToken = remember { java.util.UUID.randomUUID().toString().substring(0, 8) }

    val qrData = remember {
        org.json.JSONObject().apply {
            put("deviceName", "ArgosNode - $deviceName")
            put("ip", ipAddress)
            put("token", securityToken) // Lo metemos al QR
        }.toString()
    }

    // ⚡ NUEVO: Le pasamos el token al servidor antes de iniciar
    com.argossystem.node.utils.VideoServer.streamToken = securityToken

    val qrBitmap = remember { QrGenerator.generateQrCode(qrData) }

    Column(
        modifier = Modifier.padding(24.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Configuración del Nodo 🎥", style = MaterialTheme.typography.headlineMedium)
        Text("Escanea este código desde el Hub", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(32.dp))

        qrBitmap?.let { bitmap ->
            Card(elevation = CardDefaults.cardElevation(8.dp)) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(280.dp).padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Modelo: $deviceName", style = MaterialTheme.typography.labelLarge)
        Text(text = "IP Tailscale: $ipAddress", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(48.dp))

        // ⚡ BOTÓN FINAL: Activa la cámara
        Button(
            onClick = onStartCentinel,
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Iniciar Centinela 🛡️")
        }
    }
}