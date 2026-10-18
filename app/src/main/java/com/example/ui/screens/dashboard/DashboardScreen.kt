package com.example.ui.screens.dashboard

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.BookingSessionEntity
import com.example.ui.components.IndianCurrencyFormatter
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.AmberGold600
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.StudioNavy800
import com.example.ui.theme.StudioNavy900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StudioViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToNewBooking: (Boolean) -> Unit, // isQuotation
    onNavigateToClients: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onSelectBooking: (Long) -> Unit
) {
    val studio by viewModel.studioProfile.collectAsStateWithLifecycle()
    val kpis by viewModel.financialKpis.collectAsStateWithLifecycle()
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()

    val upcomingSessions = sessions.sortedBy { it.date }.take(8)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = studio?.studioName ?: "TracStudio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${studio?.ownerName ?: "Studio Admin"} • Photography & Cinema ERP",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Studio Settings",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. KPI Cards Grid (2x2)
            item {
                Text(
                    text = "Financial & Operations Overview",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Total Billed",
                        value = IndianCurrencyFormatter.format(kpis.totalRevenue),
                        icon = Icons.Default.MonetizationOn,
                        accentColor = SuccessGreen,
                        subtitle = "Collected: ${IndianCurrencyFormatter.format(kpis.totalReceived)}",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Client Outstanding",
                        value = IndianCurrencyFormatter.format(kpis.customerOutstanding),
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = DangerRed,
                        subtitle = "Pending Inflow",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Crew Payout Pending",
                        value = IndianCurrencyFormatter.format(kpis.crewPayoutsPending),
                        icon = Icons.Default.PendingActions,
                        accentColor = AmberGold500,
                        subtitle = "Settled: ${IndianCurrencyFormatter.format(kpis.crewPayoutsSettled)}",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Active Bookings",
                        value = "${kpis.activeBookingsCount} Events",
                        icon = Icons.Default.CameraAlt,
                        accentColor = ApertureCyan,
                        subtitle = "${kpis.quotationsCount} Quotations Open",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Quick Action Bar
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Add,
                            label = "New Booking",
                            containerColor = AmberGold500,
                            contentColor = Color.Black,
                            onClick = { onNavigateToNewBooking(false) },
                            testTag = "quick_new_booking"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.Description,
                            label = "New Quotation",
                            containerColor = ApertureCyan,
                            contentColor = Color.White,
                            onClick = { onNavigateToNewBooking(true) },
                            testTag = "quick_new_quotation"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Add Client",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            onClick = onNavigateToClients,
                            testTag = "quick_add_client"
                        )
                    }
                    item {
                        QuickActionButton(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Billing & Ledger",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            onClick = onNavigateToBilling,
                            testTag = "quick_billing"
                        )
                    }
                }
            }

            // 3. Upcoming Event Shoot Schedule Timeline
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Shoot Schedule",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${upcomingSessions.size} Sessions",
                        fontSize = 11.5.sp,
                        color = AmberGold500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (upcomingSessions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No upcoming shoot sessions scheduled. Tap 'New Booking' to plan an event.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(upcomingSessions) { session ->
                    val parentBooking = bookings.firstOrNull { it.id == session.bookingId }
                    UpcomingSessionItemCard(
                        session = session,
                        booking = parentBooking,
                        onClick = {
                            if (parentBooking != null) onSelectBooking(parentBooking.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = Modifier.testTag(testTag)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun UpcomingSessionItemCard(
    session: BookingSessionEntity,
    booking: BookingEntity?,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("upcoming_session_${session.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Badge
            Surface(
                color = AmberGold500.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(54.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val parts = session.date.split("-")
                    val day = parts.getOrNull(2) ?: "01"
                    val month = parts.getOrNull(1) ?: "10"
                    Text(
                        text = day,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold600
                    )
                    Text(
                        text = "M-$month",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberGold600
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.sessionTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (booking != null) {
                        StatusBadge(status = booking.status)
                    }
                }

                if (booking != null) {
                    Text(
                        text = "${booking.clientName} • ${booking.eventType}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ApertureCyan
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    val venueText = if (session.venueName.isNotBlank()) session.venueName else session.city
                    Text(
                        text = venueText.ifBlank { "Venue TBA" },
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (session.startTime.isNotBlank()) {
                        Text(
                            text = " • ${session.startTime}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
