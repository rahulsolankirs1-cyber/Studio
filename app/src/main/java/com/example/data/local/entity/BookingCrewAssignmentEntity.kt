package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "booking_crew_assignments",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExposureFreelancerEntity::class,
            parentColumns = ["id"],
            childColumns = ["freelancerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookingId"]),
        Index(value = ["freelancerId"]),
        Index(value = ["freelancerId", "date"])
    ]
)
data class BookingCrewAssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: Long,
    val freelancerId: Long,
    val freelancerName: String,
    val role: String,
    val date: String, // YYYY-MM-DD
    val sessionTitle: String = "",
    val rateType: String = "DAILY", // DAILY, SHIFT, EVENT
    val agreedRate: Double = 0.0,
    val isSettled: Boolean = false,
    val notes: String = ""
)
