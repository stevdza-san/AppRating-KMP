package com.stevdza_san.demo.util

enum class Platform {
    ANDROID,
    IOS,
    DESKTOP,
}

expect fun getPlatform(): Platform