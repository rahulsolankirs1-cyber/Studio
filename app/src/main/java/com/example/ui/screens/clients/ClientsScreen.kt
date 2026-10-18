package com.example.ui.screens.clients

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.ClientEntity
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.components.StatusBadge
import com.example.ui.components.launchPhoneCall
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: StudioViewModel,
    onSelectBooking: (Long) -> Unit
) {
    val context = LocalContext.current
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedClientForDetail by remember { mutableStateOf<ClientEntity?>(null) }
    var editingClient by remember { mutableStateOf<ClientEntity?>(null) }

    val filteredClients = clients.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.contactNumber.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Customer Directory",
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
            FloatingActionButton(
                onClick = {
                    editingClient = null
                    showAddDialog = true
                },
                containerColor = AmberGold500,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Client")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("clients_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by customer name, phone, address...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("client_search_input")
            )

            if (filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No clients added yet. Tap '+' to create your first client." else "No clients found matching '$searchQuery'.",
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
                    items(filteredClients, key = { it.id }) { client ->
                        val clientBookings = bookings.filter { it.clientId == client.id }
                        val totalBilled = clientBookings.filter { it.status != "CANCELLED" }.sumOf { it.grandTotal }
                        val totalPaid = clientBookings.filter { it.status != "CANCELLED" }.sumOf { it.advancePaid }
                        val balance = (totalBilled - totalPaid).coerceAtLeast(0.0)

                        ClientCardItem(
                            client = client,
                            bookingsCount = clientBookings.size,
                            balance = balance,
                            onCardClick = { selectedClientForDetail = client },
                            onCallClick = { launchPhoneCall(context, client.contactNumber) },
                            onWhatsAppClick = {
                                launchWhatsApp(
                                    context,
                                    client.contactNumber,
                                    "Hello ${client.name}, this is from the studio regarding your event booking."
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Client Dialog
    if (showAddDialog) {
        AddEditClientDialog(
            initialClient = editingClient,
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, altPhone, address, notes ->
                if (editingClient != null) {
                    viewModel.updateClient(
                        editingClient!!.copy(
                            name = name,
                            contactNumber = phone,
                            alternateNumber = altPhone,
                            address = address,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addClient(
                        ClientEntity(
                            name = name,
                            contactNumber = phone,
                            alternateNumber = altPhone,
                            address = address,
                            notes = notes
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }

    // Client Detail & Event History Dialog
    selectedClientForDetail?.let { client ->
        val clientBookings = bookings.filter { it.clientId == client.id }
        ClientDetailHistoryDialog(
            client = client,
            bookings = clientBookings,
            onDismiss = { selectedClientForDetail = null },
            onEdit = {
                editingClient = client
                selectedClientForDetail = null
                showAddDialog = true
            },
            onSelectBooking = { bookingId ->
                selectedClientForDetail = null
                onSelectBooking(bookingId)
            }
        )
    }
}

@Composable
fun ClientCardItem(
    client: ClientEntity,
    bookingsCount: Int,
    balance: Double,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("client_item_${client.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AmberGold500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = client.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = AmberGold500,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = client.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = client.contactNumber,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Balance Badge
                if (balance > 0) {
                    Surface(
                        color = DangerRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Due: ${IndianCurrencyFormatter.format(balance)}",
                            color = DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "All Clear",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (client.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = client.address,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$bookingsCount Events Booked",
                    fontSize = 11.5.sp,
                    color = ApertureCyan,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onCallClick,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "WhatsApp",
                            tint = ApertureCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditClientDialog(
    initialClient: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, altPhone: String, address: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialClient?.name ?: "") }
    var phone by remember { mutableStateOf(initialClient?.contactNumber ?: "") }
    var altPhone by remember { mutableStateOf(initialClient?.alternateNumber ?: "") }
    var address by remember { mutableStateOf(initialClient?.address ?: "") }
    var notes by remember { mutableStateOf(initialClient?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialClient != null) "Edit Customer" else "Add New Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("client_name_field")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Primary Mobile Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("client_phone_field")
                )
                OutlinedTextField(
                    value = altPhone,
                    onValueChange = { altPhone = it },
                    label = { Text("Alternate Number (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("City / Full Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Client Preferences / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name.trim(), phone.trim(), altPhone.trim(), address.trim(), notes.trim())
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                modifier = Modifier.testTag("save_client_button")
            ) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ClientDetailHistoryDialog(
    client: ClientEntity,
    bookings: List<BookingEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onSelectBooking: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(client.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextButton(onClick = onEdit) {
                    Text("Edit", color = AmberGold500)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Contact: ${client.contactNumber}", fontSize = 12.5.sp)
                if (client.alternateNumber.isNotBlank()) {
                    Text("Alternate: ${client.alternateNumber}", fontSize = 12.sp)
                }
                if (client.address.isNotBlank()) {
                    Text("Address: ${client.address}", fontSize = 12.sp)
                }
                if (client.notes.isNotBlank()) {
                    Text("Notes: ${client.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Event Booking History (${bookings.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AmberGold500
                )

                if (bookings.isEmpty()) {
                    Text("No bookings for this client yet.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.height(180.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(bookings) { b ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectBooking(b.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(b.eventType, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        Text(
                                            "Billed: ${IndianCurrencyFormatter.format(b.grandTotal)}  |  Bal: ${IndianCurrencyFormatter.format(b.balanceDue)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    StatusBadge(status = b.status)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
