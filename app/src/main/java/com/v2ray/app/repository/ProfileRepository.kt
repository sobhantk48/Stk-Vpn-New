package com.v2ray.app.repository

import com.v2ray.app.data.Profile
import com.v2ray.app.data.AppDatabase
import com.v2ray.app.data.ProfileDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    fun getAllProfilesFlow(): Flow<List<Profile>> = profileDao.getAllProfilesFlow()

    suspend fun getAllProfiles(): List<Profile> = profileDao.getAllProfiles()

    suspend fun getProfile(id: String): Profile? = profileDao.getProfile(id)

    suspend fun insertProfile(profile: Profile) = profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: Profile) = profileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: Profile) = profileDao.deleteProfile(profile)

    suspend fun setSelected(id: String) {
        profileDao.clearSelected()
        profileDao.setSelected(id)
    }

    suspend fun updateCustomSni(id: String, sni: String) {
        profileDao.updateCustomSni(id, sni)
    }

    suspend fun updateFrontingDomain(id: String, domain: String) {
        profileDao.updateFrontingDomain(id, domain)
    }
}
