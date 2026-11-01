package com.abyxcz.template.shared

/** Example of the shared layer: plain logic, tested in commonTest, no UI or platform types. */
class Greeter {
    /** The name to greet: the platform's, or a fallback when it has none. */
    fun nameFor(platform: String): String = platform.trim().ifEmpty { FALLBACK }

    companion object {
        const val FALLBACK = "a new platform"
    }
}
