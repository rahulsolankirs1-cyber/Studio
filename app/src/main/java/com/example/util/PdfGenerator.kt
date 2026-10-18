package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.BookingDeliverableEntity
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.BookingSessionEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.StudioProfileEntity
import com.example.domain.model.SessionServiceItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object PdfGenerator {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun generateInvoicePdf(
        context: Context,
        studio: StudioProfileEntity,
        booking: BookingEntity,
        sessions: List<BookingSessionEntity>,
        deliverables: List<BookingDeliverableEntity>,
        invoice: InvoiceEntity
    ): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Paint definitions
        val primaryPaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Dark Navy
            isAntiAlias = true
        }

        val amberPaint = Paint().apply {
            color = Color.rgb(217, 119, 6) // Studio Gold
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val lightGrayPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val strokePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        // --- 1. Top Decorative Brand Bar ---
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, primaryPaint)
        canvas.drawRect(0f, 12f, 180f, 16f, amberPaint)

        // --- 2. Studio Logo in Top Corner ---
        val logoBoxLeft = 36f
        val logoBoxTop = 24f
        val logoBoxSize = 52f
        val logoRect = RectF(logoBoxLeft, logoBoxTop, logoBoxLeft + logoBoxSize, logoBoxTop + logoBoxSize)

        // Draw neat border background for logo
        val logoBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val logoBorderPaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(logoRect, 8f, 8f, logoBgPaint)
        canvas.drawRoundRect(logoRect, 8f, 8f, logoBorderPaint)

        var logoBitmap: Bitmap? = null
        if (studio.logoUri.isNotBlank()) {
            try {
                val logoFile = File(studio.logoUri)
                if (logoFile.exists()) {
                    logoBitmap = BitmapFactory.decodeFile(logoFile.absolutePath)
                } else if (studio.logoUri.startsWith("content://") || studio.logoUri.startsWith("file://")) {
                    val uri = Uri.parse(studio.logoUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        logoBitmap = BitmapFactory.decodeStream(stream)
                    }
                }
            } catch (e: Exception) {
                // fallback to styled emblem
            }
        }

        if (logoBitmap != null) {
            // Draw custom uploaded studio logo neatly fitted inside corner with aspect ratio preservation
            val padding = 4f
            val maxW = logoBoxSize - (padding * 2)
            val maxH = logoBoxSize - (padding * 2)
            val srcW = logoBitmap!!.width.toFloat()
            val srcH = logoBitmap!!.height.toFloat()
            val scale = minOf(maxW / srcW, maxH / srcH)
            val drawW = srcW * scale
            val drawH = srcH * scale
            val drawLeft = logoBoxLeft + padding + (maxW - drawW) / 2f
            val drawTop = logoBoxTop + padding + (maxH - drawH) / 2f
            val fittedRect = RectF(drawLeft, drawTop, drawLeft + drawW, drawTop + drawH)

            val filterPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            canvas.drawBitmap(logoBitmap!!, null, fittedRect, filterPaint)
        } else {
            // High-end default Aperture & Camera Emblem
            val emblemPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                isAntiAlias = true
            }
            canvas.drawCircle(logoRect.centerX(), logoRect.centerY(), 18f, emblemPaint)

            val goldAperturePaint = Paint().apply {
                color = Color.rgb(245, 158, 11)
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawCircle(logoRect.centerX(), logoRect.centerY(), 13f, goldAperturePaint)

            val monogramPaint = Paint().apply {
                color = Color.WHITE
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val initials = if (studio.studioName.length >= 2) studio.studioName.take(2).uppercase() else "TS"
            canvas.drawText(initials, logoRect.centerX(), logoRect.centerY() + 3.5f, monogramPaint)
        }

        // --- 3. Studio Header Text (placed cleanly next to logo) ---
        val headerTextX = logoBoxLeft + logoBoxSize + 12f
        boldTextPaint.textSize = 17f
        boldTextPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText(studio.studioName, headerTextX, 42f, boldTextPaint)

        textPaint.textSize = 9f
        textPaint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Owner: ${studio.ownerName}  |  Contact: ${studio.mobileNumber}", headerTextX, 56f, textPaint)
        canvas.drawText(studio.fullAddress.take(55), headerTextX, 68f, textPaint)
        if (studio.email.isNotBlank()) {
            canvas.drawText(studio.email, headerTextX, 80f, textPaint)
        }

        // Type Badge (Quotation or Invoice) on Top Right
        val isQuote = invoice.isQuotation
        val badgeTitle = if (isQuote) "OFFICIAL QUOTATION" else if (invoice.taxEnabled) "TAX INVOICE" else "INVOICE"
        val badgeBg = if (isQuote) Color.rgb(245, 158, 11) else Color.rgb(2, 132, 199)
        val badgePaint = Paint().apply {
            color = badgeBg
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val badgeRect = RectF(pageWidth - 190f, 24f, pageWidth - 36f, 48f)
        canvas.drawRoundRect(badgeRect, 4f, 4f, badgePaint)

        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(badgeTitle, badgeRect.centerX(), badgeRect.centerY() + 4f, badgeTextPaint)

        boldTextPaint.textSize = 9.5f
        boldTextPaint.color = Color.rgb(51, 65, 85)
        canvas.drawText("No: ${invoice.invoiceNumber}", pageWidth - 190f, 62f, boldTextPaint)

        textPaint.textSize = 8.5f
        canvas.drawText("Date: ${invoice.issueDate}", pageWidth - 190f, 74f, textPaint)
        if (invoice.dueDate.isNotBlank()) {
            canvas.drawText("Due / Valid Date: ${invoice.dueDate}", pageWidth - 190f, 85f, textPaint)
        }

        var y = 96f
        canvas.drawLine(36f, y, pageWidth - 36f, y, strokePaint)
        y += 14f

        // --- 4. Client & Event Info Box ---
        val clientBoxRect = RectF(36f, y, pageWidth - 36f, y + 68f)
        canvas.drawRoundRect(clientBoxRect, 6f, 6f, lightGrayPaint)
        canvas.drawRoundRect(clientBoxRect, 6f, 6f, strokePaint)

        boldTextPaint.textSize = 10f
        boldTextPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText("BILLED TO / CLIENT:", 48f, y + 18f, boldTextPaint)
        canvas.drawText("EVENT DETAILS:", 320f, y + 18f, boldTextPaint)

        textPaint.color = Color.rgb(30, 41, 59)
        textPaint.textSize = 10.5f
        boldTextPaint.textSize = 11f
        canvas.drawText(booking.clientName, 48f, y + 34f, boldTextPaint)
        canvas.drawText("Phone: ${booking.clientContact}", 48f, y + 48f, textPaint)

        canvas.drawText("Type: ${booking.eventType}", 320f, y + 34f, boldTextPaint)
        val sessionCountText = "${sessions.size} Multi-Day Sessions"
        canvas.drawText("Coverage: $sessionCountText", 320f, y + 48f, textPaint)

        y += 82f

        // --- 5. Event Sessions Schedule ---
        if (sessions.isNotEmpty()) {
            boldTextPaint.textSize = 10f
            boldTextPaint.color = Color.rgb(15, 23, 42)
            canvas.drawText("MULTI-DAY EVENT SCHEDULE & COVERAGE BREAKDOWN:", 36f, y, boldTextPaint)
            y += 8f

            val rowHeight = 18f
            canvas.drawRect(36f, y, pageWidth - 36f, y + rowHeight, primaryPaint)

            val headPaint = Paint().apply {
                color = Color.WHITE
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("DATE", 46f, y + 12f, headPaint)
            canvas.drawText("FUNCTION / SESSION", 120f, y + 12f, headPaint)
            canvas.drawText("TIMINGS", 280f, y + 12f, headPaint)
            canvas.drawText("VENUE / LOCATION", 380f, y + 12f, headPaint)

            y += rowHeight
            textPaint.textSize = 8.5f
            for (s in sessions.take(4)) {
                val serviceList = SessionServiceItem.parseList(s.servicesJson)
                val hasServices = serviceList.isNotEmpty()
                val currentCardH = if (hasServices) 28f else 18f

                canvas.drawRect(36f, y, pageWidth - 36f, y + currentCardH, strokePaint)
                canvas.drawText(s.date, 46f, y + 12f, boldTextPaint)
                canvas.drawText(s.sessionTitle.take(24), 120f, y + 12f, textPaint)
                val timing = if (s.startTime.isNotBlank()) "${s.startTime} - ${s.endTime}" else "Full Day"
                canvas.drawText(timing, 280f, y + 12f, textPaint)
                val venue = if (s.venueName.isNotBlank()) s.venueName else s.city
                canvas.drawText(venue.take(24), 380f, y + 12f, textPaint)

                if (hasServices) {
                    val summary = serviceList.joinToString(" • ") { item ->
                        val priceStr = if (item.unitPrice > 0) " @ ₹${item.unitPrice.toInt()}" else ""
                        "${item.name} x${item.quantity}$priceStr"
                    }
                    val subPaint = Paint().apply {
                        color = Color.rgb(2, 132, 199) // Aperture Cyan
                        textSize = 7.5f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    canvas.drawText("Coverage: ${summary.take(80)}", 120f, y + 23f, subPaint)
                }

                y += currentCardH
            }
            y += 14f
        }

        // --- 6. Deliverables & Services Table ---
        boldTextPaint.textSize = 10f
        boldTextPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText("OUTPUT DETAILS & DELIVERABLES (FINAL CLIENT HANDOVER):", 36f, y, boldTextPaint)
        y += 8f

        val tableRowH = 18f
        canvas.drawRect(36f, y, pageWidth - 36f, y + tableRowH, primaryPaint)
        val dHeadPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("#", 46f, y + 12f, dHeadPaint)
        canvas.drawText("OUTPUT DETAILS / DELIVERABLE DESCRIPTION", 70f, y + 12f, dHeadPaint)
        canvas.drawText("QTY", 440f, y + 12f, dHeadPaint)
        canvas.drawText("STATUS", 485f, y + 12f, dHeadPaint)

        y += tableRowH
        textPaint.textSize = 8.5f
        deliverables.forEachIndexed { idx, d ->
            canvas.drawRect(36f, y, pageWidth - 36f, y + tableRowH, strokePaint)
            canvas.drawText("${idx + 1}", 46f, y + 12f, textPaint)
            canvas.drawText(d.title.take(55), 70f, y + 12f, textPaint)
            canvas.drawText("${d.quantity}", 445f, y + 12f, textPaint)
            val st = if (d.isCompleted) "Completed" else "Included"
            canvas.drawText(st, 485f, y + 12f, textPaint)
            y += tableRowH
        }

        y += 14f

        // --- 7. Financial Summary Table & UPI QR Code ---
        val financeBoxWidth = 240f
        val financeBoxLeft = pageWidth - 36f - financeBoxWidth
        val financeTop = y

        // Draw UPI QR Code on Left Side
        val qrSize = 100
        val upiUri = QrCodeGenerator.createUpiUri(
            upiId = studio.upiId,
            payeeName = studio.accountHolderName.ifBlank { studio.studioName },
            amount = if (booking.balanceDue > 0) booking.balanceDue else null,
            transactionNote = "${invoice.invoiceNumber} - ${booking.clientName}"
        )
        val qrBitmap = QrCodeGenerator.generateQrBitmap(upiUri, qrSize)
        canvas.drawBitmap(qrBitmap, 46f, financeTop, null)

        val qrNotePaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 7.5f
            isAntiAlias = true
        }
        canvas.drawText("Scan to Pay via UPI", 46f, financeTop + qrSize + 12f, qrNotePaint)
        canvas.drawText("(GPay, PhonePe, Paytm, BHIM)", 46f, financeTop + qrSize + 22f, qrNotePaint)

        // Financial summary lines on Right Side
        var finY = financeTop
        fun drawFinanceLine(label: String, value: String, isBold: Boolean = false, isHighlight: Boolean = false) {
            val p = if (isBold) boldTextPaint else textPaint
            p.textSize = if (isHighlight) 11f else 9.5f
            if (isHighlight) {
                p.color = Color.rgb(185, 28, 28) // Bold red for balance due
            } else {
                p.color = Color.rgb(30, 41, 59)
            }
            canvas.drawText(label, financeBoxLeft, finY + 12f, p)
            val alignRightPaint = Paint(p).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText(value, pageWidth - 36f, finY + 12f, alignRightPaint)
            finY += 16f
        }

        drawFinanceLine("Subtotal Package Rate:", currencyFormatter.format(booking.totalAmount))
        if (booking.discountAmount > 0) {
            val discLabel = if (booking.discountType == "PERCENTAGE") "Discount (${booking.discountValue}%):" else "Discount (Flat):"
            drawFinanceLine(discLabel, "- ${currencyFormatter.format(booking.discountAmount)}")
        }
        if (booking.taxEnabled) {
            drawFinanceLine("GST / Tax (${booking.taxPercent}%):", "+ ${currencyFormatter.format(booking.taxAmount)}")
        }
        drawFinanceLine("Grand Total:", currencyFormatter.format(booking.grandTotal), isBold = true)
        drawFinanceLine("Advance Paid Received:", currencyFormatter.format(booking.advancePaid))

        // Balance Due highlight banner
        val balRect = RectF(financeBoxLeft - 6f, finY, pageWidth - 36f, finY + 22f)
        val balBgPaint = Paint().apply {
            color = Color.rgb(254, 242, 242) // light red
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(balRect, 4f, 4f, balBgPaint)
        canvas.drawRoundRect(balRect, 4f, 4f, strokePaint)

        val balLabelPaint = Paint().apply {
            color = Color.rgb(153, 27, 27)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("BALANCE DUE:", financeBoxLeft, finY + 15f, balLabelPaint)
        val balValPaint = Paint(balLabelPaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(currencyFormatter.format(booking.balanceDue), pageWidth - 42f, finY + 15f, balValPaint)

        y = kotlin.math.max(financeTop + qrSize + 30f, finY + 32f)

        // --- 8. Bank & Payment Details Box ---
        val bankBoxRect = RectF(36f, y, pageWidth - 36f, y + 42f)
        canvas.drawRoundRect(bankBoxRect, 4f, 4f, lightGrayPaint)
        canvas.drawRoundRect(bankBoxRect, 4f, 4f, strokePaint)

        boldTextPaint.textSize = 8.5f
        boldTextPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText("BANK DETAILS FOR NEFT / RTGS / IMPS:", 46f, y + 14f, boldTextPaint)

        textPaint.textSize = 8f
        val bankLine = "Bank: ${studio.bankName}  |  A/C Holder: ${studio.accountHolderName}  |  A/C: ${studio.accountNumber}  |  IFSC: ${studio.ifscCode}  |  UPI ID: ${studio.upiId}"
        canvas.drawText(bankLine.take(85), 46f, y + 28f, textPaint)

        y += 50f

        // --- 9. Terms & Conditions and Footer Notes ---
        boldTextPaint.textSize = 8.5f
        boldTextPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText("TERMS & CONDITIONS:", 36f, y, boldTextPaint)
        y += 10f

        textPaint.textSize = 7.5f
        textPaint.color = Color.rgb(71, 85, 105)
        val termsLines = studio.defaultTermsAndConditions.split("\n").filter { it.isNotBlank() }
        for (line in termsLines.take(4)) {
            canvas.drawText(line.take(90), 36f, y, textPaint)
            y += 9.5f
        }

        y += 4f
        if (studio.defaultFooterNotes.isNotBlank()) {
            val footerPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(studio.defaultFooterNotes, pageWidth / 2f, y + 10f, footerPaint)
        }

        // Finish Page
        document.finishPage(page)

        // Write to Cache File
        val pdfDir = File(context.cacheDir, "pdfs").apply { if (!exists()) mkdirs() }
        val fileName = "${invoice.invoiceNumber.replace("/", "_")}.pdf"
        val outputFile = File(pdfDir, fileName)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    /**
     * Opens PDF in any installed PDF reader application (Drive PDF Viewer, Adobe, etc.)
     */
    fun openPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Open PDF with"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found on device.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Saves PDF to standard device Downloads folder (Scoped Storage / MediaStore compatible)
     */
    fun downloadPdfToDownloads(context: Context, pdfFile: File): String? {
        val displayName = "TracStudio_${pdfFile.name}"
        val mimeType = "application/pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/TracStudio")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(pdfFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    return "Downloads/TracStudio/$displayName"
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dest = File(downloadsDir, displayName)
                FileInputStream(pdfFile).use { input ->
                    FileOutputStream(dest).use { out ->
                        input.copyTo(out)
                    }
                }
                return dest.absolutePath
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    /**
     * Shares PDF to WhatsApp, Gmail, or system share sheet
     */
    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Invoice / Quotation PDF"))
    }
}
