package com.marketfinder.core.data.local.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>

    // insert user
    @Upsert
    suspend fun insert(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int)

    @Query("SELECT * FROM users WHERE auth_provider = :provider AND provider_user_id = :providerUserId LIMIT 1")
    suspend fun getByProviderId(provider: String, providerUserId: String): UserEntity?
}