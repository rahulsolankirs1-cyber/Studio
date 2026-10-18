package com.example.domain.model

data class CrewConflict(
    val freelancerId: Long,
    val freelancerName: String,
    val date: String,
    val conflictingBookingId: Long,
    val conflictingEventTitle: String,
    val conflictingSessionTitle: String
)

data class EquipmentConflict(
    val equipmentId: Long,
    val equipmentName: String,
    val date: String,
    val conflictingBookingId: Long,
    val conflictingEventTitle: String,
    val conflictingSessionTitle: String
)

data class FinancialKpi(
    val totalRevenue: Double,
    val totalReceived: Double,
    val customerOutstanding: Double,
    val crewPayoutsSettled: Double,
    val crewPayoutsPending: Double,
    val activeBookingsCount: Int,
    val quotationsCount: Int
)

data class ClientBalanceSummary(
    val clientId: Long,
    val clientName: String,
    val totalBilled: Double,
    val totalPaid: Double,
    val outstandingBalance: Double,
    val activeBookingsCount: Int
)

data class ExposureSettlementSummary(
    val freelancerId: Long,
    val freelancerName: String,
    val role: String,
    val contactNumber: String,
    val totalAgreedRate: Double,
    val totalSettled: Double,
    val pendingPayout: Double,
    val assignmentsCount: Int
)

object StandardPresets {
    val EventTypes = listOf(
        "Wedding (Full Package)",
        "Pre-Wedding Shoot",
        "Reception Gala",
        "Ring Ceremony / Engagement",
        "Haldi & Mehendi",
        "Sangeet & Cocktail Night",
        "Corporate Event",
        "Maternity & Baby Shoot",
        "Fashion & Portfolio",
        "Birthday & Anniversary"
    )

    val ExposureRoles = listOf(
        "Lead Cinematographer",
        "Traditional Videographer",
        "Candid Photographer",
        "Traditional Photographer",
        "Drone Pilot (4K/FPV)",
        "Gimbal Operator",
        "Crane / Jib Operator",
        "Audio / Sound Recordist",
        "Lighting Technician",
        "Album & Teaser Editor"
    )

    val EquipmentCategories = listOf(
        "Camera Body",
        "Prime & Zoom Lens",
        "Lighting & Strobe",
        "Drone & Aerial",
        "Gimbal & Stabilizer",
        "Audio & Microphone",
        "Crane & Jib",
        "Backdrop & Support"
    )

    val DefaultDeliverables = listOf(
        "Cinematic Highlight Film (4-6 Mins)",
        "Full Length Traditional Video (60-90 Mins)",
        "Teaser / Instagram Reels Pack (5 Reels)",
        "Premium Leather Wedding Album (40 Sheets / 300+ Photos)",
        "Parent Photo Albums (2 Mini Copies)",
        "Candid Photos Raw + High-Res Edited (All files in SSD/Drive)",
        "Drone 4K Aerial Highlight Cut",
        "Same Day Edit (SDE) Teaser Video"
    )
}
