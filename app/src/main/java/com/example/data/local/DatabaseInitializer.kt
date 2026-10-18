package com.example.data.local

import com.example.data.local.entity.BookingCrewAssignmentEntity
import com.example.data.local.entity.BookingDeliverableEntity
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.BookingEquipmentAllocationEntity
import com.example.data.local.entity.BookingSessionEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.EquipmentEntity
import com.example.data.local.entity.ExposureFreelancerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.PaymentLedgerEntity
import com.example.data.local.entity.StudioProfileEntity
import com.example.domain.model.SessionServiceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun seedSampleDataIfEmpty(database: StudioFlowDatabase) = withContext(Dispatchers.IO) {
        val studioDao = database.studioDao()
        if (studioDao.getStudioProfileDirect() != null) return@withContext

        // 1. Studio Profile
        val defaultProfile = StudioProfileEntity(
            id = 1,
            studioName = "TracStudio Photography & Cinema",
            ownerName = "Vikram Rathore",
            mobileNumber = "+91 98765 43210",
            email = "contact@tracstudio.com",
            fullAddress = "Suite 402, Creative Media Hub, Film City Road, Goregaon East, Mumbai - 400065",
            logoUri = "",
            bankName = "HDFC Bank",
            accountHolderName = "TracStudio Media LLP",
            accountNumber = "50200034892110",
            ifscCode = "HDFC0001234",
            upiId = "tracstudio@hdfcbank",
            upiQrUri = "",
            defaultTermsAndConditions = "1. 50% advance payable at booking confirmation.\n2. 40% mid-event payment on main wedding day.\n3. 10% balance payable prior to final edited video & raw footage handover.\n4. Raw photos will be shared on cloud drive within 7 days of event.\n5. Photo album draft selection must be completed within 30 days of receiving photos.\n6. Cancellation policy: Advance is non-refundable if cancelled less than 30 days before event.",
            defaultFooterNotes = "Thank you for choosing TracStudio. We capture stories that last a lifetime!",
            taxPercentage = 18.0
        )
        studioDao.insertOrUpdateProfile(defaultProfile)

        // 2. Clients
        val clientDao = database.clientDao()
        val c1Id = clientDao.insertClient(
            ClientEntity(
                name = "Rohit & Ananya Sharma",
                contactNumber = "+91 98200 11223",
                alternateNumber = "+91 98200 44556",
                address = "Seawoods Grand Central, Navi Mumbai",
                notes = "High-end 3-day royal wedding. Prefers cinematic tones & retro film aesthetic."
            )
        )
        val c2Id = clientDao.insertClient(
            ClientEntity(
                name = "Priya Kapoor & Siddharth Malhotra",
                contactNumber = "+91 99100 88776",
                alternateNumber = "+91 99100 22334",
                address = "Golf Links, New Delhi",
                notes = "Destination Pre-Wedding shoot at Udaipur palaces."
            )
        )
        val c3Id = clientDao.insertClient(
            ClientEntity(
                name = "TechNext Ventures (Aakash Mittal)",
                contactNumber = "+91 97654 32190",
                alternateNumber = "",
                address = "Bandra Kurla Complex (BKC), Mumbai",
                notes = "Annual Corporate Summit & Gala Dinner coverage."
            )
        )

        // 3. Exposure / Freelancers
        val exposureDao = database.exposureDao()
        val f1Id = exposureDao.insertFreelancer(
            ExposureFreelancerEntity(
                name = "Arjun Verma",
                role = "Lead Cinematographer",
                contactNumber = "+91 98111 22334",
                address = "Andheri West, Mumbai",
                defaultDailyRate = 16000.0,
                defaultShiftRate = 10000.0,
                upiId = "arjunfilm@okaxis"
            )
        )
        val f2Id = exposureDao.insertFreelancer(
            ExposureFreelancerEntity(
                name = "Meera Nair",
                role = "Candid Photographer",
                contactNumber = "+91 98222 33445",
                address = "Bandra West, Mumbai",
                defaultDailyRate = 13000.0,
                defaultShiftRate = 8000.0,
                upiId = "meeraphotos@icici"
            )
        )
        val f3Id = exposureDao.insertFreelancer(
            ExposureFreelancerEntity(
                name = "Kabir Das",
                role = "Drone Pilot (4K/FPV)",
                contactNumber = "+91 98333 44556",
                address = "Thane West",
                defaultDailyRate = 11000.0,
                defaultShiftRate = 7000.0,
                upiId = "kabirdrone@paytm"
            )
        )
        val f4Id = exposureDao.insertFreelancer(
            ExposureFreelancerEntity(
                name = "Sunil Gaikwad",
                role = "Traditional Videographer",
                contactNumber = "+91 98444 55667",
                address = "Dadar, Mumbai",
                defaultDailyRate = 8500.0,
                defaultShiftRate = 5000.0,
                upiId = "sunilvideo@oksbi"
            )
        )
        val f5Id = exposureDao.insertFreelancer(
            ExposureFreelancerEntity(
                name = "Amit Joshi",
                role = "Traditional Photographer",
                contactNumber = "+91 98555 66778",
                address = "Vile Parle, Mumbai",
                defaultDailyRate = 7500.0,
                defaultShiftRate = 4500.0,
                upiId = "amitjphoto@ybl"
            )
        )

        // 4. Equipment Inventory
        val equipmentDao = database.equipmentDao()
        val eq1Id = equipmentDao.insertEquipment(
            EquipmentEntity(
                itemName = "Sony FX3 Cinema Camera",
                category = "Camera Body",
                serialNumber = "FX3-908122",
                purchaseDate = "2024-03-15"
            )
        )
        val eq2Id = equipmentDao.insertEquipment(
            EquipmentEntity(
                itemName = "Sony Alpha 7 IV",
                category = "Camera Body",
                serialNumber = "A74-451299",
                purchaseDate = "2023-11-20"
            )
        )
        val eq3Id = equipmentDao.insertEquipment(
            EquipmentEntity(
                itemName = "Sony FE 24-70mm f/2.8 GM II",
                category = "Prime & Zoom Lens",
                serialNumber = "SEL2470-3321",
                purchaseDate = "2024-01-10"
            )
        )
        val eq4Id = equipmentDao.insertEquipment(
            EquipmentEntity(
                itemName = "DJI Mavic 3 Pro Cine Drone",
                category = "Drone & Aerial",
                serialNumber = "M3P-99120",
                purchaseDate = "2024-06-01"
            )
        )
        val eq5Id = equipmentDao.insertEquipment(
            EquipmentEntity(
                itemName = "DJI RS 3 Pro Gimbal",
                category = "Gimbal & Stabilizer",
                serialNumber = "RS3-7721",
                purchaseDate = "2023-08-14"
            )
        )
        val eq6Id = equipmentDao.insertEquipment(
            EquipmentEntity(
                itemName = "Godox SL200III Video Light Duo",
                category = "Lighting & Strobe",
                serialNumber = "GDX-200-881",
                purchaseDate = "2023-09-05"
            )
        )

        // 5. Booking 1: Rohit & Ananya Sharma Wedding (Confirmed)
        val bookingDao = database.bookingDao()
        val b1Id = bookingDao.insertBooking(
            BookingEntity(
                clientId = c1Id,
                clientName = "Rohit & Ananya Sharma",
                clientContact = "+91 98200 11223",
                eventType = "Wedding (Full Package)",
                status = "CONFIRMED",
                notes = "Grand celebration at Taj Lands End with celebrity guests.",
                totalAmount = 250000.0,
                discountType = "FLAT",
                discountValue = 25000.0,
                discountAmount = 25000.0,
                taxEnabled = true,
                taxPercent = 18.0,
                taxAmount = 40500.0,
                grandTotal = 265500.0,
                advancePaid = 120000.0,
                balanceDue = 145500.0
            )
        )

        // Sessions for Booking 1
        bookingDao.insertSessions(
            listOf(
                BookingSessionEntity(
                    bookingId = b1Id,
                    sessionTitle = "Mandap & Ganesh Sthapna",
                    date = "2026-10-20",
                    startTime = "08:00 AM",
                    endTime = "02:00 PM",
                    venueName = "Mandap Hall, Taj Lands End",
                    city = "Mumbai",
                    servicesJson = SessionServiceItem.serializeList(
                        listOf(
                            SessionServiceItem("Traditional Photo", quantity = 2, unitPrice = 0.0),
                            SessionServiceItem("Traditional Video", quantity = 2, unitPrice = 0.0),
                            SessionServiceItem("Cinematic Video", quantity = 1, unitPrice = 0.0),
                            SessionServiceItem("Candid Photo", quantity = 1, unitPrice = 0.0)
                        )
                    )
                ),
                BookingSessionEntity(
                    bookingId = b1Id,
                    sessionTitle = "Haldi & Sangeet Gala Night",
                    date = "2026-10-21",
                    startTime = "05:00 PM",
                    endTime = "11:30 PM",
                    venueName = "Poolside Lawn, Taj Lands End",
                    city = "Mumbai",
                    servicesJson = SessionServiceItem.serializeList(
                        listOf(
                            SessionServiceItem("Traditional Photo", quantity = 1, unitPrice = 0.0),
                            SessionServiceItem("Traditional Video", quantity = 1, unitPrice = 0.0),
                            SessionServiceItem("Cinematic Video", quantity = 1, unitPrice = 0.0),
                            SessionServiceItem("Candid Photo", quantity = 1, unitPrice = 0.0),
                            SessionServiceItem("Drone 4K Pilot", quantity = 1, unitPrice = 0.0)
                        )
                    )
                ),
                BookingSessionEntity(
                    bookingId = b1Id,
                    sessionTitle = "Main Wedding Pheras & Royal Reception",
                    date = "2026-10-22",
                    startTime = "04:00 PM",
                    endTime = "01:00 AM",
                    venueName = "Seaside Amphitheatre, Taj Lands End",
                    city = "Mumbai",
                    servicesJson = SessionServiceItem.serializeList(
                        listOf(
                            SessionServiceItem("Traditional Photo", quantity = 2, unitPrice = 0.0),
                            SessionServiceItem("Traditional Video", quantity = 2, unitPrice = 0.0),
                            SessionServiceItem("Cinematic Video", quantity = 2, unitPrice = 0.0),
                            SessionServiceItem("Candid Photo", quantity = 2, unitPrice = 0.0),
                            SessionServiceItem("Drone 4K Pilot", quantity = 1, unitPrice = 0.0)
                        )
                    )
                )
            )
        )

        // Deliverables / Output Details for Booking 1
        bookingDao.insertDeliverables(
            listOf(
                BookingDeliverableEntity(
                    bookingId = b1Id,
                    title = "30 photo edited",
                    quantity = 1,
                    isCompleted = false
                ),
                BookingDeliverableEntity(
                    bookingId = b1Id,
                    title = "All photo raw and jpeg in pendrive",
                    quantity = 1,
                    isCompleted = false
                ),
                BookingDeliverableEntity(
                    bookingId = b1Id,
                    title = "Premium Leather Wedding Album (45 Sheets / 350 Photos)",
                    quantity = 1,
                    isCompleted = false
                ),
                BookingDeliverableEntity(
                    bookingId = b1Id,
                    title = "Cinematic Highlight Film (4-6 Mins)",
                    quantity = 1,
                    isCompleted = false
                ),
                BookingDeliverableEntity(
                    bookingId = b1Id,
                    title = "Teaser / Instagram Reels Pack (5 Reels)",
                    quantity = 5,
                    isCompleted = false
                )
            )
        )

        // Crew Assignments for Booking 1
        bookingDao.insertCrewAssignments(
            listOf(
                BookingCrewAssignmentEntity(
                    bookingId = b1Id,
                    freelancerId = f1Id,
                    freelancerName = "Arjun Verma",
                    role = "Lead Cinematographer",
                    date = "2026-11-20",
                    sessionTitle = "Ring Ceremony & Sangeet Night",
                    rateType = "DAILY",
                    agreedRate = 16000.0,
                    isSettled = true
                ),
                BookingCrewAssignmentEntity(
                    bookingId = b1Id,
                    freelancerId = f1Id,
                    freelancerName = "Arjun Verma",
                    role = "Lead Cinematographer",
                    date = "2026-11-22",
                    sessionTitle = "Main Wedding Pheras & Reception",
                    rateType = "DAILY",
                    agreedRate = 16000.0,
                    isSettled = false
                ),
                BookingCrewAssignmentEntity(
                    bookingId = b1Id,
                    freelancerId = f2Id,
                    freelancerName = "Meera Nair",
                    role = "Candid Photographer",
                    date = "2026-11-22",
                    sessionTitle = "Main Wedding Pheras & Reception",
                    rateType = "DAILY",
                    agreedRate = 13000.0,
                    isSettled = false
                ),
                BookingCrewAssignmentEntity(
                    bookingId = b1Id,
                    freelancerId = f3Id,
                    freelancerName = "Kabir Das",
                    role = "Drone Pilot (4K/FPV)",
                    date = "2026-11-22",
                    sessionTitle = "Main Wedding Pheras & Reception",
                    rateType = "DAILY",
                    agreedRate = 11000.0,
                    isSettled = false
                )
            )
        )

        // Equipment Allocations for Booking 1
        bookingDao.insertEquipmentAllocations(
            listOf(
                BookingEquipmentAllocationEntity(
                    bookingId = b1Id,
                    equipmentId = eq1Id,
                    equipmentName = "Sony FX3 Cinema Camera",
                    category = "Camera Body",
                    date = "2026-11-20",
                    sessionTitle = "Ring Ceremony & Sangeet Night"
                ),
                BookingEquipmentAllocationEntity(
                    bookingId = b1Id,
                    equipmentId = eq1Id,
                    equipmentName = "Sony FX3 Cinema Camera",
                    category = "Camera Body",
                    date = "2026-11-22",
                    sessionTitle = "Main Wedding Pheras & Reception"
                ),
                BookingEquipmentAllocationEntity(
                    bookingId = b1Id,
                    equipmentId = eq4Id,
                    equipmentName = "DJI Mavic 3 Pro Cine Drone",
                    category = "Drone & Aerial",
                    date = "2026-11-22",
                    sessionTitle = "Main Wedding Pheras & Reception"
                )
            )
        )

        // 6. Booking 2: Priya Kapoor Pre-Wedding (Quotation stage)
        val b2Id = bookingDao.insertBooking(
            BookingEntity(
                clientId = c2Id,
                clientName = "Priya Kapoor & Siddharth Malhotra",
                clientContact = "+91 99100 88776",
                eventType = "Pre-Wedding Shoot",
                status = "QUOTATION",
                notes = "3-day palace shoot in Udaipur (Fateh Prakash & Ambrai Ghat).",
                totalAmount = 140000.0,
                discountType = "PERCENTAGE",
                discountValue = 10.0,
                discountAmount = 14000.0,
                taxEnabled = false,
                taxPercent = 18.0,
                taxAmount = 0.0,
                grandTotal = 126000.0,
                advancePaid = 0.0,
                balanceDue = 126000.0
            )
        )
        bookingDao.insertSessions(
            listOf(
                BookingSessionEntity(
                    bookingId = b2Id,
                    sessionTitle = "Sunrise Palace Session",
                    date = "2026-12-05",
                    startTime = "06:00 AM",
                    endTime = "11:00 AM",
                    venueName = "City Palace & Lake Pichola",
                    city = "Udaipur"
                ),
                BookingSessionEntity(
                    bookingId = b2Id,
                    sessionTitle = "Sunset Heritage Session",
                    date = "2026-12-06",
                    startTime = "03:30 PM",
                    endTime = "08:00 PM",
                    venueName = "Monsoon Palace & Desert Camp",
                    city = "Udaipur"
                )
            )
        )
        bookingDao.insertDeliverables(
            listOf(
                BookingDeliverableEntity(
                    bookingId = b2Id,
                    title = "Cinematic Pre-Wedding Music Video (3 Mins)",
                    quantity = 1,
                    isCompleted = false
                ),
                BookingDeliverableEntity(
                    bookingId = b2Id,
                    title = "Edited High-Res Photo Lookbook (50 Photos)",
                    quantity = 1,
                    isCompleted = false
                ),
                BookingDeliverableEntity(
                    bookingId = b2Id,
                    title = "Instagram Teaser Reel",
                    quantity = 2,
                    isCompleted = false
                )
            )
        )

        // 7. Payment Ledger & Invoices
        val paymentLedgerDao = database.paymentLedgerDao()
        // Client advance payment
        paymentLedgerDao.insertPayment(
            PaymentLedgerEntity(
                bookingId = b1Id,
                type = "CUSTOMER_INFLOW",
                partyId = c1Id,
                partyName = "Rohit & Ananya Sharma",
                amount = 120000.0,
                paymentDate = "2026-10-10",
                paymentMode = "BANK_TRANSFER",
                paymentStage = "ADVANCE",
                referenceNumber = "HDFC-IMPS-89211",
                notes = "Advance token for booking confirmation."
            )
        )

        // Freelancer advance payout
        paymentLedgerDao.insertPayment(
            PaymentLedgerEntity(
                bookingId = b1Id,
                type = "EXPOSURE_PAYOUT",
                partyId = f1Id,
                partyName = "Arjun Verma",
                amount = 16000.0,
                paymentDate = "2026-10-12",
                paymentMode = "UPI",
                paymentStage = "CREW_SETTLEMENT",
                referenceNumber = "UPI-9012481",
                notes = "Settlement for Sangeet Night shoot."
            )
        )

        // Invoices
        val invoiceDao = database.invoiceDao()
        invoiceDao.insertInvoice(
            InvoiceEntity(
                bookingId = b1Id,
                invoiceNumber = "INV-2026-001",
                isQuotation = false,
                issueDate = "2026-10-10",
                dueDate = "2026-11-22",
                taxEnabled = true,
                taxPercentage = 18.0,
                termsAndConditions = defaultProfile.defaultTermsAndConditions,
                footerNotes = defaultProfile.defaultFooterNotes
            )
        )

        invoiceDao.insertInvoice(
            InvoiceEntity(
                bookingId = b2Id,
                invoiceNumber = "QUO-2026-002",
                isQuotation = true,
                issueDate = "2026-10-15",
                dueDate = "2026-11-01",
                taxEnabled = false,
                taxPercentage = 18.0,
                termsAndConditions = defaultProfile.defaultTermsAndConditions,
                footerNotes = "Quotation valid until Nov 1st, 2026."
            )
        )
    }

    suspend fun reseedSampleData(database: StudioFlowDatabase) = withContext(Dispatchers.IO) {
        database.clearAllTables()
        seedSampleDataIfEmpty(database)
    }
}
