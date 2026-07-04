package com.v2ray.app.repository

import com.v2ray.app.data.Profile
import java.util.UUID

class ProfileRepository {
    private var profiles: MutableList<Profile> = mutableListOf(
        Profile(
            id = UUID.randomUUID().toString(),
            name = "Sample Server",
            server = "127.0.0.1",
            port = 443,
            uuid = "12345678-1234-1234-1234-123456789abc",
            type = "vless",
            address = "127.0.0.1"
        )
    )

    fun getProfiles(): List<Profile> = profiles

    fun addProfile(profile: Profile) {
        profiles.add(profile)
    }

    fun deleteProfile(id: String) {
        profiles.removeAll { it.id == id }
    }

    fun updateProfile(profile: Profile) {
        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            profiles[index] = profile
        }
    }
}
