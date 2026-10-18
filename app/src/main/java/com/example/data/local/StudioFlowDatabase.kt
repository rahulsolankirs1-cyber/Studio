package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BookingDao
import com.example.data.local.dao.ClientDao
import com.example.data.local.dao.EquipmentDao
import com.example.data.local.dao.ExposureDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.PaymentLedgerDao
import com.example.data.local.dao.StudioDao
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

@Database(
    entities = [
        StudioProfileEntity::class,
        ClientEntity::class,
        ExposureFreelancerEntity::class,
        EquipmentEntity::class,
        BookingEntity::class,
        BookingSessionEntity::class,
        BookingDeliverableEntity::class,
        BookingCrewAssignmentEntity::class,
        BookingEquipmentAllocationEntity::class,
        PaymentLedgerEntity::class,
        InvoiceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StudioFlowDatabase : RoomDatabase() {
    abstract fun studioDao(): StudioDao
    abstract fun clientDao(): ClientDao
    abstract fun exposureDao(): ExposureDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun bookingDao(): BookingDao
    abstract fun paymentLedgerDao(): PaymentLedgerDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: StudioFlowDatabase? = null

        fun getInstance(context: Context): StudioFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudioFlowDatabase::class.java,
                    "studioflow_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
