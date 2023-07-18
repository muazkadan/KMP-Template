package dev.muazkadan.myapplication

class WebpPlatform: Platform {
    override val name: String = "Web"
}

actual fun getPlatform(): Platform = WebpPlatform()