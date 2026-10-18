package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseInitializer
import com.example.data.local.StudioFlowDatabase
import com.example.data.local.entity.BookingCrewAssignmentEntity
import com.example.data.local.entity.BookingDeliverableEntity
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.BookingEquipmentAllocationEntity
import com.example.data.local.entity.BookingSessionEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.ExposureFreelancerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.PaymentLedgerEntity
import com.example.data.local.entity.StudioProfileEntity
import com.example.data.repository.BookingRepository
import com.example.data.repository.ClientRepository
import com.example.data.repository.EquipmentRepository
import com.example.data.repository.ExposureRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.StudioRepository
import com.example.domain.model.CrewConflict
import com.example.domain.model.EquipmentConflict
import com.example.domain.model.ExposureSettlementSummary
import com.example.domain.model.FinancialKpi
import com.example.util.PdfGenerator
import com.example.util.StudioBackupManager
import com.example.util.BackupFullData
import com.example.util.DeliverableInventoryManager
import com.example.util.MasterInventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StudioFlowDatabase.getInstance(application)
    private val studioRepo = StudioRepository(database.studioDao())
    private val clientRepo = ClientRepository(database.clientDao())
    private val exposureRepo = ExposureRepository(database.exposureDao())
    private val equipmentRepo = EquipmentRepository(database.equipmentDao())
    private val bookingRepo = BookingRepository(database.bookingDao(), database.invoiceDao())
    private val financeRepo = FinanceRepository(database.paymentLedgerDao(), database.invoiceDao(), database.bookingDao())

    val masterInventory = MutableStateFlow<List<MasterInventoryItem>>(emptyList())

    init {
        viewModelScope.launch {
            DatabaseInitializer.seedSampleDataIfEmpty(database)
            masterInventory.value = DeliverableInventoryManager.getAllItems(application)
            // Daily Auto-Backup to 'TracStudio Backup'
            StudioBackupManager.checkAndPerformDailyAutoBackup(application, database)
        }
    }

    val studioProfile: StateFlow<StudioProfileEntity?> = studioRepo.studioProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allClients: StateFlow<List<ClientEntity>> = clientRepo.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFreelancers: StateFlow<List<ExposureFreelancerEntity>> = exposureRepo.allFreelancers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEquipment: StateFlow<List<EquipmentEntity>> = equipmentRepo.allEquipment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<BookingEntity>> = bookingRepo.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAndPastBookings: StateFlow<List<BookingEntity>> = bookingRepo.activeAndPastBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuotations: StateFlow<List<BookingEntity>> = bookingRepo.allQuotations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<BookingSessionEntity>> = bookingRepo.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCrewAssignments: StateFlow<List<BookingCrewAssignmentEntity>> = bookingRepo.allCrewAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEquipmentAllocations: StateFlow<List<BookingEquipmentAllocationEntity>> = bookingRepo.allEquipmentAllocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentLedgerEntity>> = financeRepo.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<InvoiceEntity>> = financeRepo.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial KPI aggregation Flow
    val financialKpis: StateFlow<FinancialKpi> = combine(
        allBookings,
        allPayments,
        allCrewAssignments
    ) { bookings, payments, crewAssignments ->
        val confirmedBookings = bookings.filter { it.status != "QUOTATION" && it.status != "CANCELLED" }
        val quotations = bookings.filter { it.status == "QUOTATION" }

        val totalRevenue = confirmedBookings.sumOf { it.grandTotal }
        val customerReceived = payments.filter { it.type == "CUSTOMER_INFLOW" }.sumOf { it.amount }
        val customerOutstanding = (totalRevenue - customerReceived).coerceAtLeast(0.0)

        val crewPayoutsSettled = payments.filter { it.type == "EXPOSURE_PAYOUT" }.sumOf { it.amount }
        val totalAgreedCrew = crewAssignments.sumOf { it.agreedRate }
        val crewPayoutsPending = (totalAgreedCrew - crewPayoutsSettled).coerceAtLeast(0.0)

        FinancialKpi(
            totalRevenue = totalRevenue,
            totalReceived = customerReceived,
            customerOutstanding = customerOutstanding,
            crewPayoutsSettled = crewPayoutsSettled,
            crewPayoutsPending = crewPayoutsPending,
            activeBookingsCount = confirmedBookings.size,
            quotationsCount = quotations.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialKpi(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0)
    )

    // Exposure Settlement summaries Flow
    val exposureSettlements: StateFlow<List<ExposureSettlementSummary>> = combine(
        allFreelancers,
        allCrewAssignments,
        allPayments
    ) { freelancers, assignments, payments ->
        freelancers.map { f ->
            val fAssignments = assignments.filter { it.freelancerId == f.id }
            val fPayouts = payments.filter { it.type == "EXPOSURE_PAYOUT" && it.partyId == f.id }
            val totalAgreed = fAssignments.sumOf { it.agreedRate }
            val settled = fPayouts.sumOf { it.amount }
            val pending = (totalAgreed - settled).coerceAtLeast(0.0)

            ExposureSettlementSummary(
                freelancerId = f.id,
                freelancerName = f.name,
                role = f.role,
                contactNumber = f.contactNumber,
                totalAgreedRate = totalAgreed,
                totalSettled = settled,
                pendingPayout = pending,
                assignmentsCount = fAssignments.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Profile Actions ---
    fun updateStudioProfile(profile: StudioProfileEntity) {
        viewModelScope.launch {
            studioRepo.updateProfile(profile)
        }
    }

    // --- Client Actions ---
    fun addClient(client: ClientEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = clientRepo.addClient(client)
            onComplete(id)
        }
    }

    fun updateClient(client: ClientEntity) {
        viewModelScope.launch {
            clientRepo.updateClient(client)
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            clientRepo.deleteClient(client)
        }
    }

    // --- Exposure Freelancer Actions ---
    fun addFreelancer(freelancer: ExposureFreelancerEntity) {
        viewModelScope.launch {
            exposureRepo.addFreelancer(freelancer)
        }
    }

    fun updateFreelancer(freelancer: ExposureFreelancerEntity) {
        viewModelScope.launch {
            exposureRepo.updateFreelancer(freelancer)
        }
    }

    fun deleteFreelancer(freelancer: ExposureFreelancerEntity) {
        viewModelScope.launch {
            exposureRepo.deleteFreelancer(freelancer)
        }
    }

    // --- Equipment Actions ---
    fun addEquipment(equipment: EquipmentEntity) {
        viewModelScope.launch {
            equipmentRepo.addEquipment(equipment)
        }
    }

    fun updateEquipment(equipment: EquipmentEntity) {
        viewModelScope.launch {
            equipmentRepo.updateEquipment(equipment)
        }
    }

    fun deleteEquipment(equipment: EquipmentEntity) {
        viewModelScope.launch {
            equipmentRepo.deleteEquipment(equipment)
        }
    }

    // --- Conflict Checking ---
    suspend fun checkCrewConflict(freelancerId: Long, date: String, excludeBookingId: Long): List<CrewConflict> {
        return bookingRepo.checkCrewConflicts(freelancerId, date, excludeBookingId)
    }

    suspend fun checkEquipmentConflict(equipmentId: Long, date: String, excludeBookingId: Long): List<EquipmentConflict> {
        return bookingRepo.checkEquipmentConflicts(equipmentId, date, excludeBookingId)
    }

    // --- Booking & Quotation Actions ---
    fun createBooking(
        booking: BookingEntity,
        sessions: List<BookingSessionEntity>,
        deliverables: List<BookingDeliverableEntity>,
        crew: List<BookingCrewAssignmentEntity>,
        equipment: List<BookingEquipmentAllocationEntity>,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = bookingRepo.createBooking(booking, sessions, deliverables, crew, equipment)
            // If advance was paid at entry, record customer payment
            if (booking.advancePaid > 0) {
                financeRepo.recordCustomerPayment(
                    PaymentLedgerEntity(
                        bookingId = id,
                        type = "CUSTOMER_INFLOW",
                        partyId = booking.clientId,
                        partyName = booking.clientName,
                        amount = booking.advancePaid,
                        paymentDate = sessions.firstOrNull()?.date ?: "2026-10-18",
                        paymentMode = "UPI",
                        paymentStage = "ADVANCE",
                        notes = "Initial advance at booking creation"
                    )
                )
            }
            onSuccess(id)
        }
    }

    fun convertQuotationToBooking(bookingId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            bookingRepo.convertQuotationToBooking(bookingId)
            onDone()
        }
    }

    fun deleteBooking(booking: BookingEntity) {
        viewModelScope.launch {
            bookingRepo.deleteBooking(booking)
        }
    }

    // --- Payment & Settlement Actions ---
    fun recordCustomerPayment(
        bookingId: Long,
        clientId: Long,
        clientName: String,
        amount: Double,
        date: String,
        mode: String,
        stage: String,
        referenceNumber: String,
        notes: String
    ) {
        viewModelScope.launch {
            financeRepo.recordCustomerPayment(
                PaymentLedgerEntity(
                    bookingId = bookingId,
                    type = "CUSTOMER_INFLOW",
                    partyId = clientId,
                    partyName = clientName,
                    amount = amount,
                    paymentDate = date,
                    paymentMode = mode,
                    paymentStage = stage,
                    referenceNumber = referenceNumber,
                    notes = notes
                )
            )
        }
    }

    fun recordCrewPayout(
        freelancerId: Long,
        freelancerName: String,
        amount: Double,
        date: String,
        mode: String,
        referenceNumber: String,
        notes: String,
        bookingId: Long? = null
    ) {
        viewModelScope.launch {
            financeRepo.recordCrewPayout(
                PaymentLedgerEntity(
                    bookingId = bookingId,
                    type = "EXPOSURE_PAYOUT",
                    partyId = freelancerId,
                    partyName = freelancerName,
                    amount = amount,
                    paymentDate = date,
                    paymentMode = mode,
                    paymentStage = "CREW_SETTLEMENT",
                    referenceNumber = referenceNumber,
                    notes = notes
                )
            )
        }
    }

    // --- PDF Generator ---
    suspend fun generateAndSharePdf(
        bookingId: Long,
        onFileReady: (File) -> Unit
    ) {
        val studio = studioRepo.getProfileDirect() ?: return
        val booking = bookingRepo.getBookingByIdDirect(bookingId) ?: return
        val sessions = bookingRepo.getSessionsForBookingDirect(bookingId)
        val deliverables = bookingRepo.getDeliverablesForBookingDirect(bookingId)
        val invoice = financeRepo.getInvoiceForBookingDirect(bookingId) ?: InvoiceEntity(
            bookingId = bookingId,
            invoiceNumber = if (booking.status == "QUOTATION") "QUO-$bookingId" else "INV-$bookingId",
            isQuotation = booking.status == "QUOTATION",
            issueDate = "2026-10-18",
            taxEnabled = booking.taxEnabled,
            taxPercentage = booking.taxPercent,
            termsAndConditions = studio.defaultTermsAndConditions,
            footerNotes = studio.defaultFooterNotes
        )

        val file = PdfGenerator.generateInvoicePdf(
            context = getApplication(),
            studio = studio,
            booking = booking,
            sessions = sessions,
            deliverables = deliverables,
            invoice = invoice
        )
        onFileReady(file)
    }

    suspend fun getSessionsForBookingDirect(bookingId: Long): List<BookingSessionEntity> {
        return bookingRepo.getSessionsForBookingDirect(bookingId)
    }

    suspend fun getDeliverablesForBookingDirect(bookingId: Long): List<BookingDeliverableEntity> {
        return bookingRepo.getDeliverablesForBookingDirect(bookingId)
    }

    suspend fun getCrewAssignmentsForBookingDirect(bookingId: Long): List<BookingCrewAssignmentEntity> {
        return bookingRepo.getCrewAssignmentsForBookingDirect(bookingId)
    }

    suspend fun getEquipmentAllocationsForBookingDirect(bookingId: Long): List<BookingEquipmentAllocationEntity> {
        return bookingRepo.getEquipmentAllocationsForBookingDirect(bookingId)
    }

    fun reseedSampleData(onComplete: () -> Unit) {
        viewModelScope.launch {
            DatabaseInitializer.reseedSampleData(database)
            onComplete()
        }
    }

    fun triggerManualBackup(onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val file = StudioBackupManager.createFullBackupFile(getApplication(), database)
            onComplete(file)
        }
    }

    fun restoreFromBackupData(backupData: BackupFullData, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = StudioBackupManager.restoreFromBackup(getApplication(), database, backupData)
            onComplete(success)
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        StudioBackupManager.setAutoBackupEnabled(getApplication(), enabled)
    }

    fun isAutoBackupEnabled(): Boolean {
        return StudioBackupManager.isAutoBackupEnabled(getApplication())
    }

    fun getLastBackupInfo(): Map<String, Any> {
        return StudioBackupManager.getLastBackupInfo(getApplication())
    }

    fun refreshMasterInventory() {
        masterInventory.value = DeliverableInventoryManager.getAllItems(getApplication())
    }

    fun addMasterInventoryItem(item: MasterInventoryItem) {
        DeliverableInventoryManager.addItem(getApplication(), item)
        refreshMasterInventory()
    }

    fun updateMasterInventoryItem(item: MasterInventoryItem) {
        DeliverableInventoryManager.updateItem(getApplication(), item)
        refreshMasterInventory()
    }

    fun deleteMasterInventoryItem(id: String) {
        DeliverableInventoryManager.deleteItem(getApplication(), id)
        refreshMasterInventory()
    }

    fun resetMasterInventoryToDefaults() {
        val defaults = DeliverableInventoryManager.resetToDefaults(getApplication())
        masterInventory.value = defaults
    }

    fun updateDeliverableCompleted(deliverable: BookingDeliverableEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            database.bookingDao().updateDeliverable(deliverable.copy(isCompleted = isCompleted))
        }
    }
}
