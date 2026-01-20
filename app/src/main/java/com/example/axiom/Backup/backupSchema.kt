package com.example.axiom.Backup

import kotlinx.serialization.Serializable

@Serializable
data class VaultEntryBackup(
    val id: Long,
    val serviceIcon: String?,
    val serviceName: String,
    val username: String,
    val password: String,
    val note: String?,
    val expiryDate: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
