// backupSchema.kt

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

@Serializable
data class TaskBackup(
    val id: Long,
    val title: String,
    val note: String?,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val status: String,
    val priority: String,
    val color: Int,
    val recurrenceRule: String?,
    val sortIndex: Int,
    val timeZone: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class EventBackup(
    val id: Long,
    val title: String,
    val description: String?,
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val importance: Int,
    val pinned: Boolean,
    val color: Int,
    val timeZone: String,
    val createdAt: Long,
    val updatedAt: Long
)
