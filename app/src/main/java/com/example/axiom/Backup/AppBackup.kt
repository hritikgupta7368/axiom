package com.example.axiom.Backup

import com.example.axiom.data.vault.VaultEntryEntity
import com.example.axiom.data.temp.TaskEntity
import com.example.axiom.data.temp.EventEntity
import com.example.axiom.data.temp.TaskStatus
import com.example.axiom.data.temp.Priority
import kotlinx.serialization.Serializable

@Serializable
data class AppBackup(
    val meta: BackupMeta,
    val vaultEntries: List<VaultEntryBackup>,        // vault entries
    val tasks: List<TaskBackup>,
    val events: List<EventBackup>                   // 1. add more entities here

)

@Serializable
data class BackupMeta(
    val appVersion: Int,
    val dbVersion: Int,
    val createdAt: Long
)


//Backup Mapper                 2. add mapper funxtions
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
fun TaskEntity.toBackup(): TaskBackup =
    TaskBackup(
        id, title, note, date,
        startTime, endTime, allDay,
        status.name,
        priority.name,
        color,
        recurrenceRule,
        sortIndex,
        timeZone,
        createdAt,
        updatedAt
    )

fun TaskBackup.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        title = title,
        note = note,
        date = date,
        startTime = startTime,
        endTime = endTime,
        allDay = allDay,
        status = TaskStatus.valueOf(status),
        priority = Priority.valueOf(priority),
        color = color,
        recurrenceRule = recurrenceRule,
        sortIndex = sortIndex,
        timeZone = timeZone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun EventEntity.toBackup(): EventBackup =
    EventBackup(
        id, title, description,
        date, startTime, endTime,
        allDay, importance, pinned,
        color, timeZone,
        createdAt, updatedAt
    )

fun EventBackup.toEntity(): EventEntity =
    EventEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        startTime = startTime,
        endTime = endTime,
        allDay = allDay,
        importance = importance,
        pinned = pinned,
        color = color,
        timeZone = timeZone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
