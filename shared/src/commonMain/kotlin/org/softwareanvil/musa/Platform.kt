package org.softwareanvil.musa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform