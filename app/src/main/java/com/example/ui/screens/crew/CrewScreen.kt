package com.example.ui.screens.crew

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ExposureFreelancerEntity
import com.example.domain.model.ExposureSettlementSummary
import com.example.domain.model.StandardPresets
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.components.launchPhoneCall
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrewScreen(
    viewModel: StudioViewModel
) {
    val context = LocalContext.current
    val freelancers by viewModel.allFreelancers.collectAsStateWithLifecycle()
    val settlements by viewModel.exposureSettlements.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val crewPayments = allPayments.filter { it.type == "EXPOSURE_PAYOUT" }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedRoleFilter by remember { mutableStateOf<String?>(null) }
    var showAddCrewDialog by remember { mutableStateOf(false) }
    var editingCrew by remember { mutableStateOf<ExposureFreelancerEntity?>(null) }
    var settlingCrewSummary by remember { mutableStateOf<ExposureSettlementSummary?>(null) }

    val filteredCrew = freelancers.filter {
        selectedRoleFilter == null || it.role == selectedRoleFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Exposure & Crew Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        editingCrew = null
                        showAddCrewDialog = true
                    },
                    containerColor = AmberGold500,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("add_crew_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Freelancer")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("crew_screen")
        ) {
            // Tabs: Directory vs Payment Ledger
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Crew Directory (${freelancers.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_crew_directory")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Exposure Ledger", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_crew_ledger")
                )
            }

            if (selectedTabIndex == 0) {
                // Filter chips by role
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRoleFilter == null,
                            onClick = { selectedRoleFilter = null },
                            label = { Text("All Roles") }
                        )
                    }
                    items(StandardPresets.ExposureRoles) { role ->
                        FilterChip(
                            selected = selectedRoleFilter == role,
                            onClick = {
                                selectedRoleFilter = if (selectedRoleFilter == role) null else role
                            },
                            label = { Text(role) }
                        )
                    }
                }

                if (filteredCrew.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No crew members found. Tap '+' to add a freelancer.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredCrew, key = { it.id }) { crew ->
                            CrewMemberCard(
                                crew = crew,
                                onEdit = {
                                    editingCrew = crew
                                    showAddCrewDialog = true
                                },
                                onCall = { launchPhoneCall(context, crew.contactNumber) },
                                onWhatsApp = {
                                    launchWhatsApp(
                                        context,
                                        crew.contactNumber,
                                        "Hi ${crew.name}, this is regarding upcoming shoot dates at the studio."
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                // Exposure Payment Ledger Tab
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Pending Crew Settlement Summaries",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AmberGold500
                        )
                    }
                    items(settlements) { s ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(s.freelancerName, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                        Text(s.role, fontSize = 11.5.sp, color = ApertureCyan)
                                    }
                                    if (s.pendingPayout > 0) {
                                        Button(
                                            onClick = { settlingCrewSummary = s },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AmberGold500,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("pay_voucher_${s.freelancerId}")
                                        ) {
                                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pay Voucher", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Surface(
                                            color = SuccessGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Settled",
                                                color = SuccessGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Agreed: ${IndianCurrencyFormatter.format(s.totalAgreedRate)}", fontSize = 11.5.sp)
                                    Text("Paid: ${IndianCurrencyFormatter.format(s.totalSettled)}", fontSize = 11.5.sp, color = SuccessGreen)
                                    Text("Pending: ${IndianCurrencyFormatter.format(s.pendingPayout)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (s.pendingPayout > 0) DangerRed else SuccessGreen)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Recent Payout Vouchers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (crewPayments.isEmpty()) {
                        item {
                            Text("No payout vouchers recorded yet.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        items(crewPayments.take(15)) { p ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(p.partyName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "${p.paymentDate} • via ${p.paymentMode}${if (p.referenceNumber.isNotBlank()) " (${p.referenceNumber})" else ""}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = IndianCurrencyFormatter.format(p.amount),
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold500,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Crew Dialog
    if (showAddCrewDialog) {
        AddEditCrewDialog(
            initialCrew = editingCrew,
            onDismiss = { showAddCrewDialog = false },
            onSave = { name, role, phone, address, dailyRate, shiftRate, upi ->
                if (editingCrew != null) {
                    viewModel.updateFreelancer(
                        editingCrew!!.copy(
                            name = name,
                            role = role,
                            contactNumber = phone,
                            address = address,
                            defaultDailyRate = dailyRate,
                            defaultShiftRate = shiftRate,
                            upiId = upi
                        )
                    )
                } else {
                    viewModel.addFreelancer(
                        ExposureFreelancerEntity(
                            name = name,
                            role = role,
                            contactNumber = phone,
                            address = address,
                            defaultDailyRate = dailyRate,
                            defaultShiftRate = shiftRate,
                            upiId = upi
                        )
                    )
                }
                showAddCrewDialog = false
            }
        )
    }

    // Pay Settlement Voucher Dialog
    settlingCrewSummary?.let { s ->
        RecordPayoutDialog(
            summary = s,
            onDismiss = { settlingCrewSummary = null },
            onConfirmPayout = { amount, date, mode, ref, notes ->
                viewModel.recordCrewPayout(
                    freelancerId = s.freelancerId,
                    freelancerName = s.freelancerName,
                    amount = amount,
                    date = date,
                    mode = mode,
                    referenceNumber = ref,
                    notes = notes
                )
                Toast.makeText(context, "Payout voucher recorded successfully!", Toast.LENGTH_SHORT).show()
                settlingCrewSummary = null
            }
        )
    }
}

@Composable
fun CrewMemberCard(
    crew: ExposureFreelancerEntity,
    onEdit: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("crew_card_${crew.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(crew.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Surface(
                        color = ApertureCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = crew.role,
                            color = ApertureCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onWhatsApp,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = "WhatsApp", tint = ApertureCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daily Rate: ${IndianCurrencyFormatter.format(crew.defaultDailyRate)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AmberGold500
                )
                Text(
                    text = "Shift Rate: ${IndianCurrencyFormatter.format(crew.defaultShiftRate)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (crew.upiId.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "UPI: ${crew.upiId}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCrewDialog(
    initialCrew: ExposureFreelancerEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, phone: String, address: String, dailyRate: Double, shiftRate: Double, upi: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCrew?.name ?: "") }
    var role by remember { mutableStateOf(initialCrew?.role ?: StandardPresets.ExposureRoles.first()) }
    var phone by remember { mutableStateOf(initialCrew?.contactNumber ?: "") }
    var address by remember { mutableStateOf(initialCrew?.address ?: "") }
    var dailyRateText by remember { mutableStateOf(initialCrew?.defaultDailyRate?.toInt()?.toString() ?: "10000") }
    var shiftRateText by remember { mutableStateOf(initialCrew?.defaultShiftRate?.toInt()?.toString() ?: "6000") }
    var upi by remember { mutableStateOf(initialCrew?.upiId ?: "") }

    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCrew != null) "Edit Exposure Freelancer" else "Add Exposure Freelancer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Artist / Freelancer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("crew_name_input")
                )

                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Specialist Role *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        StandardPresets.ExposureRoles.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("crew_phone_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dailyRateText,
                        onValueChange = { dailyRateText = it },
                        label = { Text("Daily Rate (₹)") },
                        modifier = Modifier.weight(1f).testTag("crew_daily_rate_input")
                    )
                    OutlinedTextField(
                        value = shiftRateText,
                        onValueChange = { shiftRateText = it },
                        label = { Text("Shift Rate (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = upi,
                    onValueChange = { upi = it },
                    label = { Text("UPI ID for Payouts") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(
                            name.trim(),
                            role,
                            phone.trim(),
                            address.trim(),
                            dailyRateText.toDoubleOrNull() ?: 0.0,
                            shiftRateText.toDoubleOrNull() ?: 0.0,
                            upi.trim()
                        )
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                modifier = Modifier.testTag("save_crew_button")
            ) {
                Text("Save Freelancer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecordPayoutDialog(
    summary: ExposureSettlementSummary,
    onDismiss: () -> Unit,
    onConfirmPayout: (amount: Double, date: String, mode: String, ref: String, notes: String) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var amountText by remember { mutableStateOf(summary.pendingPayout.toInt().toString()) }
    var dateText by remember { mutableStateOf(dateFormat.format(Date())) }
    var paymentMode by remember { mutableStateOf("UPI") }
    var refNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payout Voucher - ${summary.freelancerName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Role: ${summary.role}  |  Pending: ${IndianCurrencyFormatter.format(summary.pendingPayout)}", fontSize = 12.sp, color = DangerRed)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payout Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth().testTag("payout_amount_input")
                )
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Payment Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UPI", "CASH", "BANK_TRANSFER").forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }
                OutlinedTextField(
                    value = refNumber,
                    onValueChange = { refNumber = it },
                    label = { Text("UTR / Cheque / Txn Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks / Voucher Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirmPayout(amt, dateText, paymentMode, refNumber.trim(), notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_payout_button")
            ) {
                Text("Confirm Payout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
