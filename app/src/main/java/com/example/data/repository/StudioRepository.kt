package com.example.data.repository

import com.example.data.local.dao.StudioDao
import com.example.data.local.entity.StudioProfileEntity
import kotlinx.coroutines.flow.Flow

class StudioRepository(private val studioDao: StudioDao) {
    val studioProfile: Flow<StudioProfileEntity?> = studioDao.getStudioProfile()

    suspend fun getProfileDirect(): StudioProfileEntity? = studioDao.getStudioProfileDirect()

    suspend fun updateProfile(profile: StudioProfileEntity) {
        studioDao.insertOrUpdateProfile(profile)
    }
}
