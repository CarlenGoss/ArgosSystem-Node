package com.argossystem.node.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as? android.app.Activity

    var isGhostMode by remember { mutableStateOf(false) }

    LaunchedEffect(isGhostMode) {
        val window = activity?.window
        if (isGhostMode) {
            window?.attributes = window?.attributes?.apply { screenBrightness = 0f }
        } else {
            window?.attributes = window?.attributes?.apply { screenBrightness = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE }
        }
    }

    // --- ESTAS ERAN LAS VARIABLES QUE SE HABÍAN BORRADO ---
    var hasCameraPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        val serviceIntent = android.content.Intent(context, com.argossystem.node.utils.CentinelService::class.java)
        androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)
        com.argossystem.node.utils.VideoServer.start()

        onDispose {
            com.argossystem.node.utils.VideoServer.stop()
            context.stopService(serviceIntent)
        }
    }
    // --------------------------------------------------------

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = androidx.camera.view.PreviewView(ctx)
                    startCamera(ctx, lifecycleOwner, previewView)
                    previewView
                }
            )

            if (isGhostMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = { isGhostMode = false })
                        }
                )
            } else {
                Button(
                    onClick = { isGhostMode = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("👻 Activar Modo Fantasma")
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Esperando permiso de cámara...", color = Color.Gray)
        }
    }
}

private fun startCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView
) {
    val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // ⚡ NUEVO: Extraemos los fotogramas en tiempo real
        val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            // Convertimos la imagen y se la pasamos al servidor
            val jpegBytes = com.argossystem.node.utils.VideoServer.imageProxyToJpeg(imageProxy)
            com.argossystem.node.utils.VideoServer.latestFrame.value = jpegBytes

            imageProxy.close() // IMPORTANTÍSIMO: Liberar el frame para recibir el siguiente
        }

        val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            // ⚡ Fíjate que ahora le pasamos 'imageAnalysis' al final
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (exc: Exception) {
            android.util.Log.e("CameraX", "Error al iniciar la cámara", exc)
        }
    }, androidx.core.content.ContextCompat.getMainExecutor(context))
}