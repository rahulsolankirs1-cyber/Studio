package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment_inventory ORDER BY itemName ASC")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment_inventory WHERE category = :category ORDER BY itemName ASC")
    fun getEquipmentByCategory(category: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment_inventory WHERE id = :id")
    suspend fun getEquipmentById(id: Long): EquipmentEntity?

    @Query("SELECT * FROM equipment_inventory ORDER BY id ASC")
    suspend fun getAllEquipmentDirect(): List<EquipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(item: EquipmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEquipment(items: List<EquipmentEntity>)

    @Update
    suspend fun updateEquipment(item: EquipmentEntity)

    @Delete
    suspend fun deleteEquipment(item: EquipmentEntity)

    @Query("DELETE FROM equipment_inventory")
    suspend fun deleteAllEquipment()
}
