package com.example.ui.screens.billing

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.PaymentLedgerEntity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: StudioViewModel,
    onNavigateToNewQuotation: () -> Unit,
    onSelectBooking: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()

    val quotations = allBookings.filter { it.status == "QUOTATION" }
    val confirmedBookings = allBookings.filter { it.status != "QUOTATION" }
    val customerPayments = allPayments.filter { it.type == "CUSTOMER_INFLOW" }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activePdfFile by remember { mutableStateOf<File?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quotations, Invoices & Billing",
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
                    onClick = onNavigateToNewQuotation,
                    containerColor = ApertureCyan,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("new_quotation_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Quotation")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("billing_screen")
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Quotations (${quotations.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_quotations")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Tax Invoices (${confirmedBookings.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_invoices")
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Customer Receipts", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_receipts")
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // Quotations Tab
                    if (quotations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No open quotations. Tap '+' to create a new formal quotation.",
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
                            items(quotations, key = { it.id }) { quote ->
                                val matchingInvoice = allInvoices.firstOrNull { it.bookingId == quote.id }
                                QuotationCardItem(
                                    booking = quote,
                                    invoice = matchingInvoice,
                                    onClick = { onSelectBooking(quote.id) },
                                    onConvert = {
                                        viewModel.convertQuotationToBooking(quote.id) {
                                            Toast.makeText(context, "Quotation converted to Final Booking & Invoice!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onPdfExport = {
                                        coroutineScope.launch {
                                            Toast.makeText(context, "Preparing Quotation PDF...", Toast.LENGTH_SHORT).show()
                                            viewModel.generateAndSharePdf(quote.id) { pdf ->
                                                activePdfFile = pdf
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Tax & Standard Invoices Tab
                    if (confirmedBookings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No confirmed invoices yet.",
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
                            items(confirmedBookings, key = { it.id }) { b ->
                                val invoice = allInvoices.firstOrNull { it.bookingId == b.id }
                                InvoiceCardItem(
                                    booking = b,
                                    invoice = invoice,
                                    onClick = { onSelectBooking(b.id) },
                                    onPdfExport = {
                                        coroutineScope.launch {
                                            Toast.makeText(context, "Preparing Invoice PDF...", Toast.LENGTH_SHORT).show()
                                            viewModel.generateAndSharePdf(b.id) { pdf ->
                                                activePdfFile = pdf
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Customer Payment Receipts Tab
                    if (customerPayments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No customer payments recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(customerPayments) { p ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(p.partyName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                "${p.paymentDate} • Stage: ${p.paymentStage} • Mode: ${p.paymentMode}",
                                                fontSize = 11.5.sp,
                                                color = ApertureCyan
                                            )
                                            if (p.referenceNumber.isNotBlank()) {
                                                Text("Txn/Ref: ${p.referenceNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Text(
                                            text = IndianCurrencyFormatter.format(p.amount),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = SuccessGreen
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

    activePdfFile?.let { file ->
        PdfActionDialog(
            pdfFile = file,
            title = if (selectedTabIndex == 0) "Quotation Document Ready" else "Invoice Document Ready",
            onDismiss = { activePdfFile = null }
        )
    }
}

@Composable
fun QuotationCardItem(
    booking: BookingEntity,
    invoice: InvoiceEntity?,
    onClick: () -> Unit,
    onConvert: () -> Unit,
    onPdfExport: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("quotation_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = invoice?.invoiceNumber ?: "QUO-${booking.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = AmberGold500
                    )
                    Text(
                        text = booking.clientName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusBadge(status = "QUOTATION")
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Event: ${booking.eventType}",
                fontSize = 12.sp,
                color = ApertureCyan
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Proposed Rate:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = IndianCurrencyFormatter.format(booking.grandTotal),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onPdfExport,
                        colors = ButtonDefaults.buttonColors(containerColor = ApertureCyan, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_quote_pdf_${booking.id}")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onConvert,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold500, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("convert_quote_btn_${booking.id}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1-Click Book", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCardItem(
    booking: BookingEntity,
    invoice: InvoiceEntity?,
    onClick: () -> Unit,
    onPdfExport: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("invoice_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = invoice?.invoiceNumber ?: "INV-${booking.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = ApertureCyan
                    )
                    Text(
                        text = booking.clientName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${booking.eventType} • Date: ${invoice?.issueDate ?: "2026-10-18"}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total: ${IndianCurrencyFormatter.format(booking.grandTotal)}  |  Paid: ${IndianCurrencyFormatter.format(booking.advancePaid)}", fontSize = 11.5.sp)
                    if (booking.balanceDue > 0) {
                        Text(
                            text = "Due: ${IndianCurrencyFormatter.format(booking.balanceDue)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    } else {
                        Text("Fully Paid", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }

                Button(
                    onClick = onPdfExport,
                    colors = ButtonDefaults.buttonColors(containerColor = ApertureCyan, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("share_invoice_pdf_${booking.id}")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
