package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ExposureFreelancerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExposureDao {
    @Query("SELECT * FROM exposure_freelancers ORDER BY name ASC")
    fun getAllFreelancers(): Flow<List<ExposureFreelancerEntity>>

    @Query("SELECT * FROM exposure_freelancers WHERE id = :id")
    suspend fun getFreelancerById(id: Long): ExposureFreelancerEntity?

    @Query("SELECT * FROM exposure_freelancers WHERE role = :role")
    fun getFreelancersByRole(role: String): Flow<List<ExposureFreelancerEntity>>

    @Query("SELECT * FROM exposure_freelancers ORDER BY id ASC")
    suspend fun getAllFreelancersDirect(): List<ExposureFreelancerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreelancer(freelancer: ExposureFreelancerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreelancers(freelancers: List<ExposureFreelancerEntity>)

    @Update
    suspend fun updateFreelancer(freelancer: ExposureFreelancerEntity)

    @Delete
    suspend fun deleteFreelancer(freelancer: ExposureFreelancerEntity)

    @Query("DELETE FROM exposure_freelancers")
    suspend fun deleteAllFreelancers()
}
