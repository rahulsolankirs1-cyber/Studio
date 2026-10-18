package com.example.ui.screens.bookings

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BookingCrewAssignmentEntity
import com.example.data.local.entity.BookingDeliverableEntity
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.BookingEquipmentAllocationEntity
import com.example.data.local.entity.BookingSessionEntity
import com.example.data.local.entity.PaymentLedgerEntity
import com.example.domain.model.SessionServiceItem
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.components.PdfActionDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.StudioViewModel
import com.example.util.PdfGenerator
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: Long,
    viewModel: StudioViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val booking = allBookings.firstOrNull { it.id == bookingId }

    var sessions by remember { mutableStateOf<List<BookingSessionEntity>>(emptyList()) }
    var deliverables by remember { mutableStateOf<List<BookingDeliverableEntity>>(emptyList()) }
    var crewAssignments by remember { mutableStateOf<List<BookingCrewAssignmentEntity>>(emptyList()) }
    var equipmentAllocations by remember { mutableStateOf<List<BookingEquipmentAllocationEntity>>(emptyList()) }

    var showPaymentDialog by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(bookingId) {
        sessions = viewModel.getSessionsForBookingDirect(bookingId)
        deliverables = viewModel.getDeliverablesForBookingDirect(bookingId)
        crewAssignments = viewModel.getCrewAssignmentsForBookingDirect(bookingId)
        equipmentAllocations = viewModel.getEquipmentAllocationsForBookingDirect(bookingId)
    }

    if (booking == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Booking not found.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = booking.clientName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        )
                        Text(
                            text = booking.eventType,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                Toast.makeText(context, "Preparing PDF Document...", Toast.LENGTH_SHORT).show()
                                viewModel.generateAndSharePdf(booking.id) { pdfFile ->
                                    generatedPdfFile = pdfFile
                                }
                            }
                        },
                        modifier = Modifier.testTag("share_pdf_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share PDF",
                            tint = AmberGold500
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
                .testTag("booking_detail_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status & Conversion Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Booking Status",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            StatusBadge(status = booking.status, modifier = Modifier.padding(top = 4.dp))
                        }

                        if (booking.status == "QUOTATION") {
                            Button(
                                onClick = {
                                    viewModel.convertQuotationToBooking(booking.id) {
                                        Toast.makeText(context, "Quotation converted to Confirmed Booking & Final Invoice!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("convert_to_booking_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Confirm & Invoice", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (booking.notes.isNotBlank()) {
                        Text(
                            text = "Notes: ${booking.notes}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // PDF Invoice / Quotation Quick Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = ApertureCyan.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (booking.status == "QUOTATION") "Official Quotation PDF" else "Tax / Final Invoice PDF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = ApertureCyan
                        )
                        Text(
                            text = "Includes QR payment footer, schedule & bank details",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                Toast.makeText(context, "Preparing PDF Document...", Toast.LENGTH_SHORT).show()
                                viewModel.generateAndSharePdf(booking.id) { pdfFile ->
                                    generatedPdfFile = pdfFile
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ApertureCyan, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("generate_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Financial Summary & Record Payment
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Financial Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                        Button(
                            onClick = { showPaymentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("record_payment_button")
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Package Base Amount:", fontSize = 12.sp)
                        Text(IndianCurrencyFormatter.format(booking.totalAmount), fontSize = 12.sp)
                    }
                    if (booking.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount Applied:", fontSize = 12.sp, color = SuccessGreen)
                            Text("- ${IndianCurrencyFormatter.format(booking.discountAmount)}", fontSize = 12.sp, color = SuccessGreen)
                        }
                    }
                    if (booking.taxEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST (${booking.taxPercent}%):", fontSize = 12.sp)
                            Text("+ ${IndianCurrencyFormatter.format(booking.taxAmount)}", fontSize = 12.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total:", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text(IndianCurrencyFormatter.format(booking.grandTotal), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Advance Paid Received:", fontSize = 12.sp)
                        Text(IndianCurrencyFormatter.format(booking.advancePaid), fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("REMAINING BALANCE DUE:", fontWeight = FontWeight.Bold, color = DangerRed, fontSize = 13.sp)
                            Text(IndianCurrencyFormatter.format(booking.balanceDue), fontWeight = FontWeight.ExtraBold, color = DangerRed, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Sessions Schedule with Services Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Multi-Day Sessions Schedule (${sessions.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                        Text("Dates & Coverage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    sessions.forEach { s ->
                        val serviceList = SessionServiceItem.parseList(s.servicesJson)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(s.sessionTitle, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    Surface(
                                        color = ApertureCyan.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = s.date,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ApertureCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                val timing = if (s.startTime.isNotBlank()) "${s.startTime} - ${s.endTime}" else "Full Day"
                                val venue = if (s.venueName.isNotBlank()) s.venueName else s.city
                                val locationLine = if (venue.isNotBlank()) "$timing • $venue" else timing
                                Text(locationLine, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                // Services breakdown within this session
                                if (serviceList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Included Coverage Services:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AmberGold500)
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        serviceList.forEach { srv ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        color = AmberGold500.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "Qty ${srv.quantity}",
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AmberGold500,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(srv.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                }
                                                if (srv.unitPrice > 0) {
                                                    Text(
                                                        text = "₹${srv.unitPrice.toInt()} ea (₹${(srv.unitPrice * srv.quantity).toInt()})",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Output Details & Final Client Handover
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Output Details & Deliverables (${deliverables.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                        Text("Client Handover", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    deliverables.forEach { d ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = d.isCompleted,
                                        onCheckedChange = { isChecked ->
                                            val updated = d.copy(isCompleted = isChecked)
                                            deliverables = deliverables.map { if (it.id == d.id) updated else it }
                                            viewModel.updateDeliverableCompleted(d, isChecked)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = d.title,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            textDecoration = if (d.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                            color = if (d.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (d.quantity > 1) {
                                            Text("Quantity: ${d.quantity}", fontSize = 11.sp, color = ApertureCyan)
                                        }
                                    }
                                }
                                Surface(
                                    color = if (d.isCompleted) SuccessGreen.copy(alpha = 0.15f) else AmberGold500.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (d.isCompleted) "Completed" else "Included",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (d.isCompleted) SuccessGreen else AmberGold500,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Assigned Crew
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Assigned Freelancer Crew (${crewAssignments.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                    if (crewAssignments.isEmpty()) {
                        Text("No crew assigned.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        crewAssignments.forEach { c ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(c.freelancerName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    Text("${c.role} • ${c.date}", fontSize = 11.sp, color = ApertureCyan)
                                }
                                Text(
                                    IndianCurrencyFormatter.format(c.agreedRate),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Payment Dialog
    if (showPaymentDialog) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var pAmountText by remember { mutableStateOf(booking.balanceDue.toInt().toString()) }
        var pDate by remember { mutableStateOf(dateFormat.format(Date())) }
        var pMode by remember { mutableStateOf("UPI") }
        var pStage by remember { mutableStateOf(if (booking.advancePaid == 0.0) "ADVANCE" else "MID_EVENT") }
        var pRef by remember { mutableStateOf("") }
        var pNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Record Customer Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Client: ${booking.clientName}  |  Balance: ${IndianCurrencyFormatter.format(booking.balanceDue)}", fontSize = 12.sp)
                    OutlinedTextField(
                        value = pAmountText,
                        onValueChange = { pAmountText = it },
                        label = { Text("Amount Received (₹) *") },
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )
                    OutlinedTextField(
                        value = pDate,
                        onValueChange = { pDate = it },
                        label = { Text("Payment Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("UPI", "CASH", "BANK_TRANSFER", "CHEQUE").forEach { m ->
                            FilterChip(
                                selected = pMode == m,
                                onClick = { pMode = m },
                                label = { Text(m) }
                            )
                        }
                    }
                    Text("Payment Stage", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ADVANCE", "MID_EVENT", "FINAL_DELIVERY").forEach { st ->
                            FilterChip(
                                selected = pStage == st,
                                onClick = { pStage = st },
                                label = { Text(st) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = pRef,
                        onValueChange = { pRef = it },
                        label = { Text("Txn ID / UTR / Cheque No") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = pAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordCustomerPayment(
                                bookingId = booking.id,
                                clientId = booking.clientId,
                                clientName = booking.clientName,
                                amount = amt,
                                date = pDate,
                                mode = pMode,
                                stage = pStage,
                                referenceNumber = pRef.trim(),
                                notes = pNotes.trim()
                            )
                            Toast.makeText(context, "Payment recorded and balance reconciled!", Toast.LENGTH_SHORT).show()
                            showPaymentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                    modifier = Modifier.testTag("confirm_record_payment_button")
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("Cancel") }
            }
        )
    }

    generatedPdfFile?.let { file ->
        PdfActionDialog(
            pdfFile = file,
            title = if (booking.status == "QUOTATION") "Quotation PDF Ready" else "Tax Invoice PDF Ready",
            onDismiss = { generatedPdfFile = null }
        )
    }
}
