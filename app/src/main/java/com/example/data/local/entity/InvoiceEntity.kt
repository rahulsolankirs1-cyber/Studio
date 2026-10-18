package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookingId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: Long,
    val invoiceNumber: String, // e.g., "QUO-2026-101" or "INV-2026-042"
    val isQuotation: Boolean = false,
    val issueDate: String, // YYYY-MM-DD
    val dueDate: String = "", // YYYY-MM-DD
    val taxEnabled: Boolean = false,
    val taxPercentage: Double = 18.0,
    val termsAndConditions: String = "",
    val footerNotes: String = "",
    val pdfFilePath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
