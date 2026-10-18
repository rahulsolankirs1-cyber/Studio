package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StudioProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    @Query("SELECT * FROM studio_profile WHERE id = 1 LIMIT 1")
    fun getStudioProfile(): Flow<StudioProfileEntity?>

    @Query("SELECT * FROM studio_profile WHERE id = 1 LIMIT 1")
    suspend fun getStudioProfileDirect(): StudioProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StudioProfileEntity)

    @Update
    suspend fun updateProfile(profile: StudioProfileEntity)

    @Query("DELETE FROM studio_profile")
    suspend fun deleteAllProfiles()
}
