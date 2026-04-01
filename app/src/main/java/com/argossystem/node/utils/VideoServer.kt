package com.argossystem.node.utils

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayOutputStream

object VideoServer {
    val latestFrame = MutableStateFlow<ByteArray?>(null)
    private var server: EmbeddedServer<*, *>? = null

    // ⚡ Variable para el token de seguridad
    var streamToken: String = ""

    fun start() {
        if (server != null) return

        server = embeddedServer(Netty, port = 8080) {
            routing {
                get("/stream") {
                    // 1. Verificamos el token de seguridad
                    val clientToken = call.request.queryParameters["token"]
                    if (clientToken != streamToken) {
                        call.respondText("Acceso Denegado 🛡️", status = HttpStatusCode.Unauthorized)
                        return@get
                    }

                    // 2. Si el token es correcto, enviamos el video
                    val multipartType = ContentType.parse("multipart/x-mixed-replace; boundary=--ArgosBoundary")
                    call.respondBytesWriter(contentType = multipartType) {
                        while (true) {
                            val frame = latestFrame.value
                            if (frame != null) {
                                writeStringUtf8("--ArgosBoundary\r\n")
                                writeStringUtf8("Content-Type: image/jpeg\r\n")
                                writeStringUtf8("Content-Length: ${frame.size}\r\n\r\n")
                                writeFully(frame)
                                writeStringUtf8("\r\n")
                                flush()
                            }
                            delay(50) // ~20 FPS
                        }
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }

    fun imageProxyToJpeg(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 80, out)
        return out.toByteArray()
    }
}