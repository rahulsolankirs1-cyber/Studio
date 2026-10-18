package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "booking_equipment_allocations",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookingId"]),
        Index(value = ["equipmentId"]),
        Index(value = ["equipmentId", "date"])
    ]
)
data class BookingEquipmentAllocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: Long,
    val equipmentId: Long,
    val equipmentName: String,
    val category: String,
    val date: String, // YYYY-MM-DD
    val sessionTitle: String = ""
)
