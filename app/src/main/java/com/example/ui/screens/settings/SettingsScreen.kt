package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.example.util.BackupFullData
import com.example.util.StudioBackupManager
import com.example.util.MasterInventoryItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.StudioProfileEntity
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.components.UpiQrPreviewDialog
import com.example.ui.components.launchPhoneCall
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.AmberGold600
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.StudioViewModel
import com.example.util.DeliverableInventoryManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val studioProfile by viewModel.studioProfile.collectAsStateWithLifecycle()
    val allClients by viewModel.allClients.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val masterInventory by viewModel.masterInventory.collectAsStateWithLifecycle()

    var showQrPreviewDialog by remember { mutableStateOf(false) }
    var showTermsPresetDialog by remember { mutableStateOf(false) }
    var showGstSettingsDialog by remember { mutableStateOf(false) }
    var showBankQuickEditDialog by remember { mutableStateOf(false) }
    var showRateCardDialog by remember { mutableStateOf(false) }
    var showPackagesDialog by remember { mutableStateOf(false) }
    var showDeliverablesDialog by remember { mutableStateOf(false) }
    var showPaymentStageGuideDialog by remember { mutableStateOf(false) }
    var showDataExportDialog by remember { mutableStateOf(false) }
    var showResetSampleDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var showBackupHubDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingRestoreData by remember { mutableStateOf<BackupFullData?>(null) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var autoBackupEnabled by remember { mutableStateOf(viewModel.isAutoBackupEnabled()) }
    var lastBackupInfo by remember { mutableStateOf(viewModel.getLastBackupInfo()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val backupData = StudioBackupManager.parseBackupJson(stream)
                    if (backupData != null) {
                        pendingRestoreData = backupData
                        showRestoreConfirmDialog = true
                    } else {
                        Toast.makeText(
                            context,
                            "Invalid or unsupported TracStudio backup file. Please select a valid JSON backup.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read backup file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Studio Settings & Control Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("settings_hub_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Studio Profile Hero Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToProfile)
                    .testTag("profile_hero_banner")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val logoFile = studioProfile?.logoUri?.let { if (it.isNotBlank()) File(it) else null }
                    val logoBmp = if (logoFile != null && logoFile.exists()) {
                        try { BitmapFactory.decodeFile(logoFile.absolutePath) } catch (e: Exception) { null }
                    } else null

                    if (logoBmp != null) {
                        Image(
                            bitmap = logoBmp.asImageBitmap(),
                            contentDescription = "Studio Logo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, AmberGold500, RoundedCornerShape(12.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmberGold500.copy(alpha = 0.15f))
                                .border(1.5.dp, AmberGold500.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = AmberGold500,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = studioProfile?.studioName ?: "TracStudio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Owner: ${studioProfile?.ownerName ?: "Studio Admin"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!studioProfile?.mobileNumber.isNullOrBlank()) {
                            Text(
                                text = "Phone: ${studioProfile?.mobileNumber}",
                                fontSize = 11.5.sp,
                                color = ApertureCyan
                            )
                        }
                        Text(
                            text = "Tap to update full profile & corner logo",
                            fontSize = 11.sp,
                            color = AmberGold500,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Edit Profile",
                        tint = AmberGold500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 2. Section: Business & Financial Settings
            Text(
                text = "BILLING & PAYMENT CONTROLS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AmberGold500,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsNavigationRow(
                        icon = Icons.Default.Business,
                        iconTint = AmberGold500,
                        title = "Studio Details & Corner Logo",
                        subtitle = "Name, address, phone, official corner logo for PDF documents",
                        onClick = onNavigateToProfile,
                        testTag = "setting_studio_profile"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.AccountBalance,
                        iconTint = ApertureCyan,
                        title = "Bank Account & UPI Details",
                        subtitle = "${studioProfile?.bankName ?: "HDFC"} • A/C: ${studioProfile?.accountNumber?.takeLast(4)?.let { "...$it" } ?: "Setup"} • UPI: ${studioProfile?.upiId ?: "Setup"}",
                        onClick = { showBankQuickEditDialog = true },
                        testTag = "setting_bank_details"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.QrCode,
                        iconTint = SuccessGreen,
                        title = "Preview & Test Dynamic UPI QR",
                        subtitle = "Test scanning the QR code that appears on invoices and quotations",
                        onClick = { showQrPreviewDialog = true },
                        testTag = "setting_preview_qr"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.Receipt,
                        iconTint = AmberGold500,
                        title = "Tax & GST Configuration",
                        subtitle = "Current rate: ${studioProfile?.taxPercentage ?: 18.0}% (Toggle GST calculation on bills)",
                        onClick = { showGstSettingsDialog = true },
                        testTag = "setting_tax_gst"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.Payments,
                        iconTint = ApertureCyan,
                        title = "Payment Stage Milestone Guide",
                        subtitle = "Standard 50% Token, 40% Main Shoot, 10% Delivery advice",
                        onClick = { showPaymentStageGuideDialog = true },
                        testTag = "setting_payment_guide"
                    )
                }
            }

            // 3. Section: Workflow, Contracts & Presets
            Text(
                text = "CONTRACTS, PACKAGES & PRESETS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AmberGold500,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsNavigationRow(
                        icon = Icons.Default.Description,
                        iconTint = ApertureCyan,
                        title = "Terms & Conditions Templates",
                        subtitle = "Wedding contracts, raw file timelines, cancellation rules",
                        onClick = { showTermsPresetDialog = true },
                        testTag = "setting_terms_preset"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.Category,
                        iconTint = AmberGold500,
                        title = "Standard Event Packages & Pricing",
                        subtitle = "Royal Wedding, Pre-Wedding, Engagement, Corporate presets",
                        onClick = { showPackagesDialog = true },
                        testTag = "setting_event_packages"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.Checklist,
                        iconTint = SuccessGreen,
                        title = "Output Deliverables & Services Inventory (આઉટપુટ ડિટેલ્સ & સર્વિસ)",
                        subtitle = "Pre-configure 30 photo edited, RAW pendrive, and event services for 1-tap addition in quotations",
                        onClick = { showDeliverablesDialog = true },
                        testTag = "setting_deliverables_catalog"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.PriceChange,
                        iconTint = AmberGold500,
                        title = "Exposure Crew Market Rate Card",
                        subtitle = "Standard shift/daily rates for photographers & drone pilots",
                        onClick = { showRateCardDialog = true },
                        testTag = "setting_crew_rates"
                    )
                }
            }

            // 4. Section: Studio Backup & Data Tools
            Text(
                text = "DATA MANAGEMENT & BACKUP",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AmberGold500,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsNavigationRow(
                        icon = Icons.Default.CloudSync,
                        iconTint = SuccessGreen,
                        title = "Google Drive Daily Auto-Backup",
                        subtitle = "Auto-backup daily to Google Drive > 'TracStudio Backup' folder • Daily replace",
                        onClick = {
                            lastBackupInfo = viewModel.getLastBackupInfo()
                            autoBackupEnabled = viewModel.isAutoBackupEnabled()
                            showBackupHubDialog = true
                        },
                        testTag = "setting_google_drive_backup"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.SettingsBackupRestore,
                        iconTint = AmberGold500,
                        title = "Restore Studio from Backup",
                        subtitle = "Recover all clients, bookings, invoices, and ledgers after reinstall or new device",
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        testTag = "setting_restore_backup"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.Share,
                        iconTint = ApertureCyan,
                        title = "Export Studio Data Snapshot",
                        subtitle = "${allClients.size} clients, ${allBookings.size} events, ${allPayments.size} payment vouchers",
                        onClick = { showDataExportDialog = true },
                        testTag = "setting_backup"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.Refresh,
                        iconTint = AmberGold500,
                        title = "Reload Sample Studio Demo Data",
                        subtitle = "Repopulate complete demo wedding bookings, gear, and crew",
                        onClick = { showResetSampleDataDialog = true },
                        testTag = "setting_reload_sample"
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Default.Info,
                        iconTint = Color.Gray,
                        title = "App Info & Studio Support",
                        subtitle = "TracStudio Pro v1.2.0 • Photography Studio ERP",
                        onClick = { showAboutDialog = true },
                        testTag = "setting_about"
                    )
                }
            }
        }
    }

    // --- Dialog 1: UPI QR Preview ---
    if (showQrPreviewDialog) {
        val upi = studioProfile?.upiId?.ifBlank { "tracstudio@hdfcbank" } ?: "tracstudio@hdfcbank"
        val payee = studioProfile?.accountHolderName?.ifBlank { studioProfile?.studioName ?: "TracStudio" } ?: "TracStudio"
        UpiQrPreviewDialog(
            upiId = upi,
            payeeName = payee,
            onDismiss = { showQrPreviewDialog = false }
        )
    }

    // --- Dialog 2: Bank & UPI Quick Edit ---
    if (showBankQuickEditDialog) {
        var currentBank by remember { mutableStateOf(studioProfile?.bankName ?: "") }
        var currentHolder by remember { mutableStateOf(studioProfile?.accountHolderName ?: "") }
        var currentAccount by remember { mutableStateOf(studioProfile?.accountNumber ?: "") }
        var currentIfsc by remember { mutableStateOf(studioProfile?.ifscCode ?: "") }
        var currentUpi by remember { mutableStateOf(studioProfile?.upiId ?: "") }

        AlertDialog(
            onDismissRequest = { showBankQuickEditDialog = false },
            title = { Text("Bank & UPI Details") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("These banking details appear in the payment footer of every PDF Quotation and Invoice.", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = currentBank,
                        onValueChange = { currentBank = it },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = currentHolder,
                        onValueChange = { currentHolder = it },
                        label = { Text("Account Holder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = currentAccount,
                        onValueChange = { currentAccount = it },
                        label = { Text("Account Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = currentIfsc,
                        onValueChange = { currentIfsc = it },
                        label = { Text("IFSC Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = currentUpi,
                        onValueChange = { currentUpi = it },
                        label = { Text("UPI ID (VPA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        studioProfile?.let {
                            viewModel.updateStudioProfile(
                                it.copy(
                                    bankName = currentBank.trim(),
                                    accountHolderName = currentHolder.trim(),
                                    accountNumber = currentAccount.trim(),
                                    ifscCode = currentIfsc.trim().uppercase(),
                                    upiId = currentUpi.trim()
                                )
                            )
                            Toast.makeText(context, "Bank and UPI details updated!", Toast.LENGTH_SHORT).show()
                        }
                        showBankQuickEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)
                ) {
                    Text("Save Details")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBankQuickEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- Dialog 3: GST & Tax Config Dialog ---
    if (showGstSettingsDialog) {
        var currentTax by remember { mutableStateOf(studioProfile?.taxPercentage?.toString() ?: "18.0") }
        AlertDialog(
            onDismissRequest = { showGstSettingsDialog = false },
            title = { Text("Tax & GST Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Set the standard tax percentage calculated on invoices when GST toggle is active.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = currentTax,
                        onValueChange = { currentTax = it },
                        label = { Text("Tax / GST Percentage (%)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("tax_rate_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("0.0", "5.0", "12.0", "18.0", "28.0").forEach { rate ->
                            FilterChip(
                                selected = currentTax == rate,
                                onClick = { currentTax = rate },
                                label = { Text("$rate%") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val taxVal = currentTax.toDoubleOrNull() ?: 18.0
                        studioProfile?.let {
                            viewModel.updateStudioProfile(it.copy(taxPercentage = taxVal))
                            Toast.makeText(context, "GST rate updated to $taxVal%!", Toast.LENGTH_SHORT).show()
                        }
                        showGstSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)
                ) {
                    Text("Save Tax Rate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGstSettingsDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- Dialog 4: Terms & Conditions Preset Selector ---
    if (showTermsPresetDialog) {
        val presets = listOf(
            "Royal Wedding Contract" to "1. 50% advance token at booking confirmation.\n2. 40% mid-event payment on main wedding day.\n3. 10% balance due prior to final edited album and teaser delivery.\n4. Raw photos shared on drive within 7 days.\n5. Album photo selection within 30 days.\n6. Advance is non-refundable if cancelled within 30 days of event.",
            "Pre-Wedding & Destination Shoot" to "1. 60% advance payable at confirmation.\n2. 40% balance payable on completion of shoot days.\n3. Client covers crew travel, stay, and food for destination shoots.\n4. 5 Cinematic Reels and 50 Edited Photos included.\n5. In case of rain or bad weather, shoot will be rescheduled at mutual availability.",
            "Commercial & Corporate Event" to "1. Official Purchase Order (PO) required for date booking.\n2. Full payment within 15 days of invoice submission.\n3. Complete uncompressed 4K footage delivered on physical SSD.\n4. Full commercial broadcast and digital marketing rights transferred to client.",
            "Babyshower & Ring Ceremony" to "1. 50% token advance upon booking.\n2. 50% balance settlement on event day.\n3. Traditional video + highlight teaser + 100 edited portraits included.\n4. Cloud drive delivery within 10 days."
        )

        AlertDialog(
            onDismissRequest = { showTermsPresetDialog = false },
            title = { Text("Apply Terms & Conditions Preset") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose an industry-standard contract preset to update your default invoice footer terms:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    presets.forEach { (name, terms) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    studioProfile?.let {
                                        viewModel.updateStudioProfile(it.copy(defaultTermsAndConditions = terms))
                                        Toast.makeText(context, "Applied '$name' preset!", Toast.LENGTH_SHORT).show()
                                    }
                                    showTermsPresetDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberGold500)
                                Text(terms.take(90) + "...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTermsPresetDialog = false }) { Text("Close") }
            }
        )
    }

    // --- Dialog 5: Standard Event Packages & Pricing Presets ---
    if (showPackagesDialog) {
        val packages = listOf(
            Triple("Royal Wedding Cinema + Photo", "₹2,50,000 - ₹4,50,000", "3 Days • 2 Cinematographers, 2 Candid Photographers, 1 Drone Pilot, Velvet Album (40 sheets), 5 Reels, Full Multi-Cam Film"),
            Triple("Pre-Wedding Cinematic Love Story", "₹65,000 - ₹1,20,000", "1-2 Days • Drone 4K, 1 Lead Cinematographer, 1 Candid Photographer, 50 Retouched Photos, 3-min Cinematic Teaser"),
            Triple("Engagement & Ring Ceremony", "₹45,000 - ₹80,000", "1 Day / Evening • 1 Candid Photographer, 1 Traditional Videographer, 1 Highlight Teaser, Mini Photo Album"),
            Triple("Corporate Annual Summit / Gala", "₹80,000 - ₹1,50,000", "Full Day Coverage • Multi-Camera Livestream, Corporate Promo Cut, High-Res Speaker Portraits, Uncompressed SSD handover"),
            Triple("Maternity & Babyshower Special", "₹35,000 - ₹55,000", "Half Day / Session • Creative Lighting, 1 Candid Artist, 1 Reel Specialist, 40 Edited Photos")
        )

        AlertDialog(
            onDismissRequest = { showPackagesDialog = false },
            title = { Text("Standard Event Packages & Pricing") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Reference industry benchmark packages for client quotations:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    packages.forEach { (name, price, details) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberGold500, modifier = Modifier.weight(1f))
                                    Text(price, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = ApertureCyan)
                                }
                                Text(details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPackagesDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)) {
                    Text("Done")
                }
            }
        )
    }

    // --- Dialog 6: Deliverables & Services Inventory Master (આઉટપુટ ડિટેલ્સ & સર્વિસ) ---
    if (showDeliverablesDialog) {
        val context = LocalContext.current
        var inventoryItems by remember {
            mutableStateOf(DeliverableInventoryManager.getAllItems(context))
        }
        var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "OUTPUT_DELIVERABLE", "EVENT_SERVICE"
        var showAddNewDialog by remember { mutableStateOf(false) }

        var newTitle by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("OUTPUT_DELIVERABLE") }
        var newDefaultQty by remember { mutableStateOf("1") }
        var newDefaultPrice by remember { mutableStateOf("0") }
        var newDesc by remember { mutableStateOf("") }

        val filteredItems = remember(inventoryItems, selectedFilter) {
            when (selectedFilter) {
                "OUTPUT_DELIVERABLE" -> inventoryItems.filter { it.category == "OUTPUT_DELIVERABLE" }
                "EVENT_SERVICE" -> inventoryItems.filter { it.category == "EVENT_SERVICE" }
                else -> inventoryItems
            }
        }

        AlertDialog(
            onDismissRequest = { showDeliverablesDialog = false },
            title = {
                Column {
                    Text(
                        text = "Inventory Master (આઉટપુટ ડિટેલ્સ & સર્વિસ)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Pre-configured deliverables & services for 1-tap quotation adding",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    // Category Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All (${inventoryItems.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "OUTPUT_DELIVERABLE",
                            onClick = { selectedFilter = "OUTPUT_DELIVERABLE" },
                            label = { Text("Deliverables", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "EVENT_SERVICE",
                            onClick = { selectedFilter = "EVENT_SERVICE" },
                            label = { Text("Services", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Items List
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredItems.forEach { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (item.category == "OUTPUT_DELIVERABLE") ApertureCyan.copy(alpha = 0.2f) else AmberGold500.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (item.category == "OUTPUT_DELIVERABLE") "Deliverable" else "Service",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.category == "OUTPUT_DELIVERABLE") ApertureCyan else AmberGold500,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            )
                                        }
                                        if (item.description.isNotBlank()) {
                                            Text(
                                                text = item.description,
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                        if (item.defaultPrice > 0 || item.defaultQty > 1) {
                                            val extraInfo = buildString {
                                                if (item.defaultQty > 1) append("Default Qty: ${item.defaultQty}")
                                                if (item.defaultPrice > 0) {
                                                    if (isNotEmpty()) append(" • ")
                                                    append("Default Rate: ₹${item.defaultPrice.toInt()}")
                                                }
                                            }
                                            Text(
                                                text = extraInfo,
                                                fontSize = 10.sp,
                                                color = AmberGold500,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            DeliverableInventoryManager.deleteItem(context, item.id)
                                            inventoryItems = DeliverableInventoryManager.getAllItems(context)
                                            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete item",
                                            tint = DangerRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                newTitle = ""
                                newCategory = if (selectedFilter == "EVENT_SERVICE") "EVENT_SERVICE" else "OUTPUT_DELIVERABLE"
                                newDefaultQty = "1"
                                newDefaultPrice = "0"
                                newDesc = ""
                                showAddNewDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Item", fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                inventoryItems = DeliverableInventoryManager.resetToDefaults(context)
                                Toast.makeText(context, "Reset to standard defaults", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Defaults", fontSize = 11.5.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeliverablesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)
                ) {
                    Text("Done")
                }
            }
        )

        // Mini Dialog to Add New Master Item
        if (showAddNewDialog) {
            AlertDialog(
                onDismissRequest = { showAddNewDialog = false },
                title = { Text("Add Master Inventory Entry", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Title (e.g. 50 photo edited / Crane)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Category:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = newCategory == "OUTPUT_DELIVERABLE",
                                onClick = { newCategory = "OUTPUT_DELIVERABLE" },
                                label = { Text("Output Deliverable", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = newCategory == "EVENT_SERVICE",
                                onClick = { newCategory = "EVENT_SERVICE" },
                                label = { Text("Event Service", fontSize = 11.sp) }
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newDefaultQty,
                                onValueChange = { newDefaultQty = it },
                                label = { Text("Default Qty") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newDefaultPrice,
                                onValueChange = { newDefaultPrice = it },
                                label = { Text("Default Rate (₹)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                val item = MasterInventoryItem(
                                    id = "item_${System.currentTimeMillis()}",
                                    title = newTitle.trim(),
                                    category = newCategory,
                                    defaultQty = newDefaultQty.toIntOrNull() ?: 1,
                                    defaultPrice = newDefaultPrice.toDoubleOrNull() ?: 0.0,
                                    description = newDesc.trim()
                                )
                                DeliverableInventoryManager.addItem(context, item)
                                inventoryItems = DeliverableInventoryManager.getAllItems(context)
                                showAddNewDialog = false
                                Toast.makeText(context, "Added to Inventory Master", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)
                    ) {
                        Text("Add to Master")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddNewDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // --- Dialog 7: Payment Stage Milestone Guide ---
    if (showPaymentStageGuideDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentStageGuideDialog = false },
            title = { Text("Payment Milestone Best Practices") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("To avoid unpaid client balances, Indian photography studios follow this 3-stage milestone structure:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("1. Stage 1: Booking Token (25% - 50%)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = SuccessGreen)
                            Text("Paid immediately upon date lock. Non-refundable. Blocks team and camera gear from other inquiries.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("2. Stage 2: Main Event Shoot Day (40%)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = AmberGold500)
                            Text("Settled on or before the main wedding reception night. Covers immediate freelance crew payouts and expenses.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("3. Stage 3: Final Delivery Handover (10%)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = ApertureCyan)
                            Text("Cleared prior to printing physical albums or releasing high-res uncompressed master SSD drives.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPaymentStageGuideDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)) {
                    Text("Understood")
                }
            }
        )
    }

    // --- Dialog 8: Exposure Crew Standard Rate Card ---
    if (showRateCardDialog) {
        val standardRoles = listOf(
            "Lead Cinematographer" to "₹15,000 - ₹20,000 / day",
            "Candid Photographer" to "₹12,000 - ₹16,000 / day",
            "Drone Pilot (4K/FPV)" to "₹10,000 - ₹14,000 / day",
            "Traditional Videographer" to "₹8,000 - ₹10,000 / day",
            "Traditional Photographer" to "₹7,000 - ₹9,000 / day",
            "Crane / Jib Operator" to "₹9,000 - ₹12,000 / day",
            "Cinematic Teaser Editor" to "₹8,000 - ₹15,000 / project"
        )

        AlertDialog(
            onDismissRequest = { showRateCardDialog = false },
            title = { Text("Standard Crew Rate Card") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Studio benchmark rates for camera crew & editing technicians:",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    standardRoles.forEach { (role, rate) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(role, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(rate, fontSize = 11.5.sp, color = AmberGold500, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRateCardDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // --- Dialog 9: Data Backup & Snapshot Export ---
    if (showDataExportDialog) {
        AlertDialog(
            onDismissRequest = { showDataExportDialog = false },
            title = { Text("Export Studio Data Snapshot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Export all studio records into a clean text/JSON snapshot to backup on WhatsApp, Google Drive, or Email.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Registered Clients: ${allClients.size}", fontSize = 12.sp)
                            Text("• Total Bookings & Quotations: ${allBookings.size}", fontSize = 12.sp)
                            Text("• Financial Ledger Entries: ${allPayments.size}", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val report = buildString {
                            appendLine("=== TRACSTUDIO ERP BACKUP SNAPSHOT ===")
                            appendLine("Exported on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
                            appendLine("Studio: ${studioProfile?.studioName}")
                            appendLine("Owner: ${studioProfile?.ownerName} (${studioProfile?.mobileNumber})")
                            appendLine("\n--- CLIENTS (${allClients.size}) ---")
                            allClients.forEach { c ->
                                appendLine("• ${c.name} | Phone: ${c.contactNumber} | Address: ${c.address}")
                            }
                            appendLine("\n--- BOOKINGS & EVENTS (${allBookings.size}) ---")
                            allBookings.forEach { b ->
                                appendLine("• [${b.status}] ${b.clientName} - ${b.eventType} | Total: ₹${b.grandTotal} | Bal: ₹${b.balanceDue}")
                            }
                            appendLine("\n--- FINANCIAL LEDGER (${allPayments.size}) ---")
                            allPayments.forEach { p ->
                                appendLine("• [${p.type}] ₹${p.amount} on ${p.paymentDate} via ${p.paymentMode} (${p.partyName})")
                            }
                        }

                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "TracStudio Data Backup")
                            putExtra(Intent.EXTRA_TEXT, report)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Backup Studio Data"))
                        showDataExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White)
                ) {
                    Text("Export & Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDataExportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- Dialog 10: Reset & Reload Sample Data ---
    if (showResetSampleDataDialog) {
        AlertDialog(
            onDismissRequest = { showResetSampleDataDialog = false },
            title = { Text("Reload Demo Studio Data?") },
            text = {
                Text(
                    text = "This will refresh all sample clients, multi-day wedding events, cinema gear, and freelancer ledgers. Useful for demoing and testing the complete ERP system.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reseedSampleData {
                            Toast.makeText(context, "Sample demo studio data reloaded successfully!", Toast.LENGTH_SHORT).show()
                        }
                        showResetSampleDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)
                ) {
                    Text("Reload Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSampleDataDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- Dialog 11: About & Support Dialog ---
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About TracStudio ERP") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TracStudio Professional Edition", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                    Text("Version 1.2.0 • Production Build", fontSize = 12.sp)
                    Text(
                        "Designed exclusively for Photography & Videography Studios, Wedding Cinema Teams, and Media Production Houses.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Core Modules:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("• Client Directory & Outstanding Balance Tracking\n• Multi-Day Booking & Multi-Session Event Workflow\n• Exposure Freelancer Booking & Conflict Alerts\n• Camera & Lens Inventory Overlap Prevention\n• Dynamic UPI QR Invoices & Professional PDF Generator\n• Open, Download & Direct Share PDF Engine", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { launchPhoneCall(context, "+919876543210") },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Support", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { launchWhatsApp(context, "+919876543210", "Hello TracStudio Support Team, I need assistance with my studio ERP.") },
                            colors = ButtonDefaults.buttonColors(containerColor = ApertureCyan, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // --- Dialog 12: Google Drive Daily Auto-Backup Hub ---
    if (showBackupHubDialog) {
        AlertDialog(
            onDismissRequest = { showBackupHubDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Drive Auto-Backup")
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Auto-Backup Switch Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Daily Auto-Backup", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    Text(
                                        "Replace previous backup daily with latest snapshot",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = autoBackupEnabled,
                                    onCheckedChange = {
                                        autoBackupEnabled = it
                                        viewModel.setAutoBackupEnabled(it)
                                        lastBackupInfo = viewModel.getLastBackupInfo()
                                        Toast.makeText(
                                            context,
                                            if (it) "Daily Auto-Backup enabled!" else "Daily Auto-Backup paused.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = SuccessGreen)
                                )
                            }
                        }
                    }

                    // Backup Destination and Specifications
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = AmberGold500, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Drive Folder: TracStudio Backup", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AmberGold500)
                            }
                            Text("• File: TracStudio_AutoBackup.json (Daily Auto-Replaced)", fontSize = 11.5.sp)
                            Text("• Last Auto-Backup: ${lastBackupInfo["date"]} (${lastBackupInfo["size"]})", fontSize = 11.5.sp)
                            Text("• Summary: ${lastBackupInfo["summary"]}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "When app is uninstalled and reinstalled, you can restore all clients, wedding bookings, gear, and ledgers in 1 click from this file.",
                                fontSize = 11.sp,
                                color = ApertureCyan
                            )
                        }
                    }

                    // Backup Now Button
                    Button(
                        onClick = {
                            isBackingUp = true
                            viewModel.triggerManualBackup { file ->
                                isBackingUp = false
                                if (file != null && file.exists()) {
                                    lastBackupInfo = viewModel.getLastBackupInfo()
                                    StudioBackupManager.shareBackupFileToGoogleDrive(context, file)
                                    Toast.makeText(
                                        context,
                                        "Backup ready! Select Google Drive and save to 'TracStudio Backup' folder.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(context, "Error creating backup file", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isBackingUp
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating Backup...", fontSize = 12.5.sp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Backup Now & Save to Google Drive", fontSize = 12.5.sp)
                        }
                    }

                    // Restore from Google Drive Button
                    Button(
                        onClick = {
                            showBackupHubDialog = false
                            filePickerLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore from Google Drive / File", fontSize = 12.5.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupHubDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // --- Dialog 13: Restore Confirmation Preview Dialog ---
    if (showRestoreConfirmDialog && pendingRestoreData != null) {
        val restore = pendingRestoreData!!
        AlertDialog(
            onDismissRequest = {
                if (!isRestoring) showRestoreConfirmDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, tint = AmberGold500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore TracStudio Backup")
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Backup file verified successfully! You can restore your studio records into TracStudio. Ideal after reinstalling the app or setting up a new device.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Studio: ${restore.metadata.studioName.ifBlank { "TracStudio" }}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberGold500)
                            Text("Owner: ${restore.metadata.ownerName}", fontSize = 12.sp)
                            Text("Backup Date: ${restore.metadata.backupDate}", fontSize = 11.5.sp, color = ApertureCyan)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text("• Clients to restore: ${restore.clients.size}", fontSize = 11.5.sp)
                            Text("• Bookings & Events: ${restore.bookings.size}", fontSize = 11.5.sp)
                            Text("• Invoices & Quotations: ${restore.invoices.size}", fontSize = 11.5.sp)
                            Text("• Ledger Entries: ${restore.payments.size}", fontSize = 11.5.sp)
                            Text("• Freelance Crew: ${restore.freelancers.size}", fontSize = 11.5.sp)
                            Text("• Equipment Items: ${restore.equipment.size}", fontSize = 11.5.sp)
                        }
                    }

                    Text(
                        text = "⚠️ Notice: Restoring will overwrite existing records with the data from this backup snapshot.",
                        fontSize = 11.sp,
                        color = AmberGold600,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoring = true
                        viewModel.restoreFromBackupData(restore) { success ->
                            isRestoring = false
                            showRestoreConfirmDialog = false
                            pendingRestoreData = null
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "TracStudio restored successfully! All clients, events, invoices, and ledgers recovered.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(context, "Failed to restore backup", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White),
                    enabled = !isRestoring
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restoring Data...", fontSize = 12.sp)
                    } else {
                        Text("Confirm & Restore All Data", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                if (!isRestoring) {
                    TextButton(onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreData = null
                    }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun SettingsNavigationRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}
