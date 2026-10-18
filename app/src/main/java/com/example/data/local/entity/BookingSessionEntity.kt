package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "booking_sessions",
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
data class BookingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: Long,
    val sessionTitle: String, // e.g., "Ring Ceremony", "Haldi Ritual", "Sangeet & Dance", "Wedding Pheras", "Reception"
    val date: String, // YYYY-MM-DD
    val startTime: String = "",
    val endTime: String = "",
    val venueName: String = "",
    val city: String = "",
    val servicesJson: String = ""
)
