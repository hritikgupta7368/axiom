package com.example.axiom.Backup

import com.example.axiom.data.vault.VaultEntryEntity
import kotlinx.serialization.Serializable

@Serializable
data class AppBackup(
    val meta: BackupMeta,
    val vaultEntries: List<VaultEntryBackup>
)

@Serializable
data class BackupMeta(
    val appVersion: Int,
    val dbVersion: Int,
    val createdAt: Long
)


//Backup Mapper
fun VaultEntryEntity.toBackup(): VaultEntryBackup =
    VaultEntryBackup(
        id = id,
        serviceIcon = serviceIcon,
        serviceName = serviceName,
        username = username,
        password = password,
        note = note,
        expiryDate = expiryDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun VaultEntryBackup.toEntity(): VaultEntryEntity =
    VaultEntryEntity(
        id = id,
        serviceIcon = serviceIcon,
        serviceName = serviceName,
        username = username,
        password = password,
        note = note,
        expiryDate = expiryDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )