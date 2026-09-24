package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: String = "local_player",
    val username: String = "BlockBuilder_99",
    val bloxCoins: Int = 1500,
    val level: Int = 8,
    val xp: Int = 3400,
    val headItem: String = "Cyber Visor",
    val faceItem: String = "Cool Sunglasses",
    val torsoItem: String = "Cyber Hoodie",
    val pantsItem: String = "Dark Joggers",
    val backItem: String = "Voxel Katana",
    val skinColor: Long = 0xFFFFDBAC,
    val shirtColor: Long = 0xFF2563EB,
    val pantsColor: Long = 0xFF1E293B,
    val obbysCompleted: Int = 12,
    val blocksPlaced: Int = 430,
    val survivalWins: Int = 5
)

@Entity(tableName = "custom_worlds")
data class CustomWorldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val gameMode: String,
    val maxPlayers: Int = 16,
    val seed: Long = System.currentTimeMillis(),
    val blocksJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true
)

@Entity(tableName = "server_history")
data class ServerHistoryEntity(
    @PrimaryKey val roomCode: String,
    val serverName: String,
    val gameMode: String,
    val hostPlatform: String,
    val lastJoined: Long = System.currentTimeMillis()
)
