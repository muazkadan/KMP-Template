package dev.muazkadan.myapplication

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
