package com.v2ray.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles")
    fun getAllProfilesFlow(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles")
    suspend fun getAllProfiles(): List<Profile>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfile(id: String): Profile?

    @Insert
    suspend fun insertProfile(profile: Profile)

    @Update
    suspend fun updateProfile(profile: Profile)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)

    @Query("DELETE FROM profiles")
    suspend fun deleteAll()

    @Query("UPDATE profiles SET selected = 0")
    suspend fun clearSelected()

    @Query("UPDATE profiles SET selected = 1 WHERE id = :id")
    suspend fun setSelected(id: String)

    @Query("UPDATE profiles SET customSni = :sni WHERE id = :id")
    suspend fun updateCustomSni(id: String, sni: String)

    @Query("UPDATE profiles SET frontingDomain = :domain WHERE id = :id")
    suspend fun updateFrontingDomain(id: String, domain: String)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    suspend fun deleteProfile(profile: Profile) = deleteProfileById(profile.id)
}
