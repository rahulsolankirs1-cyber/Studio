package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment_inventory")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val category: String, // Camera, Lens, Lighting, Drone, Gimbal, Audio, Crane
    val serialNumber: String = "",
    val purchaseDate: String = "",
    val notes: String = "",
    val isAvailable: Boolean = true
)
