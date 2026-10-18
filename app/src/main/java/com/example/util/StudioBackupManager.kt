package com.example.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupMetadata(
    val appName: String = "TracStudio",
    val backupVersion: Int = 1,
    val backupDate: String = "",
    val backupTimestamp: Long = 0L,
    val folderTarget: String = "TracStudio Backup",
    val studioName: String = "",
    val ownerName: String = "",
    val clientCount: Int = 0,
    val bookingCount: Int = 0,
    val invoiceCount: Int = 0,
    val paymentCount: Int = 0,
    val crewCount: Int = 0,
    val equipmentCount: Int = 0
)

data class BackupFullData(
    val metadata: BackupMetadata,
    val studioProfile: StudioProfileEntity?,
    val clients: List<ClientEntity>,
    val freelancers: List<ExposureFreelancerEntity>,
    val equipment: List<EquipmentEntity>,
    val bookings: List<BookingEntity>,
    val sessions: List<BookingSessionEntity>,
    val deliverables: List<BookingDeliverableEntity>,
    val crewAssignments: List<BookingCrewAssignmentEntity>,
    val equipmentAllocations: List<BookingEquipmentAllocationEntity>,
    val payments: List<PaymentLedgerEntity>,
    val invoices: List<InvoiceEntity>,
    val masterInventoryJson: String = ""
)

object StudioBackupManager {
    private const val TAG = "StudioBackupManager"
    const val BACKUP_FOLDER_NAME = "TracStudio Backup"
    const val MASTER_BACKUP_FILENAME = "TracStudio_AutoBackup.json"

    private const val PREFS_NAME = "tracstudio_backup_prefs"
    const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val KEY_LAST_BACKUP_DATE = "last_backup_date"
    const val KEY_LAST_BACKUP_TIMESTAMP = "last_backup_timestamp"
    const val KEY_LAST_BACKUP_FILE_PATH = "last_backup_file_path"
    const val KEY_LAST_BACKUP_SIZE = "last_backup_size"
    const val KEY_LAST_BACKUP_SUMMARY = "last_backup_summary"
    const val KEY_LAST_RESTORE_TIMESTAMP = "last_restore_timestamp"

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAutoBackupEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_BACKUP_ENABLED, true)
    }

    fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
    }

    fun getLastBackupInfo(context: Context): Map<String, Any> {
        val prefs = getPrefs(context)
        return mapOf(
            "enabled" to prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, true),
            "date" to (prefs.getString(KEY_LAST_BACKUP_DATE, null) ?: "Never"),
            "timestamp" to prefs.getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L),
            "size" to (prefs.getString(KEY_LAST_BACKUP_SIZE, null) ?: "0 KB"),
            "summary" to (prefs.getString(KEY_LAST_BACKUP_SUMMARY, null) ?: "No backup taken yet"),
            "restoreTimestamp" to prefs.getLong(KEY_LAST_RESTORE_TIMESTAMP, 0L)
        )
    }

    /**
     * Checks if a daily backup is needed for today, and performs it in background.
     */
    suspend fun checkAndPerformDailyAutoBackup(
        context: Context,
        database: StudioFlowDatabase
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isAutoBackupEnabled(context)) return@withContext false

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastDate = getPrefs(context).getString(KEY_LAST_BACKUP_DATE, "")

            if (lastDate != today) {
                val file = createFullBackupFile(context, database)
                if (file != null && file.exists()) {
                    Log.d(TAG, "Daily auto backup completed: ${file.absolutePath}")
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed daily auto backup check", e)
        }
        return@withContext false
    }

    /**
     * Creates and replaces the master daily backup JSON file in the 'TracStudio Backup' folder.
     */
    suspend fun createFullBackupFile(
        context: Context,
        database: StudioFlowDatabase
    ): File? = withContext(Dispatchers.IO) {
        try {
            val jsonString = generateBackupJsonString(context, database)

            // Prepare directory: TracStudio Backup
            val backupDir = File(context.getExternalFilesDir(null), BACKUP_FOLDER_NAME).apply {
                if (!exists()) mkdirs()
            }

            // Always write and replace TracStudio_AutoBackup.json (daily replace)
            val masterFile = File(backupDir, MASTER_BACKUP_FILENAME)
            FileOutputStream(masterFile).use { fos ->
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            // Also save in internal files dir for persistent app safety
            val internalBackupDir = File(context.filesDir, BACKUP_FOLDER_NAME).apply {
                if (!exists()) mkdirs()
            }
            val internalMaster = File(internalBackupDir, MASTER_BACKUP_FILENAME)
            FileOutputStream(internalMaster).use { fos ->
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            // Update preferences
            val now = System.currentTimeMillis()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
            val sizeKb = String.format(Locale.US, "%.1f KB", masterFile.length() / 1024f)
            
            val studio = database.studioDao().getStudioProfileDirect()
            val clientCount = database.clientDao().getAllClientsDirect().size
            val bookingCount = database.bookingDao().getAllBookingsDirect().size
            val summary = "$clientCount Clients, $bookingCount Events, ${studio?.studioName ?: "TracStudio"}"

            getPrefs(context).edit()
                .putString(KEY_LAST_BACKUP_DATE, todayStr)
                .putLong(KEY_LAST_BACKUP_TIMESTAMP, now)
                .putString(KEY_LAST_BACKUP_FILE_PATH, masterFile.absolutePath)
                .putString(KEY_LAST_BACKUP_SIZE, sizeKb)
                .putString(KEY_LAST_BACKUP_SUMMARY, summary)
                .apply()

            return@withContext masterFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup file", e)
            return@withContext null
        }
    }

    /**
     * Serializes the entire database into a single, clean JSON string.
     */
    suspend fun generateBackupJsonString(
        context: Context? = null,
        database: StudioFlowDatabase
    ): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        val studio = database.studioDao().getStudioProfileDirect()
        val clients = database.clientDao().getAllClientsDirect()
        val freelancers = database.exposureDao().getAllFreelancersDirect()
        val equipment = database.equipmentDao().getAllEquipmentDirect()
        val bookings = database.bookingDao().getAllBookingsDirect()
        val sessions = database.bookingDao().getAllSessionsDirect()
        val deliverables = database.bookingDao().getAllDeliverablesDirect()
        val crewAssignments = database.bookingDao().getAllCrewAssignmentsDirect()
        val equipmentAllocations = database.bookingDao().getAllEquipmentAllocationsDirect()
        val payments = database.paymentLedgerDao().getAllPaymentsDirect()
        val invoices = database.invoiceDao().getAllInvoicesDirect()

        val now = System.currentTimeMillis()
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now))

        // Metadata Header
        val meta = JSONObject().apply {
            put("appName", "TracStudio")
            put("backupVersion", 1)
            put("backupDate", dateFormatted)
            put("backupTimestamp", now)
            put("folderTarget", BACKUP_FOLDER_NAME)
            put("fileName", MASTER_BACKUP_FILENAME)
            put("studioName", studio?.studioName ?: "TracStudio")
            put("ownerName", studio?.ownerName ?: "Owner")
            put("clientCount", clients.size)
            put("bookingCount", bookings.size)
            put("invoiceCount", invoices.size)
            put("paymentCount", payments.size)
            put("crewCount", freelancers.size)
            put("equipmentCount", equipment.size)
        }
        root.put("metadata", meta)

        // 1. Studio Profile
        if (studio != null) {
            val sObj = JSONObject().apply {
                put("id", studio.id)
                put("studioName", studio.studioName)
                put("ownerName", studio.ownerName)
                put("mobileNumber", studio.mobileNumber)
                put("email", studio.email)
                put("fullAddress", studio.fullAddress)
                put("logoUri", studio.logoUri)
                put("bankName", studio.bankName)
                put("accountHolderName", studio.accountHolderName)
                put("accountNumber", studio.accountNumber)
                put("ifscCode", studio.ifscCode)
                put("upiId", studio.upiId)
                put("upiQrUri", studio.upiQrUri)
                put("defaultTermsAndConditions", studio.defaultTermsAndConditions)
                put("defaultFooterNotes", studio.defaultFooterNotes)
                put("taxPercentage", studio.taxPercentage)
            }
            root.put("studioProfile", sObj)
        }

        // 2. Clients
        val clientsArr = JSONArray()
        clients.forEach { c ->
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("contactNumber", c.contactNumber)
                put("alternateNumber", c.alternateNumber)
                put("address", c.address)
                put("notes", c.notes)
                put("createdAt", c.createdAt)
            }
            clientsArr.put(cObj)
        }
        root.put("clients", clientsArr)

        // 3. Exposure Freelancers
        val crewArr = JSONArray()
        freelancers.forEach { f ->
            val fObj = JSONObject().apply {
                put("id", f.id)
                put("name", f.name)
                put("role", f.role)
                put("contactNumber", f.contactNumber)
                put("address", f.address)
                put("defaultDailyRate", f.defaultDailyRate)
                put("defaultShiftRate", f.defaultShiftRate)
                put("upiId", f.upiId)
                put("notes", f.notes)
                put("isActive", f.isActive)
            }
            crewArr.put(fObj)
        }
        root.put("freelancers", crewArr)

        // 4. Equipment Inventory
        val gearArr = JSONArray()
        equipment.forEach { eq ->
            val eqObj = JSONObject().apply {
                put("id", eq.id)
                put("itemName", eq.itemName)
                put("category", eq.category)
                put("serialNumber", eq.serialNumber)
                put("purchaseDate", eq.purchaseDate)
                put("notes", eq.notes)
                put("isAvailable", eq.isAvailable)
            }
            gearArr.put(eqObj)
        }
        root.put("equipment", gearArr)

        // 5. Bookings
        val bookingsArr = JSONArray()
        bookings.forEach { b ->
            val bObj = JSONObject().apply {
                put("id", b.id)
                put("clientId", b.clientId)
                put("clientName", b.clientName)
                put("clientContact", b.clientContact)
                put("eventType", b.eventType)
                put("status", b.status)
                put("notes", b.notes)
                put("totalAmount", b.totalAmount)
                put("discountType", b.discountType)
                put("discountValue", b.discountValue)
                put("discountAmount", b.discountAmount)
                put("taxEnabled", b.taxEnabled)
                put("taxPercent", b.taxPercent)
                put("taxAmount", b.taxAmount)
                put("grandTotal", b.grandTotal)
                put("advancePaid", b.advancePaid)
                put("balanceDue", b.balanceDue)
                put("createdAt", b.createdAt)
            }
            bookingsArr.put(bObj)
        }
        root.put("bookings", bookingsArr)

        // 6. Booking Sessions
        val sessionsArr = JSONArray()
        sessions.forEach { s ->
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("bookingId", s.bookingId)
                put("sessionTitle", s.sessionTitle)
                put("date", s.date)
                put("startTime", s.startTime)
                put("endTime", s.endTime)
                put("venueName", s.venueName)
                put("city", s.city)
                put("servicesJson", s.servicesJson)
            }
            sessionsArr.put(sObj)
        }
        root.put("sessions", sessionsArr)

        // 7. Booking Deliverables
        val deliverablesArr = JSONArray()
        deliverables.forEach { d ->
            val dObj = JSONObject().apply {
                put("id", d.id)
                put("bookingId", d.bookingId)
                put("title", d.title)
                put("quantity", d.quantity)
                put("isCompleted", d.isCompleted)
                put("estimatedDeliveryDate", d.estimatedDeliveryDate)
            }
            deliverablesArr.put(dObj)
        }
        root.put("deliverables", deliverablesArr)

        // 8. Crew Assignments
        val crewAssignArr = JSONArray()
        crewAssignments.forEach { ca ->
            val caObj = JSONObject().apply {
                put("id", ca.id)
                put("bookingId", ca.bookingId)
                put("freelancerId", ca.freelancerId)
                put("freelancerName", ca.freelancerName)
                put("role", ca.role)
                put("date", ca.date)
                put("sessionTitle", ca.sessionTitle)
                put("rateType", ca.rateType)
                put("agreedRate", ca.agreedRate)
                put("isSettled", ca.isSettled)
                put("notes", ca.notes)
            }
            crewAssignArr.put(caObj)
        }
        root.put("crewAssignments", crewAssignArr)

        // 9. Equipment Allocations
        val eqAllocArr = JSONArray()
        equipmentAllocations.forEach { ea ->
            val eaObj = JSONObject().apply {
                put("id", ea.id)
                put("bookingId", ea.bookingId)
                put("equipmentId", ea.equipmentId)
                put("equipmentName", ea.equipmentName)
                put("category", ea.category)
                put("date", ea.date)
                put("sessionTitle", ea.sessionTitle)
            }
            eqAllocArr.put(eaObj)
        }
        root.put("equipmentAllocations", eqAllocArr)

        // 10. Payment Ledger
        val paymentsArr = JSONArray()
        payments.forEach { p ->
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("bookingId", p.bookingId ?: 0L)
                put("type", p.type)
                put("partyId", p.partyId)
                put("partyName", p.partyName)
                put("amount", p.amount)
                put("paymentDate", p.paymentDate)
                put("paymentMode", p.paymentMode)
                put("paymentStage", p.paymentStage)
                put("referenceNumber", p.referenceNumber)
                put("notes", p.notes)
                put("createdAt", p.createdAt)
            }
            paymentsArr.put(pObj)
        }
        root.put("payments", paymentsArr)

        // 11. Invoices
        val invoicesArr = JSONArray()
        invoices.forEach { inv ->
            val invObj = JSONObject().apply {
                put("id", inv.id)
                put("bookingId", inv.bookingId)
                put("invoiceNumber", inv.invoiceNumber)
                put("isQuotation", inv.isQuotation)
                put("issueDate", inv.issueDate)
                put("dueDate", inv.dueDate)
                put("taxEnabled", inv.taxEnabled)
                put("taxPercentage", inv.taxPercentage)
                put("termsAndConditions", inv.termsAndConditions)
                put("footerNotes", inv.footerNotes)
                put("pdfFilePath", inv.pdfFilePath)
                put("createdAt", inv.createdAt)
            }
            invoicesArr.put(invObj)
        }
        root.put("invoices", invoicesArr)
        val masterInv = if (context != null) DeliverableInventoryManager.exportToJson(context) else ""
        root.put("masterInventoryJson", masterInv)

        return@withContext root.toString(2)
    }

    /**
     * Parses a backup JSON input stream and returns BackupFullData.
     */
    fun parseBackupJson(inputStream: InputStream): BackupFullData? {
        return try {
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            parseBackupJsonString(sb.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error reading backup stream", e)
            null
        }
    }

    fun parseBackupJsonString(jsonString: String): BackupFullData? {
        return try {
            val root = JSONObject(jsonString)

            val metaObj = root.optJSONObject("metadata")
            val metadata = if (metaObj != null) {
                BackupMetadata(
                    appName = metaObj.optString("appName", "TracStudio"),
                    backupVersion = metaObj.optInt("backupVersion", 1),
                    backupDate = metaObj.optString("backupDate", ""),
                    backupTimestamp = metaObj.optLong("backupTimestamp", 0L),
                    folderTarget = metaObj.optString("folderTarget", BACKUP_FOLDER_NAME),
                    studioName = metaObj.optString("studioName", "Studio"),
                    ownerName = metaObj.optString("ownerName", "Owner"),
                    clientCount = metaObj.optInt("clientCount", 0),
                    bookingCount = metaObj.optInt("bookingCount", 0),
                    invoiceCount = metaObj.optInt("invoiceCount", 0),
                    paymentCount = metaObj.optInt("paymentCount", 0),
                    crewCount = metaObj.optInt("crewCount", 0),
                    equipmentCount = metaObj.optInt("equipmentCount", 0)
                )
            } else {
                BackupMetadata(backupDate = "Legacy Backup", studioName = "TracStudio")
            }

            // Studio Profile
            val sObj = root.optJSONObject("studioProfile")
            val studioProfile = if (sObj != null) {
                StudioProfileEntity(
                    id = sObj.optInt("id", 1),
                    studioName = sObj.optString("studioName", "TracStudio"),
                    ownerName = sObj.optString("ownerName", "Owner"),
                    mobileNumber = sObj.optString("mobileNumber", ""),
                    email = sObj.optString("email", ""),
                    fullAddress = sObj.optString("fullAddress", ""),
                    logoUri = sObj.optString("logoUri", ""),
                    bankName = sObj.optString("bankName", ""),
                    accountHolderName = sObj.optString("accountHolderName", ""),
                    accountNumber = sObj.optString("accountNumber", ""),
                    ifscCode = sObj.optString("ifscCode", ""),
                    upiId = sObj.optString("upiId", ""),
                    upiQrUri = sObj.optString("upiQrUri", ""),
                    defaultTermsAndConditions = sObj.optString("defaultTermsAndConditions", ""),
                    defaultFooterNotes = sObj.optString("defaultFooterNotes", ""),
                    taxPercentage = sObj.optDouble("taxPercentage", 18.0)
                )
            } else null

            // Clients
            val clients = mutableListOf<ClientEntity>()
            val cArr = root.optJSONArray("clients") ?: JSONArray()
            for (i in 0 until cArr.length()) {
                val item = cArr.getJSONObject(i)
                clients.add(
                    ClientEntity(
                        id = item.optLong("id", 0L),
                        name = item.optString("name", "Unknown Client"),
                        contactNumber = item.optString("contactNumber", ""),
                        alternateNumber = item.optString("alternateNumber", ""),
                        address = item.optString("address", ""),
                        notes = item.optString("notes", ""),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Freelancers
            val freelancers = mutableListOf<ExposureFreelancerEntity>()
            val fArr = root.optJSONArray("freelancers") ?: JSONArray()
            for (i in 0 until fArr.length()) {
                val item = fArr.getJSONObject(i)
                freelancers.add(
                    ExposureFreelancerEntity(
                        id = item.optLong("id", 0L),
                        name = item.optString("name", "Crew"),
                        role = item.optString("role", "Photographer"),
                        contactNumber = item.optString("contactNumber", ""),
                        address = item.optString("address", ""),
                        defaultDailyRate = item.optDouble("defaultDailyRate", 0.0),
                        defaultShiftRate = item.optDouble("defaultShiftRate", 0.0),
                        upiId = item.optString("upiId", ""),
                        notes = item.optString("notes", ""),
                        isActive = item.optBoolean("isActive", true)
                    )
                )
            }

            // Equipment
            val equipment = mutableListOf<EquipmentEntity>()
            val eqArr = root.optJSONArray("equipment") ?: JSONArray()
            for (i in 0 until eqArr.length()) {
                val item = eqArr.getJSONObject(i)
                equipment.add(
                    EquipmentEntity(
                        id = item.optLong("id", 0L),
                        itemName = item.optString("itemName", "Camera Gear"),
                        category = item.optString("category", "Camera"),
                        serialNumber = item.optString("serialNumber", ""),
                        purchaseDate = item.optString("purchaseDate", ""),
                        notes = item.optString("notes", ""),
                        isAvailable = item.optBoolean("isAvailable", true)
                    )
                )
            }

            // Bookings
            val bookings = mutableListOf<BookingEntity>()
            val bArr = root.optJSONArray("bookings") ?: JSONArray()
            for (i in 0 until bArr.length()) {
                val item = bArr.getJSONObject(i)
                bookings.add(
                    BookingEntity(
                        id = item.optLong("id", 0L),
                        clientId = item.optLong("clientId", 0L),
                        clientName = item.optString("clientName", "Client"),
                        clientContact = item.optString("clientContact", ""),
                        eventType = item.optString("eventType", "Wedding"),
                        status = item.optString("status", "CONFIRMED"),
                        notes = item.optString("notes", ""),
                        totalAmount = item.optDouble("totalAmount", 0.0),
                        discountType = item.optString("discountType", "FLAT"),
                        discountValue = item.optDouble("discountValue", 0.0),
                        discountAmount = item.optDouble("discountAmount", 0.0),
                        taxEnabled = item.optBoolean("taxEnabled", false),
                        taxPercent = item.optDouble("taxPercent", 18.0),
                        taxAmount = item.optDouble("taxAmount", 0.0),
                        grandTotal = item.optDouble("grandTotal", 0.0),
                        advancePaid = item.optDouble("advancePaid", 0.0),
                        balanceDue = item.optDouble("balanceDue", 0.0),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Sessions
            val sessions = mutableListOf<BookingSessionEntity>()
            val sArr = root.optJSONArray("sessions") ?: JSONArray()
            for (i in 0 until sArr.length()) {
                val item = sArr.getJSONObject(i)
                sessions.add(
                    BookingSessionEntity(
                        id = item.optLong("id", 0L),
                        bookingId = item.optLong("bookingId", 0L),
                        sessionTitle = item.optString("sessionTitle", "Session"),
                        date = item.optString("date", "2026-01-01"),
                        startTime = item.optString("startTime", ""),
                        endTime = item.optString("endTime", ""),
                        venueName = item.optString("venueName", ""),
                        city = item.optString("city", ""),
                        servicesJson = item.optString("servicesJson", "")
                    )
                )
            }

            // Deliverables
            val deliverables = mutableListOf<BookingDeliverableEntity>()
            val dArr = root.optJSONArray("deliverables") ?: JSONArray()
            for (i in 0 until dArr.length()) {
                val item = dArr.getJSONObject(i)
                deliverables.add(
                    BookingDeliverableEntity(
                        id = item.optLong("id", 0L),
                        bookingId = item.optLong("bookingId", 0L),
                        title = item.optString("title", "Deliverable"),
                        quantity = item.optInt("quantity", 1),
                        isCompleted = item.optBoolean("isCompleted", false),
                        estimatedDeliveryDate = item.optString("estimatedDeliveryDate", "")
                    )
                )
            }

            // Crew Assignments
            val crewAssignments = mutableListOf<BookingCrewAssignmentEntity>()
            val caArr = root.optJSONArray("crewAssignments") ?: JSONArray()
            for (i in 0 until caArr.length()) {
                val item = caArr.getJSONObject(i)
                crewAssignments.add(
                    BookingCrewAssignmentEntity(
                        id = item.optLong("id", 0L),
                        bookingId = item.optLong("bookingId", 0L),
                        freelancerId = item.optLong("freelancerId", 0L),
                        freelancerName = item.optString("freelancerName", "Crew Member"),
                        role = item.optString("role", "Photographer"),
                        date = item.optString("date", "2026-01-01"),
                        sessionTitle = item.optString("sessionTitle", ""),
                        rateType = item.optString("rateType", "DAILY"),
                        agreedRate = item.optDouble("agreedRate", 0.0),
                        isSettled = item.optBoolean("isSettled", false),
                        notes = item.optString("notes", "")
                    )
                )
            }

            // Equipment Allocations
            val equipmentAllocations = mutableListOf<BookingEquipmentAllocationEntity>()
            val eaArr = root.optJSONArray("equipmentAllocations") ?: JSONArray()
            for (i in 0 until eaArr.length()) {
                val item = eaArr.getJSONObject(i)
                equipmentAllocations.add(
                    BookingEquipmentAllocationEntity(
                        id = item.optLong("id", 0L),
                        bookingId = item.optLong("bookingId", 0L),
                        equipmentId = item.optLong("equipmentId", 0L),
                        equipmentName = item.optString("equipmentName", "Gear"),
                        category = item.optString("category", "Camera"),
                        date = item.optString("date", "2026-01-01"),
                        sessionTitle = item.optString("sessionTitle", "")
                    )
                )
            }

            // Payments
            val payments = mutableListOf<PaymentLedgerEntity>()
            val pArr = root.optJSONArray("payments") ?: JSONArray()
            for (i in 0 until pArr.length()) {
                val item = pArr.getJSONObject(i)
                val bId = item.optLong("bookingId", 0L)
                payments.add(
                    PaymentLedgerEntity(
                        id = item.optLong("id", 0L),
                        bookingId = if (bId > 0) bId else null,
                        type = item.optString("type", "CUSTOMER_INFLOW"),
                        partyId = item.optLong("partyId", 0L),
                        partyName = item.optString("partyName", "Party"),
                        amount = item.optDouble("amount", 0.0),
                        paymentDate = item.optString("paymentDate", "2026-01-01"),
                        paymentMode = item.optString("paymentMode", "UPI"),
                        paymentStage = item.optString("paymentStage", "ADVANCE"),
                        referenceNumber = item.optString("referenceNumber", ""),
                        notes = item.optString("notes", ""),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Invoices
            val invoices = mutableListOf<InvoiceEntity>()
            val invArr = root.optJSONArray("invoices") ?: JSONArray()
            for (i in 0 until invArr.length()) {
                val item = invArr.getJSONObject(i)
                invoices.add(
                    InvoiceEntity(
                        id = item.optLong("id", 0L),
                        bookingId = item.optLong("bookingId", 0L),
                        invoiceNumber = item.optString("invoiceNumber", "INV-${System.currentTimeMillis()}"),
                        isQuotation = item.optBoolean("isQuotation", false),
                        issueDate = item.optString("issueDate", "2026-01-01"),
                        dueDate = item.optString("dueDate", ""),
                        taxEnabled = item.optBoolean("taxEnabled", false),
                        taxPercentage = item.optDouble("taxPercentage", 18.0),
                        termsAndConditions = item.optString("termsAndConditions", ""),
                        footerNotes = item.optString("footerNotes", ""),
                        pdfFilePath = item.optString("pdfFilePath", ""),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            return BackupFullData(
                metadata = metadata,
                studioProfile = studioProfile,
                clients = clients,
                freelancers = freelancers,
                equipment = equipment,
                bookings = bookings,
                sessions = sessions,
                deliverables = deliverables,
                crewAssignments = crewAssignments,
                equipmentAllocations = equipmentAllocations,
                payments = payments,
                invoices = invoices,
                masterInventoryJson = root.optString("masterInventoryJson", "")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse backup JSON", e)
            null
        }
    }

    /**
     * Restores all data from the parsed backup into Room database, replacing existing tables safely.
     */
    suspend fun restoreFromBackup(
        context: Context,
        database: StudioFlowDatabase,
        backupData: BackupFullData
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Clear existing database tables
            database.invoiceDao().deleteAllInvoices()
            database.paymentLedgerDao().deleteAllPayments()
            database.bookingDao().deleteAllEquipmentAllocations()
            database.bookingDao().deleteAllCrewAssignments()
            database.bookingDao().deleteAllDeliverables()
            database.bookingDao().deleteAllSessions()
            database.bookingDao().deleteAllBookings()
            database.equipmentDao().deleteAllEquipment()
            database.exposureDao().deleteAllFreelancers()
            database.clientDao().deleteAllClients()
            database.studioDao().deleteAllProfiles()

            // 2. Insert Studio Profile
            backupData.studioProfile?.let {
                database.studioDao().insertOrUpdateProfile(it)
            }

            // 3. Insert Clients
            if (backupData.clients.isNotEmpty()) {
                database.clientDao().insertClients(backupData.clients)
            }

            // 4. Insert Freelancers
            if (backupData.freelancers.isNotEmpty()) {
                database.exposureDao().insertFreelancers(backupData.freelancers)
            }

            // 5. Insert Equipment
            if (backupData.equipment.isNotEmpty()) {
                database.equipmentDao().insertAllEquipment(backupData.equipment)
            }

            // 6. Insert Bookings
            if (backupData.bookings.isNotEmpty()) {
                database.bookingDao().insertBookings(backupData.bookings)
            }

            // 7. Insert Sessions
            if (backupData.sessions.isNotEmpty()) {
                database.bookingDao().insertSessions(backupData.sessions)
            }

            // 8. Insert Deliverables
            if (backupData.deliverables.isNotEmpty()) {
                database.bookingDao().insertDeliverables(backupData.deliverables)
            }

            // 9. Insert Crew Assignments
            if (backupData.crewAssignments.isNotEmpty()) {
                database.bookingDao().insertCrewAssignments(backupData.crewAssignments)
            }

            // 10. Insert Equipment Allocations
            if (backupData.equipmentAllocations.isNotEmpty()) {
                database.bookingDao().insertEquipmentAllocations(backupData.equipmentAllocations)
            }

            // 11. Insert Payments
            if (backupData.payments.isNotEmpty()) {
                database.paymentLedgerDao().insertPayments(backupData.payments)
            }

            // 12. Insert Invoices
            if (backupData.invoices.isNotEmpty()) {
                database.invoiceDao().insertInvoices(backupData.invoices)
            }

            // 13. Restore Master Inventory if present
            if (backupData.masterInventoryJson.isNotBlank()) {
                DeliverableInventoryManager.importFromJson(context, backupData.masterInventoryJson)
            }

            // Update preference tracking
            val now = System.currentTimeMillis()
            getPrefs(context).edit()
                .putLong(KEY_LAST_RESTORE_TIMESTAMP, now)
                .apply()

            Log.d(TAG, "Restored successfully: ${backupData.clients.size} clients, ${backupData.bookings.size} bookings")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from backup", e)
            return@withContext false
        }
    }

    /**
     * Launches the system share/upload intent with Google Drive target and instructions.
     */
    fun shareBackupFileToGoogleDrive(context: Context, backupFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "TracStudio Daily Backup Snapshot")
            putExtra(Intent.EXTRA_TITLE, MASTER_BACKUP_FILENAME)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Chooser allows user to pick Google Drive ("Save to Drive") and select or create the 'TracStudio Backup' folder
        val chooser = Intent.createChooser(intent, "Upload to Google Drive (TracStudio Backup)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
