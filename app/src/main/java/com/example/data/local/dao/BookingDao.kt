package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.BookingCrewAssignmentEntity
import com.example.data.local.entity.BookingDeliverableEntity
import com.example.data.local.entity.BookingEntity
import com.example.data.local.entity.BookingEquipmentAllocationEntity
import com.example.data.local.entity.BookingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE status = :status ORDER BY createdAt DESC")
    fun getBookingsByStatus(status: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE status != 'QUOTATION' ORDER BY createdAt DESC")
    fun getActiveAndPastBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBookingById(id: Long): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingByIdDirect(id: Long): BookingEntity?

    @Query("SELECT * FROM bookings WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getBookingsByClient(clientId: Long): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Delete
    suspend fun deleteBooking(booking: BookingEntity)

    // Sessions
    @Query("SELECT * FROM booking_sessions WHERE bookingId = :bookingId ORDER BY date ASC, startTime ASC")
    fun getSessionsForBooking(bookingId: Long): Flow<List<BookingSessionEntity>>

    @Query("SELECT * FROM booking_sessions WHERE bookingId = :bookingId ORDER BY date ASC, startTime ASC")
    suspend fun getSessionsForBookingDirect(bookingId: Long): List<BookingSessionEntity>

    @Query("SELECT * FROM booking_sessions ORDER BY date ASC")
    fun getAllSessions(): Flow<List<BookingSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: BookingSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<BookingSessionEntity>)

    @Query("DELETE FROM booking_sessions WHERE bookingId = :bookingId")
    suspend fun deleteSessionsForBooking(bookingId: Long)

    @Delete
    suspend fun deleteSession(session: BookingSessionEntity)

    // Deliverables
    @Query("SELECT * FROM booking_deliverables WHERE bookingId = :bookingId ORDER BY id ASC")
    fun getDeliverablesForBooking(bookingId: Long): Flow<List<BookingDeliverableEntity>>

    @Query("SELECT * FROM booking_deliverables WHERE bookingId = :bookingId ORDER BY id ASC")
    suspend fun getDeliverablesForBookingDirect(bookingId: Long): List<BookingDeliverableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliverables(deliverables: List<BookingDeliverableEntity>)

    @Update
    suspend fun updateDeliverable(deliverable: BookingDeliverableEntity)

    @Query("DELETE FROM booking_deliverables WHERE bookingId = :bookingId")
    suspend fun deleteDeliverablesForBooking(bookingId: Long)

    // Crew Assignments
    @Query("SELECT * FROM booking_crew_assignments WHERE bookingId = :bookingId")
    fun getCrewAssignmentsForBooking(bookingId: Long): Flow<List<BookingCrewAssignmentEntity>>

    @Query("SELECT * FROM booking_crew_assignments WHERE bookingId = :bookingId")
    suspend fun getCrewAssignmentsForBookingDirect(bookingId: Long): List<BookingCrewAssignmentEntity>

    @Query("SELECT * FROM booking_crew_assignments WHERE freelancerId = :freelancerId")
    fun getAssignmentsForFreelancer(freelancerId: Long): Flow<List<BookingCrewAssignmentEntity>>

    @Query("SELECT * FROM booking_crew_assignments")
    fun getAllCrewAssignments(): Flow<List<BookingCrewAssignmentEntity>>

    @Query("SELECT * FROM booking_crew_assignments WHERE date = :date AND bookingId != :excludeBookingId")
    suspend fun getCrewAssignmentsOnDate(date: String, excludeBookingId: Long): List<BookingCrewAssignmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrewAssignment(assignment: BookingCrewAssignmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrewAssignments(assignments: List<BookingCrewAssignmentEntity>)

    @Update
    suspend fun updateCrewAssignment(assignment: BookingCrewAssignmentEntity)

    @Query("DELETE FROM booking_crew_assignments WHERE id = :id")
    suspend fun deleteCrewAssignmentById(id: Long)

    @Query("DELETE FROM booking_crew_assignments WHERE bookingId = :bookingId")
    suspend fun deleteCrewAssignmentsForBooking(bookingId: Long)

    // Equipment Allocations
    @Query("SELECT * FROM booking_equipment_allocations WHERE bookingId = :bookingId")
    fun getEquipmentAllocationsForBooking(bookingId: Long): Flow<List<BookingEquipmentAllocationEntity>>

    @Query("SELECT * FROM booking_equipment_allocations WHERE bookingId = :bookingId")
    suspend fun getEquipmentAllocationsForBookingDirect(bookingId: Long): List<BookingEquipmentAllocationEntity>

    @Query("SELECT * FROM booking_equipment_allocations")
    fun getAllEquipmentAllocations(): Flow<List<BookingEquipmentAllocationEntity>>

    @Query("SELECT * FROM booking_equipment_allocations WHERE date = :date AND bookingId != :excludeBookingId")
    suspend fun getEquipmentAllocationsOnDate(date: String, excludeBookingId: Long): List<BookingEquipmentAllocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipmentAllocations(allocations: List<BookingEquipmentAllocationEntity>)

    @Query("DELETE FROM booking_equipment_allocations WHERE id = :id")
    suspend fun deleteEquipmentAllocationById(id: Long)

    @Query("DELETE FROM booking_equipment_allocations WHERE bookingId = :bookingId")
    suspend fun deleteEquipmentAllocationsForBooking(bookingId: Long)

    // Backup & Restore Bulk Helpers
    @Query("SELECT * FROM bookings ORDER BY id ASC")
    suspend fun getAllBookingsDirect(): List<BookingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Query("SELECT * FROM booking_sessions ORDER BY id ASC")
    suspend fun getAllSessionsDirect(): List<BookingSessionEntity>

    @Query("SELECT * FROM booking_deliverables ORDER BY id ASC")
    suspend fun getAllDeliverablesDirect(): List<BookingDeliverableEntity>

    @Query("SELECT * FROM booking_crew_assignments ORDER BY id ASC")
    suspend fun getAllCrewAssignmentsDirect(): List<BookingCrewAssignmentEntity>

    @Query("SELECT * FROM booking_equipment_allocations ORDER BY id ASC")
    suspend fun getAllEquipmentAllocationsDirect(): List<BookingEquipmentAllocationEntity>

    @Query("DELETE FROM bookings")
    suspend fun deleteAllBookings()

    @Query("DELETE FROM booking_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM booking_deliverables")
    suspend fun deleteAllDeliverables()

    @Query("DELETE FROM booking_crew_assignments")
    suspend fun deleteAllCrewAssignments()

    @Query("DELETE FROM booking_equipment_allocations")
    suspend fun deleteAllEquipmentAllocations()
}
