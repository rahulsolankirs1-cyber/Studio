package com.example.data.repository

import com.example.data.local.dao.EquipmentDao
import com.example.data.local.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

class EquipmentRepository(private val equipmentDao: EquipmentDao) {
    val allEquipment: Flow<List<EquipmentEntity>> = equipmentDao.getAllEquipment()

    suspend fun getEquipmentById(id: Long): EquipmentEntity? = equipmentDao.getEquipmentById(id)

    fun getEquipmentByCategory(category: String): Flow<List<EquipmentEntity>> =
        equipmentDao.getEquipmentByCategory(category)

    suspend fun addEquipment(item: EquipmentEntity): Long = equipmentDao.insertEquipment(item)

    suspend fun updateEquipment(item: EquipmentEntity) = equipmentDao.updateEquipment(item)

    suspend fun deleteEquipment(item: EquipmentEntity) = equipmentDao.deleteEquipment(item)
}
