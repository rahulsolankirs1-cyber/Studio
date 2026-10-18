package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "booking_deliverables",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookingId"])]
)
data class BookingDeliverableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: Long,
    val title: String, // e.g. "Candid Photography", "Cinematic Teaser (3-5 min)", "Traditional Video", "Traditional Album (40 Sheets)"
    val quantity: Int = 1,
    val isCompleted: Boolean = false,
    val estimatedDeliveryDate: String = ""
)
