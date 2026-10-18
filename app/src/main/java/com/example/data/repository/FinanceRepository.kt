package com.example.data.repository

import com.example.data.local.dao.BookingDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.PaymentLedgerDao
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.PaymentLedgerEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val paymentLedgerDao: PaymentLedgerDao,
    private val invoiceDao: InvoiceDao,
    private val bookingDao: BookingDao
) {
    val allPayments: Flow<List<PaymentLedgerEntity>> = paymentLedgerDao.getAllPayments()
    val customerPayments: Flow<List<PaymentLedgerEntity>> = paymentLedgerDao.getCustomerPayments()
    val exposurePayouts: Flow<List<PaymentLedgerEntity>> = paymentLedgerDao.getExposurePayouts()
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val allQuotations: Flow<List<InvoiceEntity>> = invoiceDao.getAllQuotations()
    val taxAndStandardInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllTaxAndStandardInvoices()

    fun getPaymentsForBooking(bookingId: Long): Flow<List<PaymentLedgerEntity>> =
        paymentLedgerDao.getPaymentsForBooking(bookingId)

    fun getInvoiceForBooking(bookingId: Long): Flow<InvoiceEntity?> =
        invoiceDao.getInvoiceForBooking(bookingId)

    suspend fun getInvoiceForBookingDirect(bookingId: Long): InvoiceEntity? =
        invoiceDao.getInvoiceForBookingDirect(bookingId)

    suspend fun recordCustomerPayment(payment: PaymentLedgerEntity): Long {
        val paymentId = paymentLedgerDao.insertPayment(payment)
        // Reconcile booking balance if bookingId is provided
        if (payment.bookingId != null) {
            val booking = bookingDao.getBookingByIdDirect(payment.bookingId)
            if (booking != null) {
                val allPayments = paymentLedgerDao.getPaymentsForBookingDirect(payment.bookingId)
                val totalPaid = allPayments.sumOf { it.amount }
                val newBalance = (booking.grandTotal - totalPaid).coerceAtLeast(0.0)
                bookingDao.updateBooking(
                    booking.copy(
                        advancePaid = totalPaid,
                        balanceDue = newBalance
                    )
                )
            }
        }
        return paymentId
    }

    suspend fun recordCrewPayout(payment: PaymentLedgerEntity): Long {
        return paymentLedgerDao.insertPayment(payment)
    }

    suspend fun updateInvoice(invoice: InvoiceEntity) {
        invoiceDao.updateInvoice(invoice)
    }

    suspend fun deletePayment(payment: PaymentLedgerEntity) {
        paymentLedgerDao.deletePayment(payment)
        if (payment.bookingId != null) {
            val booking = bookingDao.getBookingByIdDirect(payment.bookingId)
            if (booking != null) {
                val allPayments = paymentLedgerDao.getPaymentsForBookingDirect(payment.bookingId)
                val totalPaid = allPayments.sumOf { it.amount }
                val newBalance = (booking.grandTotal - totalPaid).coerceAtLeast(0.0)
                bookingDao.updateBooking(
                    booking.copy(
                        advancePaid = totalPaid,
                        balanceDue = newBalance
                    )
                )
            }
        }
    }
}
