package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.billing.BillingScreen
import com.example.ui.screens.bookings.BookingDetailScreen
import com.example.ui.screens.bookings.BookingEntryScreen
import com.example.ui.screens.clients.ClientsScreen
import com.example.ui.screens.crew.CrewScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.equipment.EquipmentScreen
import com.example.ui.screens.profile.StudioProfileScreen
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.AmberGold600
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.viewmodel.StudioViewModel

sealed class AppScreen {
    data object Dashboard : AppScreen()
    data object Clients : AppScreen()
    data object Crew : AppScreen()
    data object Equipment : AppScreen()
    data object Billing : AppScreen()
    data object Settings : AppScreen()
    data object StudioProfile : AppScreen()
    data class BookingEntry(val isQuotation: Boolean = false) : AppScreen()
    data class BookingDetail(val bookingId: Long) : AppScreen()
}

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StudioManagementApp()
            }
        }
    }
}

@Composable
fun StudioManagementApp(
    viewModel: StudioViewModel = viewModel()
) {
    val backStack = remember { mutableStateListOf<AppScreen>(AppScreen.Dashboard) }
    val currentScreen = backStack.lastOrNull() ?: AppScreen.Dashboard

    val isRootScreen = currentScreen is AppScreen.Dashboard ||
            currentScreen is AppScreen.Clients ||
            currentScreen is AppScreen.Crew ||
            currentScreen is AppScreen.Equipment ||
            currentScreen is AppScreen.Billing

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.Dashboard) {
            backStack.clear()
            backStack.add(AppScreen.Dashboard)
        } else if (isRootScreen && (screen is AppScreen.Clients || screen is AppScreen.Crew || screen is AppScreen.Equipment || screen is AppScreen.Billing)) {
            // Replace root tab
            backStack.clear()
            backStack.add(screen)
        } else {
            backStack.add(screen)
        }
    }

    fun popBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    // Handle Android system back gesture for sub-screens
    if (!isRootScreen) {
        BackHandler { popBack() }
    }

    val navItems = listOf(
        NavItem(AppScreen.Dashboard, "Home", Icons.Default.Dashboard, "nav_dashboard"),
        NavItem(AppScreen.Clients, "Clients", Icons.Default.People, "nav_clients"),
        NavItem(AppScreen.Crew, "Crew", Icons.Default.Group, "nav_crew"),
        NavItem(AppScreen.Equipment, "Gear", Icons.Default.CameraAlt, "nav_equipment"),
        NavItem(AppScreen.Billing, "Billing", Icons.Default.ReceiptLong, "nav_billing")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isRootScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = when (item.screen) {
                            is AppScreen.Dashboard -> currentScreen is AppScreen.Dashboard
                            is AppScreen.Clients -> currentScreen is AppScreen.Clients
                            is AppScreen.Crew -> currentScreen is AppScreen.Crew
                            is AppScreen.Equipment -> currentScreen is AppScreen.Equipment
                            is AppScreen.Billing -> currentScreen is AppScreen.Billing
                            else -> false
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { navigateTo(item.screen) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                indicatorColor = AmberGold500,
                                selectedTextColor = AmberGold600,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenTransition"
        ) { targetScreen ->
            when (targetScreen) {
                is AppScreen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { navigateTo(AppScreen.Settings) },
                        onNavigateToNewBooking = { isQuote -> navigateTo(AppScreen.BookingEntry(isQuote)) },
                        onNavigateToClients = { navigateTo(AppScreen.Clients) },
                        onNavigateToBilling = { navigateTo(AppScreen.Billing) },
                        onSelectBooking = { id -> navigateTo(AppScreen.BookingDetail(id)) }
                    )
                }
                is AppScreen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = { navigateTo(AppScreen.StudioProfile) },
                        onBack = { popBack() }
                    )
                }
                is AppScreen.Clients -> {
                    ClientsScreen(
                        viewModel = viewModel,
                        onSelectBooking = { id -> navigateTo(AppScreen.BookingDetail(id)) }
                    )
                }
                is AppScreen.Crew -> {
                    CrewScreen(viewModel = viewModel)
                }
                is AppScreen.Equipment -> {
                    EquipmentScreen(viewModel = viewModel)
                }
                is AppScreen.Billing -> {
                    BillingScreen(
                        viewModel = viewModel,
                        onNavigateToNewQuotation = { navigateTo(AppScreen.BookingEntry(isQuotation = true)) },
                        onSelectBooking = { id -> navigateTo(AppScreen.BookingDetail(id)) }
                    )
                }
                is AppScreen.StudioProfile -> {
                    StudioProfileScreen(
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )
                }
                is AppScreen.BookingEntry -> {
                    BookingEntryScreen(
                        viewModel = viewModel,
                        initialIsQuotation = targetScreen.isQuotation,
                        onBack = { popBack() },
                        onBookingCreated = { id ->
                            popBack()
                            navigateTo(AppScreen.BookingDetail(id))
                        }
                    )
                }
                is AppScreen.BookingDetail -> {
                    BookingDetailScreen(
                        bookingId = targetScreen.bookingId,
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )
                }
            }
        }
    }
}
