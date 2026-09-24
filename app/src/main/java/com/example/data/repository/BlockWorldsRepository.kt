package com.example.data.repository

import com.example.data.db.CustomWorldDao
import com.example.data.db.CustomWorldEntity
import com.example.data.db.PlayerProfileDao
import com.example.data.db.PlayerProfileEntity
import com.example.data.db.ServerHistoryDao
import com.example.data.db.ServerHistoryEntity
import kotlinx.coroutines.flow.Flow

class BlockWorldsRepository(
    private val profileDao: PlayerProfileDao,
    private val customWorldDao: CustomWorldDao,
    private val serverHistoryDao: ServerHistoryDao
) {
    val profile: Flow<PlayerProfileEntity?> = profileDao.getProfile()
    val customWorlds: Flow<List<CustomWorldEntity>> = customWorldDao.getAllCustomWorlds()
    val serverHistory: Flow<List<ServerHistoryEntity>> = serverHistoryDao.getRecentServers()

    suspend fun saveProfile(profile: PlayerProfileEntity) {
        profileDao.upsertProfile(profile)
    }

    suspend fun addCoins(coins: Int) {
        profileDao.addCoins(coins)
    }

    suspend fun createCustomWorld(world: CustomWorldEntity): Long {
        return customWorldDao.insertWorld(world)
    }

    suspend fun deleteCustomWorld(id: Long) {
        customWorldDao.deleteWorld(id)
    }

    suspend fun logServerJoin(roomCode: String, name: String, mode: String, platform: String) {
        serverHistoryDao.recordServer(
            ServerHistoryEntity(
                roomCode = roomCode,
                serverName = name,
                gameMode = mode,
                hostPlatform = platform,
                lastJoined = System.currentTimeMillis()
            )
        )
    }
}
