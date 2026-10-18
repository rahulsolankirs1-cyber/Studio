package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE isQuotation = 1 ORDER BY createdAt DESC")
    fun getAllQuotations(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE isQuotation = 0 ORDER BY createdAt DESC")
    fun getAllTaxAndStandardInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE bookingId = :bookingId LIMIT 1")
    fun getInvoiceForBooking(bookingId: Long): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getInvoiceForBookingDirect(bookingId: Long): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :number LIMIT 1")
    suspend fun getInvoiceByNumber(number: String): InvoiceEntity?

    @Query("SELECT * FROM invoices ORDER BY id ASC")
    suspend fun getAllInvoicesDirect(): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices")
    suspend fun deleteAllInvoices()
}
