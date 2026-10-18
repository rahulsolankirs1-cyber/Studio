package com.example.ui.screens.profile

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.StudioProfileEntity
import com.example.ui.components.UpiQrPreviewDialog
import com.example.ui.theme.AmberGold500
import com.example.ui.theme.ApertureCyan
import com.example.ui.theme.DangerRed
import com.example.ui.viewmodel.StudioViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioProfileScreen(
    viewModel: StudioViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProfile by viewModel.studioProfile.collectAsStateWithLifecycle()

    var studioName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf("") }

    var bankName by remember { mutableStateOf("") }
    var accountHolderName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }

    var defaultTerms by remember { mutableStateOf("") }
    var defaultNotes by remember { mutableStateOf("") }
    var taxPercentage by remember { mutableStateOf("18.0") }

    var showQrPreview by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val destFile = File(context.filesDir, "studio_logo.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                logoUri = destFile.absolutePath
                Toast.makeText(context, "Logo updated! Remember to tap 'Save Profile'.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load logo image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(currentProfile) {
        currentProfile?.let {
            studioName = it.studioName
            ownerName = it.ownerName
            mobileNumber = it.mobileNumber
            email = it.email
            fullAddress = it.fullAddress
            logoUri = it.logoUri
            bankName = it.bankName
            accountHolderName = it.accountHolderName
            accountNumber = it.accountNumber
            ifscCode = it.ifscCode
            upiId = it.upiId
            defaultTerms = it.defaultTermsAndConditions
            defaultNotes = it.defaultFooterNotes
            taxPercentage = it.taxPercentage.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Studio Profile & Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
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
                .testTag("studio_profile_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Studio Details & Official Logo Upload
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. Studio Identity & Corner Logo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AmberGold500
                    )

                    // Official Studio Logo Card Box (Appears on top corner of PDF)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val logoFile = if (logoUri.isNotBlank()) File(logoUri) else null
                            val bitmap = if (logoFile != null && logoFile.exists()) {
                                try { BitmapFactory.decodeFile(logoFile.absolutePath) } catch (e: Exception) { null }
                            } else null

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Studio Logo",
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(2.dp, AmberGold500, RoundedCornerShape(10.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AmberGold500.copy(alpha = 0.15f))
                                        .border(1.5.dp, AmberGold500.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Default Emblem",
                                        tint = AmberGold500,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (bitmap != null) "Custom Studio Logo Set" else "Default Vector Logo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "This logo fits at the top corner of all generated PDF Quotations & Invoices.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ApertureCyan,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("upload_logo_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (bitmap != null) "Change Logo" else "Upload Logo", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (bitmap != null) {
                                        OutlinedButton(
                                            onClick = {
                                                logoUri = ""
                                                Toast.makeText(context, "Logo reset to default emblem.", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = studioName,
                        onValueChange = { studioName = it },
                        label = { Text("Studio Name") },
                        modifier = Modifier.fillMaxWidth().testTag("studio_name_input")
                    )
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner / Lead Photographer Name") },
                        modifier = Modifier.fillMaxWidth().testTag("owner_name_input")
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            label = { Text("Mobile Number") },
                            modifier = Modifier.weight(1f).testTag("mobile_input")
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Official Email") },
                            modifier = Modifier.weight(1f).testTag("email_input")
                        )
                    }
                    OutlinedTextField(
                        value = fullAddress,
                        onValueChange = { fullAddress = it },
                        label = { Text("Studio Full Address") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("address_input")
                    )
                }
            }

            // Section 2: Banking & UPI Details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "2. Banking & Payment Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ApertureCyan
                        )
                        if (upiId.isNotBlank()) {
                            OutlinedButton(
                                onClick = { showQrPreview = true },
                                modifier = Modifier.testTag("preview_qr_button")
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview QR", fontSize = 11.sp)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name (e.g. HDFC Bank, ICICI)") },
                        modifier = Modifier.fillMaxWidth().testTag("bank_name_input")
                    )
                    OutlinedTextField(
                        value = accountHolderName,
                        onValueChange = { accountHolderName = it },
                        label = { Text("Account Holder / Firm Name") },
                        modifier = Modifier.fillMaxWidth().testTag("holder_name_input")
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it },
                            label = { Text("Account Number") },
                            modifier = Modifier.weight(1f).testTag("account_number_input")
                        )
                        OutlinedTextField(
                            value = ifscCode,
                            onValueChange = { ifscCode = it },
                            label = { Text("IFSC Code") },
                            modifier = Modifier.weight(1f).testTag("ifsc_input")
                        )
                    }
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID (e.g. studio@okaxis / studio@paytm)") },
                        supportingText = { Text("Used for auto-generating dynamic payment QR codes on invoices") },
                        modifier = Modifier.fillMaxWidth().testTag("upi_id_input")
                    )
                }
            }

            // Section 3: Default Invoice & Quotation Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "3. Default Terms & PDF Footer Notes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AmberGold500
                    )
                    OutlinedTextField(
                        value = taxPercentage,
                        onValueChange = { taxPercentage = it },
                        label = { Text("Default GST / Tax Rate (%)") },
                        modifier = Modifier.fillMaxWidth().testTag("tax_percentage_input")
                    )
                    OutlinedTextField(
                        value = defaultTerms,
                        onValueChange = { defaultTerms = it },
                        label = { Text("Standard Terms & Conditions") },
                        minLines = 4,
                        supportingText = { Text("e.g. 50% advance, delivery timelines, cancellation policy") },
                        modifier = Modifier.fillMaxWidth().testTag("terms_input")
                    )
                    OutlinedTextField(
                        value = defaultNotes,
                        onValueChange = { defaultNotes = it },
                        label = { Text("Custom Notes for PDF Footer") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("notes_input")
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    val updated = StudioProfileEntity(
                        id = 1,
                        studioName = studioName.trim().ifBlank { "TracStudio" },
                        ownerName = ownerName.trim(),
                        mobileNumber = mobileNumber.trim(),
                        email = email.trim(),
                        fullAddress = fullAddress.trim(),
                        logoUri = logoUri.trim(),
                        bankName = bankName.trim(),
                        accountHolderName = accountHolderName.trim(),
                        accountNumber = accountNumber.trim(),
                        ifscCode = ifscCode.trim(),
                        upiId = upiId.trim(),
                        defaultTermsAndConditions = defaultTerms.trim(),
                        defaultFooterNotes = defaultNotes.trim(),
                        taxPercentage = taxPercentage.toDoubleOrNull() ?: 18.0
                    )
                    viewModel.updateStudioProfile(updated)
                    Toast.makeText(context, "Studio profile & settings updated successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold500,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile & Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }

    if (showQrPreview && upiId.isNotBlank()) {
        UpiQrPreviewDialog(
            upiId = upiId,
            payeeName = accountHolderName.ifBlank { studioName },
            onDismiss = { showQrPreview = false }
        )
    }
}
