package com.doubleu.muniq

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform