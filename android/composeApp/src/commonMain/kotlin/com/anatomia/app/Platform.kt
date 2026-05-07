package com.anatomia.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform