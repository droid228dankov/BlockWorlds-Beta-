package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = 'local_player' LIMIT 1")
    fun getProfile(): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("UPDATE player_profile SET bloxCoins = bloxCoins + :delta WHERE id = 'local_player'")
    suspend fun addCoins(delta: Int)
}

@Dao
interface CustomWorldDao {
    @Query("SELECT * FROM custom_worlds ORDER BY createdAt DESC")
    fun getAllCustomWorlds(): Flow<List<CustomWorldEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorld(world: CustomWorldEntity): Long

    @Query("DELETE FROM custom_worlds WHERE id = :id")
    suspend fun deleteWorld(id: Long)
}

@Dao
interface ServerHistoryDao {
    @Query("SELECT * FROM server_history ORDER BY lastJoined DESC LIMIT 15")
    fun getRecentServers(): Flow<List<ServerHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordServer(server: ServerHistoryEntity)
}
