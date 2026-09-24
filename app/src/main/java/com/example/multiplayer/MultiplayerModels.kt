package com.example.multiplayer

import com.example.model.GameMode
import com.example.model.PlatformType

data class ServerRoom(
    val id: String,
    val name: String,
    val roomCode: String,
    val gameMode: GameMode,
    val hostPlayerName: String,
    val hostPlatform: PlatformType,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val pingMs: Int,
    val region: String, // "US-East", "EU-West", "Asia-Tokyo", "Global"
    val isCrossPlay: Boolean = true,
    val pvpEnabled: Boolean = false,
    val worldSeed: Long = 1337L
)

enum class PacketType {
    HANDSHAKE,
    HEARTBEAT,
    PLAYER_TRANSFORM,
    BLOCK_ACTION,
    CHAT_MESSAGE,
    EMOTE_TRIGGER,
    PLAYER_JOIN,
    PLAYER_LEAVE,
    GAME_EVENT
}

data class NetworkBlockEvent(
    val x: Int,
    val y: Int,
    val z: Int,
    val blockTypeName: String,
    val isPlacement: Boolean,
    val playerId: String,
    val timestamp: Long = System.currentTimeMillis()
)
