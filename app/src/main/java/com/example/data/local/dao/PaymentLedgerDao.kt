package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PaymentLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentLedgerDao {
    @Query("SELECT * FROM payment_ledger ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentLedgerEntity>>

    @Query("SELECT * FROM payment_ledger WHERE bookingId = :bookingId ORDER BY createdAt DESC")
    fun getPaymentsForBooking(bookingId: Long): Flow<List<PaymentLedgerEntity>>

    @Query("SELECT * FROM payment_ledger WHERE bookingId = :bookingId ORDER BY createdAt DESC")
    suspend fun getPaymentsForBookingDirect(bookingId: Long): List<PaymentLedgerEntity>

    @Query("SELECT * FROM payment_ledger WHERE type = 'CUSTOMER_INFLOW' ORDER BY createdAt DESC")
    fun getCustomerPayments(): Flow<List<PaymentLedgerEntity>>

    @Query("SELECT * FROM payment_ledger WHERE type = 'EXPOSURE_PAYOUT' ORDER BY createdAt DESC")
    fun getExposurePayouts(): Flow<List<PaymentLedgerEntity>>

    @Query("SELECT * FROM payment_ledger WHERE partyId = :partyId AND type = :type ORDER BY createdAt DESC")
    fun getPaymentsForParty(partyId: Long, type: String): Flow<List<PaymentLedgerEntity>>

    @Query("SELECT * FROM payment_ledger WHERE partyId = :partyId AND type = :type ORDER BY createdAt DESC")
    suspend fun getPaymentsForPartyDirect(partyId: Long, type: String): List<PaymentLedgerEntity>

    @Query("SELECT * FROM payment_ledger ORDER BY id ASC")
    suspend fun getAllPaymentsDirect(): List<PaymentLedgerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentLedgerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentLedgerEntity>)

    @Delete
    suspend fun deletePayment(payment: PaymentLedgerEntity)

    @Query("DELETE FROM payment_ledger")
    suspend fun deleteAllPayments()
}
