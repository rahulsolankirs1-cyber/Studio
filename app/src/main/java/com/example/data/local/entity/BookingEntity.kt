package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val clientContact: String,
    val eventType: String, // Wedding, Pre-Wedding, Reception, Ring Ceremony, Corporate, Birthday
    val status: String, // QUOTATION, CONFIRMED, COMPLETED, CANCELLED
    val notes: String = "",
    val totalAmount: Double = 0.0,
    val discountType: String = "FLAT", // FLAT or PERCENTAGE
    val discountValue: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxEnabled: Boolean = false,
    val taxPercent: Double = 18.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val advancePaid: Double = 0.0,
    val balanceDue: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
