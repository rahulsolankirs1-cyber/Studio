package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CrewConflict
import com.example.domain.model.EquipmentConflict
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.StudioNavy800
import com.example.ui.theme.StudioNavy900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.util.QrCodeGenerator
import java.text.NumberFormat
import java.util.Locale

val IndianCurrencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = 0
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "CONFIRMED" -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
        "QUOTATION" -> WarningOrange.copy(alpha = 0.15f) to WarningOrange
        "COMPLETED" -> ApertureCyan.copy(alpha = 0.15f) to ApertureCyan
        "CANCELLED" -> DangerRed.copy(alpha = 0.15f) to DangerRed
        else -> Color.Gray.copy(alpha = 0.15f) to Color.DarkGray
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CrewConflictWarningCard(
    conflict: CrewConflict,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .testTag("crew_conflict_card")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Conflict Alert",
                tint = DangerRed,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CREW SCHEDULE CONFLICT!",
                    color = DangerRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${conflict.freelancerName} is already booked on ${conflict.date} for '${conflict.conflictingEventTitle}' (${conflict.conflictingSessionTitle}).",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun EquipmentConflictWarningCard(
    conflict: EquipmentConflict,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, WarningOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .testTag("equipment_conflict_card")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Equipment Conflict",
                tint = WarningOrange,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "EQUIPMENT OVERLAP ALERT!",
                    color = WarningOrange,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${conflict.equipmentName} is already allocated on ${conflict.date} for '${conflict.conflictingEventTitle}'.",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UpiQrPreviewDialog(
    upiId: String,
    payeeName: String,
    amount: Double? = null,
    onDismiss: () -> Unit
) {
    val upiUri = QrCodeGenerator.createUpiUri(upiId, payeeName, amount)
    val qrBitmap = QrCodeGenerator.generateQrBitmap(upiUri, 320)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan to Pay via UPI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioNavy900
                )
                Text(
                    text = payeeName,
                    fontSize = 13.sp,
                    color = StudioNavy800
                )
                Text(
                    text = "UPI ID: $upiId",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ApertureCyan
                )
                if (amount != null && amount > 0) {
                    Text(
                        text = IndianCurrencyFormatter.format(amount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberGold500,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "UPI QR Code",
                    modifier = Modifier
                        .size(200.dp)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Accepts Google Pay, PhonePe, Paytm, BHIM & all UPI apps",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_qr_button")
                ) {
                    Text("Close", color = StudioNavy900, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun launchPhoneCall(context: Context, phoneNumber: String) {
    val clean = phoneNumber.replace(" ", "").replace("-", "")
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open dialer: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsApp(context: Context, phoneNumber: String, message: String = "") {
    val clean = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
    try {
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$clean&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not available", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun PdfActionDialog(
    pdfFile: java.io.File,
    title: String = "Document Ready",
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pdf_action_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AmberGold500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Document",
                        tint = AmberGold500,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = pdfFile.name,
                    fontSize = 12.5.sp,
                    color = ApertureCyan,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Choose how you'd like to access your quotation or invoice document:",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // 1. Open / View PDF
                Button(
                    onClick = {
                        com.example.util.PdfGenerator.openPdf(context, pdfFile)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ApertureCyan,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("open_pdf_btn")
                ) {
                    Text("👁️  Open & View PDF", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }

                // 2. Download to Device
                Button(
                    onClick = {
                        val path = com.example.util.PdfGenerator.downloadPdfToDownloads(context, pdfFile)
                        if (path != null) {
                            Toast.makeText(context, "Saved to device: $path", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Saved to cache: ${pdfFile.name}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("download_pdf_btn")
                ) {
                    Text("📥  Download to Device", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }

                // 3. Share via WhatsApp / Other Apps
                Button(
                    onClick = {
                        com.example.util.PdfGenerator.sharePdf(context, pdfFile)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold500,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("share_pdf_btn")
                ) {
                    Text("📤  Share via WhatsApp / Apps", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }

                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_pdf_dialog")
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

