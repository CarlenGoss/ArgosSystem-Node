# 🎥 ArgosSystem Node (El Centinela)

**ArgosSystem Node** es el módulo de captura y emisión del ecosistema de seguridad privada **ArgosSystem**. Su objetivo es convertir dispositivos Android en desuso en cámaras de videovigilancia IP, procesando y transmitiendo video en tiempo real dentro de una red local sin depender de servidores en la nube de terceros.

## ✨ Características Principales

* **Emparejamiento Inteligente:** Generación dinámica de códigos QR (mediante ZXing) que exponen la IP privada del dispositivo y su modelo para una conexión instantánea con el visor (Hub).
* **Motor de Captura Eficiente:** Integración con **CameraX** para gestionar el ciclo de vida del sensor fotográfico, asegurando estabilidad y optimización de recursos de hardware.
* **Servidor Local Autónomo:** Utiliza **Ktor Server** y el motor **Netty** para levantar un microservicio interno en el teléfono capaz de despachar el streaming de video.
* **Privacidad por Diseño:** Lógica de red configurada para detectar y operar exclusivamente sobre interfaces de redes privadas virtuales (VPN) como **Tailscale** (rango `100.x.x.x`).

## 🛠️ Stack Tecnológico

* **Lenguaje:** Kotlin
* **Arquitectura UI:** Jetpack Compose (Material 3)
* **Hardware Interop:** AndroidX CameraX
* **Redes y Servidor:** Ktor Server Core / Netty
* **Decodificación:** Google ZXing Core

## 📦 Instalación y Uso

1. Navega a la sección de **[Releases](../../releases)** de este repositorio.
2. Descarga el archivo ejecutable `ArgosSystem-Node-v1.0-alpha.apk`.
3. Instálalo en un dispositivo Android secundario (Requiere **Android 8.0 Oreo** / API 26 o superior).
4. Asegúrate de que el dispositivo esté conectado a tu malla de Tailscale.
5. Abre la aplicación y utiliza el **ArgosSystem Hub** en tu teléfono principal para escanear el QR generado en pantalla.
