package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_ledger",
    indices = [
        Index(value = ["bookingId"]),
        Index(value = ["partyId", "type"])
    ]
)
data class PaymentLedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingId: Long? = null,
    val type: String, // "CUSTOMER_INFLOW" or "EXPOSURE_PAYOUT"
    val partyId: Long, // clientId or freelancerId
    val partyName: String,
    val amount: Double,
    val paymentDate: String, // YYYY-MM-DD
    val paymentMode: String, // "UPI", "CASH", "BANK_TRANSFER", "CHEQUE"
    val paymentStage: String = "", // "ADVANCE", "MID_EVENT", "FINAL_DELIVERY", "CREW_SETTLEMENT"
    val referenceNumber: String = "", // UTR / Cheque / Txn ID
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
