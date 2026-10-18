package com.example.data.repository

import com.example.data.local.dao.BookingDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.entity.BookingCrewAssignmentEntity
import com.example.data.local.entity.BookingDeliverableEntity
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.BookingEquipmentAllocationEntity
import com.example.data.local.entity.BookingSessionEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.domain.model.CrewConflict
import com.example.domain.model.EquipmentConflict
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingRepository(
    private val bookingDao: BookingDao,
    private val invoiceDao: InvoiceDao
) {
    val allBookings: Flow<List<BookingEntity>> = bookingDao.getAllBookings()
    val activeAndPastBookings: Flow<List<BookingEntity>> = bookingDao.getActiveAndPastBookings()
    val allQuotations: Flow<List<BookingEntity>> = bookingDao.getBookingsByStatus("QUOTATION")
    val allSessions: Flow<List<BookingSessionEntity>> = bookingDao.getAllSessions()
    val allCrewAssignments: Flow<List<BookingCrewAssignmentEntity>> = bookingDao.getAllCrewAssignments()
    val allEquipmentAllocations: Flow<List<BookingEquipmentAllocationEntity>> = bookingDao.getAllEquipmentAllocations()

    fun getBookingById(id: Long): Flow<BookingEntity?> = bookingDao.getBookingById(id)
    suspend fun getBookingByIdDirect(id: Long): BookingEntity? = bookingDao.getBookingByIdDirect(id)

    fun getSessionsForBooking(bookingId: Long): Flow<List<BookingSessionEntity>> =
        bookingDao.getSessionsForBooking(bookingId)

    suspend fun getSessionsForBookingDirect(bookingId: Long): List<BookingSessionEntity> =
        bookingDao.getSessionsForBookingDirect(bookingId)

    fun getDeliverablesForBooking(bookingId: Long): Flow<List<BookingDeliverableEntity>> =
        bookingDao.getDeliverablesForBooking(bookingId)

    suspend fun getDeliverablesForBookingDirect(bookingId: Long): List<BookingDeliverableEntity> =
        bookingDao.getDeliverablesForBookingDirect(bookingId)

    fun getCrewAssignmentsForBooking(bookingId: Long): Flow<List<BookingCrewAssignmentEntity>> =
        bookingDao.getCrewAssignmentsForBooking(bookingId)

    suspend fun getCrewAssignmentsForBookingDirect(bookingId: Long): List<BookingCrewAssignmentEntity> =
        bookingDao.getCrewAssignmentsForBookingDirect(bookingId)

    fun getEquipmentAllocationsForBooking(bookingId: Long): Flow<List<BookingEquipmentAllocationEntity>> =
        bookingDao.getEquipmentAllocationsForBooking(bookingId)

    suspend fun getEquipmentAllocationsForBookingDirect(bookingId: Long): List<BookingEquipmentAllocationEntity> =
        bookingDao.getEquipmentAllocationsForBookingDirect(bookingId)

    suspend fun createBooking(
        booking: BookingEntity,
        sessions: List<BookingSessionEntity>,
        deliverables: List<BookingDeliverableEntity>,
        crew: List<BookingCrewAssignmentEntity>,
        equipment: List<BookingEquipmentAllocationEntity>
    ): Long {
        val bookingId = bookingDao.insertBooking(booking)
        val sessionsWithId = sessions.map { it.copy(bookingId = bookingId) }
        val deliverablesWithId = deliverables.map { it.copy(bookingId = bookingId) }
        val crewWithId = crew.map { it.copy(bookingId = bookingId) }
        val equipWithId = equipment.map { it.copy(bookingId = bookingId) }

        if (sessionsWithId.isNotEmpty()) bookingDao.insertSessions(sessionsWithId)
        if (deliverablesWithId.isNotEmpty()) bookingDao.insertDeliverables(deliverablesWithId)
        if (crewWithId.isNotEmpty()) bookingDao.insertCrewAssignments(crewWithId)
        if (equipWithId.isNotEmpty()) bookingDao.insertEquipmentAllocations(equipWithId)

        // Generate matching invoice or quotation entry
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val prefix = if (booking.status == "QUOTATION") "QUO" else "INV"
        val invoiceNumber = "$prefix-${System.currentTimeMillis().toString().takeLast(6)}"

        val invoice = InvoiceEntity(
            bookingId = bookingId,
            invoiceNumber = invoiceNumber,
            isQuotation = booking.status == "QUOTATION",
            issueDate = today,
            dueDate = sessions.firstOrNull()?.date ?: today,
            taxEnabled = booking.taxEnabled,
            taxPercentage = booking.taxPercent
        )
        invoiceDao.insertInvoice(invoice)

        return bookingId
    }

    suspend fun updateBookingFull(
        booking: BookingEntity,
        sessions: List<BookingSessionEntity>,
        deliverables: List<BookingDeliverableEntity>,
        crew: List<BookingCrewAssignmentEntity>,
        equipment: List<BookingEquipmentAllocationEntity>
    ) {
        bookingDao.updateBooking(booking)
        bookingDao.deleteSessionsForBooking(booking.id)
        bookingDao.deleteDeliverablesForBooking(booking.id)
        bookingDao.deleteCrewAssignmentsForBooking(booking.id)
        bookingDao.deleteEquipmentAllocationsForBooking(booking.id)

        val sessionsWithId = sessions.map { it.copy(bookingId = booking.id) }
        val deliverablesWithId = deliverables.map { it.copy(bookingId = booking.id) }
        val crewWithId = crew.map { it.copy(bookingId = booking.id) }
        val equipWithId = equipment.map { it.copy(bookingId = booking.id) }

        if (sessionsWithId.isNotEmpty()) bookingDao.insertSessions(sessionsWithId)
        if (deliverablesWithId.isNotEmpty()) bookingDao.insertDeliverables(deliverablesWithId)
        if (crewWithId.isNotEmpty()) bookingDao.insertCrewAssignments(crewWithId)
        if (equipWithId.isNotEmpty()) bookingDao.insertEquipmentAllocations(equipWithId)
    }

    suspend fun updateBooking(booking: BookingEntity) = bookingDao.updateBooking(booking)

    suspend fun convertQuotationToBooking(bookingId: Long): Boolean {
        val existing = bookingDao.getBookingByIdDirect(bookingId) ?: return false
        val updated = existing.copy(status = "CONFIRMED")
        bookingDao.updateBooking(updated)

        // Update corresponding invoice to be a final tax/non-tax invoice
        val invoice = invoiceDao.getInvoiceForBookingDirect(bookingId)
        if (invoice != null) {
            val newNumber = "INV-${invoice.invoiceNumber.replace("QUO-", "")}"
            invoiceDao.updateInvoice(invoice.copy(isQuotation = false, invoiceNumber = newNumber))
        }
        return true
    }

    suspend fun deleteBooking(booking: BookingEntity) = bookingDao.deleteBooking(booking)

    suspend fun updateDeliverable(deliverable: BookingDeliverableEntity) =
        bookingDao.updateDeliverable(deliverable)

    // Conflict Checkers
    suspend fun checkCrewConflicts(
        freelancerId: Long,
        date: String,
        excludeBookingId: Long
    ): List<CrewConflict> {
        val assignments = bookingDao.getCrewAssignmentsOnDate(date, excludeBookingId)
        val filtered = assignments.filter { it.freelancerId == freelancerId }
        val conflicts = mutableListOf<CrewConflict>()
        for (a in filtered) {
            val otherBooking = bookingDao.getBookingByIdDirect(a.bookingId)
            if (otherBooking != null && otherBooking.status != "CANCELLED") {
                conflicts.add(
                    CrewConflict(
                        freelancerId = freelancerId,
                        freelancerName = a.freelancerName,
                        date = date,
                        conflictingBookingId = otherBooking.id,
                        conflictingEventTitle = "${otherBooking.clientName} (${otherBooking.eventType})",
                        conflictingSessionTitle = a.sessionTitle
                    )
                )
            }
        }
        return conflicts
    }

    suspend fun checkEquipmentConflicts(
        equipmentId: Long,
        date: String,
        excludeBookingId: Long
    ): List<EquipmentConflict> {
        val allocations = bookingDao.getEquipmentAllocationsOnDate(date, excludeBookingId)
        val filtered = allocations.filter { it.equipmentId == equipmentId }
        val conflicts = mutableListOf<EquipmentConflict>()
        for (alloc in filtered) {
            val otherBooking = bookingDao.getBookingByIdDirect(alloc.bookingId)
            if (otherBooking != null && otherBooking.status != "CANCELLED") {
                conflicts.add(
                    EquipmentConflict(
                        equipmentId = equipmentId,
                        equipmentName = alloc.equipmentName,
                        date = date,
                        conflictingBookingId = otherBooking.id,
                        conflictingEventTitle = "${otherBooking.clientName} (${otherBooking.eventType})",
                        conflictingSessionTitle = alloc.sessionTitle
                    )
                )
            }
        }
        return conflicts
    }
}
