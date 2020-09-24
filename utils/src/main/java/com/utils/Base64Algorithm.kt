package com.utils

import java.util.*

class Base64Algorithm {
    companion object {
        @JvmStatic
        fun encode(content: String): String {
            return String(Base64.getEncoder().encode(content.toByteArray()))
        }
        @JvmStatic
        fun decode(content: String): String {
            return String(Base64.getDecoder().decode(content.toByteArray()))
        }
    }
}