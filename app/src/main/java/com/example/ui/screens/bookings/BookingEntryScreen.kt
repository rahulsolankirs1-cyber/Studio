package com.example.ui.screens.bookings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import com.example.domain.model.SessionServiceItem
import com.example.util.MasterInventoryItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.ExposureFreelancerEntity
import com.example.domain.model.CrewConflict
import com.example.domain.model.EquipmentConflict
import com.example.domain.model.StandardPresets
import com.example.ui.components.CrewConflictWarningCard
import com.example.ui.components.EquipmentConflictWarningCard
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.screens.clients.AddEditClientDialog
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.StudioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingEntryScreen(
    viewModel: StudioViewModel,
    initialIsQuotation: Boolean,
    onBack: () -> Unit,
    onBookingCreated: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val freelancers by viewModel.allFreelancers.collectAsStateWithLifecycle()
    val equipmentList by viewModel.allEquipment.collectAsStateWithLifecycle()
    val masterInventory by viewModel.masterInventory.collectAsStateWithLifecycle()
    val outputDeliverableInventory = masterInventory.filter { it.category == "OUTPUT_DELIVERABLE" }
    val serviceInventory = masterInventory.filter { it.category == "EVENT_SERVICE" }

    var isQuotationMode by remember { mutableStateOf(initialIsQuotation) }

    // Client
    var selectedClient by remember { mutableStateOf<ClientEntity?>(clients.firstOrNull()) }
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var showQuickAddClientDialog by remember { mutableStateOf(false) }

    // Event Info
    var eventType by remember { mutableStateOf(StandardPresets.EventTypes.first()) }
    var eventTypeExpanded by remember { mutableStateOf(false) }
    var bookingNotes by remember { mutableStateOf("") }

    // Sessions (Multi-Day with Mandap & Coverage services default)
    val initialDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 86400000L * 2))
    val defaultInitialServices = listOf(
        SessionServiceItem("Traditional Photo", quantity = 2, unitPrice = 0.0),
        SessionServiceItem("Traditional Video", quantity = 2, unitPrice = 0.0),
        SessionServiceItem("Cinematic Video", quantity = 1, unitPrice = 0.0),
        SessionServiceItem("Candid Photo", quantity = 1, unitPrice = 0.0)
    )
    val sessions = remember {
        mutableStateListOf(
            BookingSessionEntity(
                bookingId = 0,
                sessionTitle = "Mandap",
                date = initialDate,
                startTime = "08:00 AM",
                endTime = "02:00 PM",
                venueName = "",
                city = "",
                servicesJson = SessionServiceItem.serializeList(defaultInitialServices)
            )
        )
    }

    // Deliverables / Output Details (with 30 photo edited, pendrive raw/jpeg default)
    val deliverables = remember {
        mutableStateListOf(
            BookingDeliverableEntity(bookingId = 0, title = "30 photo edited", quantity = 1, isCompleted = false),
            BookingDeliverableEntity(bookingId = 0, title = "All photo raw and jpeg in pendrive", quantity = 1, isCompleted = false),
            BookingDeliverableEntity(bookingId = 0, title = "Premium Wedding Photobook Album (40 Sheets / 300+ Photos)", quantity = 1, isCompleted = false),
            BookingDeliverableEntity(bookingId = 0, title = "Cinematic Teaser Film (1-3 Mins 4K)", quantity = 1, isCompleted = false)
        )
    }
    var newDeliverableText by remember { mutableStateOf("") }

    // Crew Assignments
    val crewAssignments = remember { mutableStateListOf<BookingCrewAssignmentEntity>() }
    val crewConflicts = remember { mutableStateListOf<CrewConflict>() }

    // Equipment Allocations
    val equipmentAllocations = remember { mutableStateListOf<BookingEquipmentAllocationEntity>() }
    val equipmentConflicts = remember { mutableStateListOf<EquipmentConflict>() }

    // Financial Calculation
    var totalAmountText by remember { mutableStateOf("150000") }
    var discountType by remember { mutableStateOf("FLAT") } // FLAT or PERCENTAGE
    var discountValueText by remember { mutableStateOf("10000") }
    var taxEnabled by remember { mutableStateOf(false) }
    var taxPercentText by remember { mutableStateOf("18.0") }
    var advancePaidText by remember { mutableStateOf(if (isQuotationMode) "0" else "40000") }

    // Computed financial numbers
    val totalAmount = totalAmountText.toDoubleOrNull() ?: 0.0
    val discountVal = discountValueText.toDoubleOrNull() ?: 0.0
    val discountAmount = if (discountType == "PERCENTAGE") {
        (totalAmount * (discountVal / 100.0)).coerceAtLeast(0.0)
    } else {
        discountVal.coerceAtMost(totalAmount).coerceAtLeast(0.0)
    }
    val taxableBase = (totalAmount - discountAmount).coerceAtLeast(0.0)
    val taxRate = taxPercentText.toDoubleOrNull() ?: 18.0
    val taxAmount = if (taxEnabled) (taxableBase * (taxRate / 100.0)) else 0.0
    val grandTotal = taxableBase + taxAmount
    val advancePaid = (advancePaidText.toDoubleOrNull() ?: 0.0).coerceAtMost(grandTotal)
    val balanceDue = (grandTotal - advancePaid).coerceAtLeast(0.0)

    // Dialogs for Adding Session, Crew, Gear
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var showAssignCrewDialog by remember { mutableStateOf(false) }
    var showAllocateGearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isQuotationMode) "New Formal Quotation" else "New Event Booking",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("booking_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = if (isQuotationMode) "Quotation" else "Booking",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isQuotationMode) ApertureCyan else AmberGold500
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = !isQuotationMode,
                            onCheckedChange = { isQuotationMode = !it }
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
                .testTag("booking_entry_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Client Selection Card
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
                        Text("1. Client Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                        TextButton(
                            onClick = { showQuickAddClientDialog = true },
                            modifier = Modifier.testTag("quick_new_client_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Client", fontSize = 11.5.sp)
                        }
                    }

                    if (clients.isEmpty()) {
                        Button(
                            onClick = { showQuickAddClientDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black)
                        ) {
                            Text("Add First Client")
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = clientDropdownExpanded,
                            onExpandedChange = { clientDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedClient?.name ?: "Select Client",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Customer *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable).testTag("client_picker")
                            )
                            ExposedDropdownMenu(
                                expanded = clientDropdownExpanded,
                                onDismissRequest = { clientDropdownExpanded = false }
                            ) {
                                clients.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.name} (${c.contactNumber})") },
                                        onClick = {
                                            selectedClient = c
                                            clientDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Event Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = eventTypeExpanded,
                        onExpandedChange = { eventTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = eventType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Event Category / Package *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventTypeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable).testTag("event_type_picker")
                        )
                        ExposedDropdownMenu(
                            expanded = eventTypeExpanded,
                            onDismissRequest = { eventTypeExpanded = false }
                        ) {
                            StandardPresets.EventTypes.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        eventType = t
                                        eventTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = bookingNotes,
                        onValueChange = { bookingNotes = it },
                        label = { Text("Event Theme / Special Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2. Multi-Day Event Schedule & Coverage Breakdown (તારીખ મુજબ ઇવેન્ટ શિડ્યુલ)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("2. Multi-Day Event Schedule & Coverage", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                            Text("Manage multi-day functions with photographers & cinematographers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    sessions.forEachIndexed { sIndex, s ->
                        val serviceList = remember(s.servicesJson) {
                            SessionServiceItem.parseList(s.servicesJson).toMutableList()
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Session Header: Date & Function Title
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = AmberGold500,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Day ${sIndex + 1}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = s.sessionTitle.ifBlank { "Function ${sIndex + 1}" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (sessions.size > 1) {
                                        IconButton(onClick = { sessions.removeAt(sIndex) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Day", tint = DangerRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                // Date & Title text inputs
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = s.date,
                                        onValueChange = { newDate ->
                                            sessions[sIndex] = s.copy(date = newDate)
                                        },
                                        label = { Text("Date (YYYY-MM-DD) *") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = s.sessionTitle,
                                        onValueChange = { newTitle ->
                                            sessions[sIndex] = s.copy(sessionTitle = newTitle)
                                        },
                                        label = { Text("Title (e.g. Mandap)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Quick Function Title Presets Chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("Mandap", "Haldi", "Sangeet", "Wedding", "Reception", "Ring Ceremony", "Pre-Wedding")) { fn ->
                                        FilterChip(
                                            selected = s.sessionTitle == fn,
                                            onClick = { sessions[sIndex] = s.copy(sessionTitle = fn) },
                                            label = { Text(fn, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                // Coverage Services within this session
                                Text(
                                    text = "Included Coverage Services & Quantity (આ દિવસ ની સર્વિસ):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ApertureCyan
                                )

                                // Service Items List
                                serviceList.forEachIndexed { srvIndex, srv ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(srv.name, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                                                if (srv.unitPrice > 0) {
                                                    Text(
                                                        "Rate: ₹${srv.unitPrice.toInt()} ea • Subtotal: ₹${(srv.unitPrice * srv.quantity).toInt()}",
                                                        fontSize = 11.sp,
                                                        color = AmberGold500
                                                    )
                                                }
                                            }

                                            // Stepper & Optional Unit Price
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Unit Price input (optional)
                                                OutlinedTextField(
                                                    value = if (srv.unitPrice > 0) srv.unitPrice.toInt().toString() else "",
                                                    onValueChange = { txt ->
                                                        val p = txt.toDoubleOrNull() ?: 0.0
                                                        val updated = serviceList.toMutableList()
                                                        updated[srvIndex] = srv.copy(unitPrice = p)
                                                        sessions[sIndex] = s.copy(servicesJson = SessionServiceItem.serializeList(updated))
                                                    },
                                                    placeholder = { Text("₹ / Qty", fontSize = 10.sp) },
                                                    modifier = Modifier.width(75.dp).height(46.dp),
                                                    singleLine = true
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))

                                                // Stepper [-] Qty [+]
                                                IconButton(
                                                    onClick = {
                                                        val updated = serviceList.toMutableList()
                                                        if (srv.quantity > 1) {
                                                            updated[srvIndex] = srv.copy(quantity = srv.quantity - 1)
                                                        } else {
                                                            updated.removeAt(srvIndex)
                                                        }
                                                        sessions[sIndex] = s.copy(servicesJson = SessionServiceItem.serializeList(updated))
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                                }

                                                Text(
                                                    text = "${srv.quantity}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )

                                                IconButton(
                                                    onClick = {
                                                        val updated = serviceList.toMutableList()
                                                        updated[srvIndex] = srv.copy(quantity = srv.quantity + 1)
                                                        sessions[sIndex] = s.copy(servicesJson = SessionServiceItem.serializeList(updated))
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                                }

                                                IconButton(
                                                    onClick = {
                                                        val updated = serviceList.toMutableList()
                                                        updated.removeAt(srvIndex)
                                                        sessions[sIndex] = s.copy(servicesJson = SessionServiceItem.serializeList(updated))
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // Quick Add Service Chips from Inventory
                                Text("+ Tap service to add / increase Qty:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(serviceInventory) { invService ->
                                        FilterChip(
                                            selected = false,
                                            onClick = {
                                                val updated = serviceList.toMutableList()
                                                val existingIdx = updated.indexOfFirst { it.name.equals(invService.title, ignoreCase = true) }
                                                if (existingIdx >= 0) {
                                                    val existing = updated[existingIdx]
                                                    updated[existingIdx] = existing.copy(quantity = existing.quantity + 1)
                                                } else {
                                                    updated.add(
                                                        SessionServiceItem(
                                                            name = invService.title,
                                                            quantity = 1,
                                                            unitPrice = 0.0
                                                        )
                                                    )
                                                }
                                                sessions[sIndex] = s.copy(servicesJson = SessionServiceItem.serializeList(updated))
                                            },
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(invService.title, fontSize = 11.sp)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Multi-Day Action Buttons: + Add Next Date Schedule
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // Add next day automatically!
                                val lastDateStr = sessions.lastOrNull()?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val nextDate = try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val parsed = sdf.parse(lastDateStr) ?: Date()
                                    sdf.format(Date(parsed.time + 86400000L))
                                } catch (e: Exception) {
                                    lastDateStr
                                }

                                val nextTitleSuggestions = listOf("Mandap", "Haldi", "Sangeet", "Wedding", "Reception")
                                val existingTitles = sessions.map { it.sessionTitle }
                                val nextTitle = nextTitleSuggestions.firstOrNull { it !in existingTitles } ?: "Event Day ${sessions.size + 1}"

                                val defaultServicesForNext = listOf(
                                    SessionServiceItem("Traditional Photo", quantity = 2, unitPrice = 0.0),
                                    SessionServiceItem("Traditional Video", quantity = 2, unitPrice = 0.0),
                                    SessionServiceItem("Cinematic Video", quantity = 1, unitPrice = 0.0),
                                    SessionServiceItem("Candid Photo", quantity = 1, unitPrice = 0.0)
                                )

                                sessions.add(
                                    BookingSessionEntity(
                                        bookingId = 0,
                                        sessionTitle = nextTitle,
                                        date = nextDate,
                                        startTime = "09:00 AM",
                                        endTime = "10:00 PM",
                                        servicesJson = SessionServiceItem.serializeList(defaultServicesForNext)
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("add_next_date_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Next Date Schedule (+Day)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showAddSessionDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("add_custom_session_button")
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom Session", fontSize = 11.5.sp)
                        }
                    }
                }
            }

            // 3. Output Details & Final Handover (આઉટપુટ ડિટેલ્સ)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Output Details & Deliverables (આઉટપુટ ડિટેલ્સ)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                    Text("Final deliverables handed over to party. Tap '+' on inventory to add multiple copies:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Master Inventory Quick Picker (Tap + to add / multiple)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(outputDeliverableInventory) { invItem ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val existingIdx = deliverables.indexOfFirst { it.title.equals(invItem.title, ignoreCase = true) }
                                    if (existingIdx >= 0) {
                                        val existing = deliverables[existingIdx]
                                        deliverables[existingIdx] = existing.copy(quantity = existing.quantity + 1)
                                    } else {
                                        deliverables.add(
                                            BookingDeliverableEntity(
                                                bookingId = 0,
                                                title = invItem.title,
                                                quantity = invItem.defaultQty,
                                                isCompleted = false
                                            )
                                        )
                                    }
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(invItem.title, fontSize = 11.sp)
                                    }
                                }
                            )
                        }
                    }

                    // Selected Deliverables List with Stepper controls
                    deliverables.forEachIndexed { index, d ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(d.title, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                    Text("Included in client handover", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Stepper [-] Qty [+]
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (d.quantity > 1) {
                                                deliverables[index] = d.copy(quantity = d.quantity - 1)
                                            } else {
                                                deliverables.removeAt(index)
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                    }

                                    Surface(
                                        color = ApertureCyan.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Qty ${d.quantity}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = ApertureCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            deliverables[index] = d.copy(quantity = d.quantity + 1)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { deliverables.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Add Custom Deliverable input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newDeliverableText,
                            onValueChange = { newDeliverableText = it },
                            placeholder = { Text("Custom deliverable (e.g. 50 Retouched Prints, 2TB SSD)...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("custom_deliverable_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newDeliverableText.isNotBlank()) {
                                    deliverables.add(
                                        BookingDeliverableEntity(
                                            bookingId = 0,
                                            title = newDeliverableText.trim(),
                                            quantity = 1,
                                            isCompleted = false
                                        )
                                    )
                                    newDeliverableText = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AmberGold500)
                                .testTag("add_custom_deliverable_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                        }
                    }
                }
            }

            // 4. Exposure / Crew Assignments with Real-Time Conflict Warning
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
                        Text("4. Exposure / Freelancer Crew", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                        Button(
                            onClick = { showAssignCrewDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ApertureCyan, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("assign_crew_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Crew", fontSize = 11.sp)
                        }
                    }

                    // Display Conflict Warnings (Red Alert)
                    crewConflicts.forEach { conflict ->
                        CrewConflictWarningCard(conflict = conflict)
                    }

                    if (crewAssignments.isEmpty()) {
                        Text(
                            text = "No crew assigned yet. Tap 'Assign Crew' to book cameramen / drone pilots.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        crewAssignments.forEachIndexed { index, c ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(c.freelancerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${c.role} • Date: ${c.date}", fontSize = 11.sp, color = ApertureCyan)
                                        Text("Rate: ${IndianCurrencyFormatter.format(c.agreedRate)} (${c.rateType})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AmberGold500)
                                    }
                                    IconButton(
                                        onClick = {
                                            crewAssignments.removeAt(index)
                                            // Recheck conflicts
                                            coroutineScope.launch {
                                                crewConflicts.clear()
                                                for (assign in crewAssignments) {
                                                    val confs = viewModel.checkCrewConflict(assign.freelancerId, assign.date, 0)
                                                    crewConflicts.addAll(confs)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DangerRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Equipment Allocations with Real-Time Conflict Warning
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
                        Text("5. Equipment & Gear Allocation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)
                        Button(
                            onClick = { showAllocateGearDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("allocate_gear_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Allocate Gear", fontSize = 11.sp)
                        }
                    }

                    equipmentConflicts.forEach { conflict ->
                        EquipmentConflictWarningCard(conflict = conflict)
                    }

                    if (equipmentAllocations.isEmpty()) {
                        Text(
                            text = "No gear allocated yet. Allocate cameras/drones to ensure zero scheduling overlaps.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        equipmentAllocations.forEachIndexed { index, eq ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(eq.equipmentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${eq.category} • Date: ${eq.date}", fontSize = 11.sp, color = ApertureCyan)
                                    }
                                    IconButton(
                                        onClick = {
                                            equipmentAllocations.removeAt(index)
                                            coroutineScope.launch {
                                                equipmentConflicts.clear()
                                                for (alloc in equipmentAllocations) {
                                                    val confs = viewModel.checkEquipmentConflict(alloc.equipmentId, alloc.date, 0)
                                                    equipmentConflicts.addAll(confs)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DangerRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Rate Calculation, Discount & Balance Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("6. Pricing, Discounts & Balance Calculation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberGold500)

                    // Helper banner if individual services have unit prices
                    val computedServicesSum = sessions.sumOf { sess ->
                        SessionServiceItem.parseList(sess.servicesJson).sumOf { it.quantity * it.unitPrice }
                    }
                    if (computedServicesSum > 0) {
                        Surface(
                            color = ApertureCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Itemized Services Sum: ₹${computedServicesSum.toInt()}", fontSize = 12.sp, color = ApertureCyan, fontWeight = FontWeight.Bold)
                                    Text("Calculated from individual service prices", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { totalAmountText = computedServicesSum.toInt().toString() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ApertureCyan, contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Apply Sum", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = { totalAmountText = it },
                        label = { Text("Full Package Amount / Base Rate (₹) *") },
                        supportingText = { Text("Enter overall full package amount or tap 'Apply Sum' above") },
                        modifier = Modifier.fillMaxWidth().testTag("base_rate_input")
                    )

                    // Discount Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = discountValueText,
                            onValueChange = { discountValueText = it },
                            label = { Text(if (discountType == "PERCENTAGE") "Discount (%)" else "Discount Amount (₹)") },
                            modifier = Modifier.weight(1f).testTag("discount_value_input")
                        )
                        Row {
                            FilterChip(
                                selected = discountType == "FLAT",
                                onClick = { discountType = "FLAT" },
                                label = { Text("Flat ₹") }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(
                                selected = discountType == "PERCENTAGE",
                                onClick = { discountType = "PERCENTAGE" },
                                label = { Text("%") }
                            )
                        }
                    }

                    // Tax / GST Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable GST / Tax (18%)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Adds official tax calculation to final invoice", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = taxEnabled,
                            onCheckedChange = { taxEnabled = it },
                            modifier = Modifier.testTag("tax_switch")
                        )
                    }

                    // Advance Amount Input
                    OutlinedTextField(
                        value = advancePaidText,
                        onValueChange = { advancePaidText = it },
                        label = { Text("Advance Amount Received (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("advance_received_input")
                    )

                    // Live Summary Box
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = 12.sp)
                                Text(IndianCurrencyFormatter.format(totalAmount), fontSize = 12.sp)
                            }
                            if (discountAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount Applied:", fontSize = 12.sp, color = SuccessGreen)
                                    Text("- ${IndianCurrencyFormatter.format(discountAmount)}", fontSize = 12.sp, color = SuccessGreen)
                                }
                            }
                            if (taxEnabled) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GST ($taxRate%):", fontSize = 12.sp)
                                    Text("+ ${IndianCurrencyFormatter.format(taxAmount)}", fontSize = 12.sp)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total:", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                Text(IndianCurrencyFormatter.format(grandTotal), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Advance Paid:", fontSize = 12.sp)
                                Text(IndianCurrencyFormatter.format(advancePaid), fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("BALANCE DUE:", fontWeight = FontWeight.Bold, color = DangerRed, fontSize = 14.sp)
                                Text(IndianCurrencyFormatter.format(balanceDue), fontWeight = FontWeight.Bold, color = DangerRed, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // 7. Submit Action Button
            Button(
                onClick = {
                    if (selectedClient == null) {
                        Toast.makeText(context, "Please select or create a client", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (sessions.isEmpty()) {
                        Toast.makeText(context, "Please add at least one event session date", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val bookingEntity = BookingEntity(
                        clientId = selectedClient!!.id,
                        clientName = selectedClient!!.name,
                        clientContact = selectedClient!!.contactNumber,
                        eventType = eventType,
                        status = if (isQuotationMode) "QUOTATION" else "CONFIRMED",
                        notes = bookingNotes.trim(),
                        totalAmount = totalAmount,
                        discountType = discountType,
                        discountValue = discountVal,
                        discountAmount = discountAmount,
                        taxEnabled = taxEnabled,
                        taxPercent = taxRate,
                        taxAmount = taxAmount,
                        grandTotal = grandTotal,
                        advancePaid = advancePaid,
                        balanceDue = balanceDue
                    )

                    viewModel.createBooking(
                        booking = bookingEntity,
                        sessions = sessions.toList(),
                        deliverables = deliverables.toList(),
                        crew = crewAssignments.toList(),
                        equipment = equipmentAllocations.toList(),
                        onSuccess = { id ->
                            val msg = if (isQuotationMode) "Quotation generated successfully!" else "Event booked successfully!"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            onBookingCreated(id)
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isQuotationMode) ApertureCyan else AmberGold500,
                    contentColor = if (isQuotationMode) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_booking_button")
            ) {
                Icon(
                    imageVector = if (isQuotationMode) Icons.Default.Description else Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isQuotationMode) "Generate Formal Quotation" else "Confirm Booking & Generate Invoice",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // Quick Add Client Dialog
    if (showQuickAddClientDialog) {
        AddEditClientDialog(
            initialClient = null,
            onDismiss = { showQuickAddClientDialog = false },
            onSave = { name, phone, altPhone, addr, notes ->
                viewModel.addClient(
                    ClientEntity(
                        name = name,
                        contactNumber = phone,
                        alternateNumber = altPhone,
                        address = addr,
                        notes = notes
                    )
                ) { newId ->
                    selectedClient = ClientEntity(
                        id = newId,
                        name = name,
                        contactNumber = phone,
                        alternateNumber = altPhone,
                        address = addr,
                        notes = notes
                    )
                }
                showQuickAddClientDialog = false
            }
        )
    }

    // Add Session Dialog
    if (showAddSessionDialog) {
        AddSessionDialog(
            onDismiss = { showAddSessionDialog = false },
            onAdd = { title, date, start, end, venue, city ->
                val defaultServices = listOf(
                    SessionServiceItem("Traditional Photo", quantity = 2, unitPrice = 0.0),
                    SessionServiceItem("Traditional Video", quantity = 2, unitPrice = 0.0),
                    SessionServiceItem("Cinematic Video", quantity = 1, unitPrice = 0.0),
                    SessionServiceItem("Candid Photo", quantity = 1, unitPrice = 0.0)
                )
                sessions.add(
                    BookingSessionEntity(
                        bookingId = 0,
                        sessionTitle = title,
                        date = date,
                        startTime = start,
                        endTime = end,
                        venueName = venue,
                        city = city,
                        servicesJson = SessionServiceItem.serializeList(defaultServices)
                    )
                )
                showAddSessionDialog = false
            }
        )
    }

    // Assign Crew Dialog with Real-Time Conflict Detection
    if (showAssignCrewDialog) {
        AssignCrewDialog(
            freelancers = freelancers,
            availableSessions = sessions,
            onDismiss = { showAssignCrewDialog = false },
            onAssign = { freelancer, date, sessionTitle, rateType, rate ->
                val newAssignment = BookingCrewAssignmentEntity(
                    bookingId = 0,
                    freelancerId = freelancer.id,
                    freelancerName = freelancer.name,
                    role = freelancer.role,
                    date = date,
                    sessionTitle = sessionTitle,
                    rateType = rateType,
                    agreedRate = rate
                )
                crewAssignments.add(newAssignment)
                showAssignCrewDialog = false

                // Run conflict detection immediately
                coroutineScope.launch {
                    val confs = viewModel.checkCrewConflict(freelancer.id, date, 0)
                    if (confs.isNotEmpty()) {
                        crewConflicts.addAll(confs)
                    }
                }
            }
        )
    }

    // Allocate Equipment Dialog with Conflict Detection
    if (showAllocateGearDialog) {
        AllocateGearDialog(
            equipmentList = equipmentList,
            availableSessions = sessions,
            onDismiss = { showAllocateGearDialog = false },
            onAllocate = { gear, date, sessionTitle ->
                val newAlloc = BookingEquipmentAllocationEntity(
                    bookingId = 0,
                    equipmentId = gear.id,
                    equipmentName = gear.itemName,
                    category = gear.category,
                    date = date,
                    sessionTitle = sessionTitle
                )
                equipmentAllocations.add(newAlloc)
                showAllocateGearDialog = false

                // Run equipment conflict check
                coroutineScope.launch {
                    val confs = viewModel.checkEquipmentConflict(gear.id, date, 0)
                    if (confs.isNotEmpty()) {
                        equipmentConflicts.addAll(confs)
                    }
                }
            }
        )
    }
}

@Composable
fun AddSessionDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, date: String, start: String, end: String, venue: String, city: String) -> Unit
) {
    var title by remember { mutableStateOf("Wedding Pheras") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 86400000L * 7))) }
    var start by remember { mutableStateOf("04:00 PM") }
    var end by remember { mutableStateOf("11:00 PM") }
    var venue by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Multi-Day Event Session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Name (e.g. Haldi, Sangeet, Wedding)") },
                    modifier = Modifier.fillMaxWidth().testTag("session_title_input")
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth().testTag("session_date_input")
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && date.isNotBlank()) {
                        onAdd(title.trim(), date.trim(), start.trim(), end.trim(), venue.trim(), city.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_add_session")
            ) {
                Text("Add Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignCrewDialog(
    freelancers: List<ExposureFreelancerEntity>,
    availableSessions: List<BookingSessionEntity>,
    onDismiss: () -> Unit,
    onAssign: (freelancer: ExposureFreelancerEntity, date: String, sessionTitle: String, rateType: String, rate: Double) -> Unit
) {
    if (freelancers.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Freelancers Registered") },
            text = { Text("Please register exposure artists/cameramen in the Crew Directory first.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var selectedFreelancer by remember { mutableStateOf(freelancers.first()) }
    var freeExpanded by remember { mutableStateOf(false) }

    val defaultSession = availableSessions.firstOrNull()
    var selectedSessionTitle by remember { mutableStateOf(defaultSession?.sessionTitle ?: "") }
    var selectedDate by remember { mutableStateOf(defaultSession?.date ?: "2026-10-20") }
    var sessionExpanded by remember { mutableStateOf(false) }

    var rateType by remember { mutableStateOf("DAILY") } // DAILY, SHIFT, EVENT
    var rateText by remember { mutableStateOf(selectedFreelancer.defaultDailyRate.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Exposure Artist / Crew") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Freelancer Picker
                ExposedDropdownMenuBox(expanded = freeExpanded, onExpandedChange = { freeExpanded = it }) {
                    OutlinedTextField(
                        value = "${selectedFreelancer.name} (${selectedFreelancer.role})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Artist *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable).testTag("select_artist_dropdown")
                    )
                    ExposedDropdownMenu(expanded = freeExpanded, onDismissRequest = { freeExpanded = false }) {
                        freelancers.forEach { f ->
                            DropdownMenuItem(
                                text = { Text("${f.name} • ${f.role}") },
                                onClick = {
                                    selectedFreelancer = f
                                    rateText = if (rateType == "DAILY") f.defaultDailyRate.toInt().toString() else f.defaultShiftRate.toInt().toString()
                                    freeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Session / Date Picker
                if (availableSessions.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = sessionExpanded, onExpandedChange = { sessionExpanded = it }) {
                        OutlinedTextField(
                            value = "$selectedSessionTitle ($selectedDate)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Event Session *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sessionExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = sessionExpanded, onDismissRequest = { sessionExpanded = false }) {
                            availableSessions.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.sessionTitle} - ${s.date}") },
                                    onClick = {
                                        selectedSessionTitle = s.sessionTitle
                                        selectedDate = s.date
                                        sessionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Shoot Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Rate Basis
                Text("Agreed Rate Basis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DAILY", "SHIFT", "EVENT").forEach { t ->
                        FilterChip(
                            selected = rateType == t,
                            onClick = {
                                rateType = t
                                rateText = when (t) {
                                    "DAILY" -> selectedFreelancer.defaultDailyRate.toInt().toString()
                                    "SHIFT" -> selectedFreelancer.defaultShiftRate.toInt().toString()
                                    else -> rateText
                                }
                            },
                            label = { Text(t) }
                        )
                    }
                }

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Agreed Shooting Rate (₹) *") },
                    modifier = Modifier.fillMaxWidth().testTag("crew_agreed_rate_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull() ?: 0.0
                    onAssign(selectedFreelancer, selectedDate, selectedSessionTitle, rateType, rate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_assign_crew_button")
            ) {
                Text("Assign to Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllocateGearDialog(
    equipmentList: List<EquipmentEntity>,
    availableSessions: List<BookingSessionEntity>,
    onDismiss: () -> Unit,
    onAllocate: (gear: EquipmentEntity, date: String, sessionTitle: String) -> Unit
) {
    if (equipmentList.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Equipment In Inventory") },
            text = { Text("Please add cameras, lenses, or lighting in the Equipment Inventory screen first.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var selectedGear by remember { mutableStateOf(equipmentList.first()) }
    var gearExpanded by remember { mutableStateOf(false) }

    val defaultSession = availableSessions.firstOrNull()
    var selectedSessionTitle by remember { mutableStateOf(defaultSession?.sessionTitle ?: "") }
    var selectedDate by remember { mutableStateOf(defaultSession?.date ?: "2026-10-20") }
    var sessionExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allocate Gear to Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = gearExpanded, onExpandedChange = { gearExpanded = it }) {
                    OutlinedTextField(
                        value = "${selectedGear.itemName} (${selectedGear.category})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Gear Item *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gearExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable).testTag("select_gear_dropdown")
                    )
                    ExposedDropdownMenu(expanded = gearExpanded, onDismissRequest = { gearExpanded = false }) {
                        equipmentList.forEach { eq ->
                            DropdownMenuItem(
                                text = { Text("${eq.itemName} • ${eq.category}") },
                                onClick = {
                                    selectedGear = eq
                                    gearExpanded = false
                                }
                            )
                        }
                    }
                }

                if (availableSessions.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = sessionExpanded, onExpandedChange = { sessionExpanded = it }) {
                        OutlinedTextField(
                            value = "$selectedSessionTitle ($selectedDate)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Event Session *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sessionExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = sessionExpanded, onDismissRequest = { sessionExpanded = false }) {
                            availableSessions.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.sessionTitle} - ${s.date}") },
                                    onClick = {
                                        selectedSessionTitle = s.sessionTitle
                                        selectedDate = s.date
                                        sessionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAllocate(selectedGear, selectedDate, selectedSessionTitle) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_allocate_gear_button")
            ) {
                Text("Allocate Gear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
