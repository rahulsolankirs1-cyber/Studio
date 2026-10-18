package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studio_profile")
data class StudioProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val studioName: String,
    val ownerName: String,
    val mobileNumber: String,
    val email: String = "",
    val fullAddress: String,
    val logoUri: String = "",
    val bankName: String,
    val accountHolderName: String,
    val accountNumber: String,
    val ifscCode: String,
    val upiId: String,
    val upiQrUri: String = "",
    val defaultTermsAndConditions: String,
    val defaultFooterNotes: String,
    val taxPercentage: Double = 18.0
)
