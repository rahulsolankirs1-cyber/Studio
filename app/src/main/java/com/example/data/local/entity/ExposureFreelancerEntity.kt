package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exposure_freelancers")
data class ExposureFreelancerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String, // Cinematographer, Traditional Photographer, Drone Pilot, Album Editor, Crane Operator, Candid Photographer, Lighting Tech
    val contactNumber: String,
    val address: String = "",
    val defaultDailyRate: Double = 0.0,
    val defaultShiftRate: Double = 0.0,
    val upiId: String = "",
    val notes: String = "",
    val isActive: Boolean = true
)
