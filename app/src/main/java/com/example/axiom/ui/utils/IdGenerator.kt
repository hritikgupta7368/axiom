package com.example.axiom.ui.utils

import java.util.UUID

object IdGenerator {

    /**
     * Stable, collision-safe, offline-capable.
     * Suitable for Room primary keys.
     */
    fun newId(): String = UUID.randomUUID().toString()
}