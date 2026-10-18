package com.example.data.repository

import com.example.data.local.dao.ExposureDao
import com.example.data.local.entity.ExposureFreelancerEntity
import kotlinx.coroutines.flow.Flow

class ExposureRepository(private val exposureDao: ExposureDao) {
    val allFreelancers: Flow<List<ExposureFreelancerEntity>> = exposureDao.getAllFreelancers()

    suspend fun getFreelancerById(id: Long): ExposureFreelancerEntity? = exposureDao.getFreelancerById(id)

    fun getFreelancersByRole(role: String): Flow<List<ExposureFreelancerEntity>> =
        exposureDao.getFreelancersByRole(role)

    suspend fun addFreelancer(freelancer: ExposureFreelancerEntity): Long =
        exposureDao.insertFreelancer(freelancer)

    suspend fun updateFreelancer(freelancer: ExposureFreelancerEntity) =
        exposureDao.updateFreelancer(freelancer)

    suspend fun deleteFreelancer(freelancer: ExposureFreelancerEntity) =
        exposureDao.deleteFreelancer(freelancer)
}
