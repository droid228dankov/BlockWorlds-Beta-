package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.R

enum class PlatformType(val displayName: String, val icon: String, val tagColor: Long) {
    ANDROID("Mobile (Android)", "📱", 0xFF3DDC84),
    IOS("Mobile (iOS)", "📱", 0xFF007AFF),
    PC("PC (Win/Mac)", "💻", 0xFF00D2FF),
    WEB("Web (Browser)", "🌐", 0xFFFF9900),
    CONSOLE("Console", "🎮", 0xFFA855F7)
}

enum class GameMode(val label: String, val badgeColor: Long) {
    NORMAL_ELEVATOR("The Normal Elevator", 0xFFFF9800),
    OBBY("Obby / Parkour", 0xFFFF5722),
    SANDBOX("Creative Sandbox", 0xFF4CAF50),
    LAVA_SURVIVAL("Lava Escape", 0xFFE91E63),
    BATTLE_ARENA("Battle Arena", 0xFF9C27B0)
}

enum class ElevatorState {
    BOARDING,
    TRANSIT,
    DOORS_OPENING,
    FLOOR_ACTIVE,
    DOORS_CLOSING
}

data class ElevatorFloorInfo(
    val floorNumber: Int,
    val title: String,
    val description: String,
    val dangerLevel: String,
    val durationSeconds: Int,
    val themeColor: Long,
    val musicTrackName: String,
    val hazardType: String = "None"
)

enum class BlockType(
    val title: String,
    val topColor: Long,
    val sideColor: Long,
    val bounce: Float = 0f,
    val isHazard: Boolean = false,
    val isFinish: Boolean = false,
    val isCheckpoint: Boolean = false,
    val isInteractive: Boolean = false,
    val glow: Boolean = false
) {
    GRASS("Grass", 0xFF4CAF50, 0xFF388E3C),
    DIRT("Dirt", 0xFF795548, 0xFF5D4037),
    STONE("Stone", 0xFF9E9E9E, 0xFF757575),
    WOOD("Wood", 0xFF8D6E63, 0xFF6D4C41),
    BRICK("Brick", 0xFFE57373, 0xFFD32F2F),
    NEON_CYAN("Neon Cyan", 0xFF00E5FF, 0xFF00B0FF, glow = true),
    NEON_PINK("Neon Pink", 0xFFFF4081, 0xFFF50057, glow = true),
    NEON_YELLOW("Neon Yellow", 0xFFFFEB3B, 0xFFFDD835, glow = true),
    GOLD("Gold Block", 0xFFFFD700, 0xFFFFC107, glow = true),
    DIAMOND("Diamond Block", 0xFF00E5FF, 0xFF0091EA, glow = true),
    LAVA("Lava Hazard", 0xFFFF3D00, 0xFFDD2C00, isHazard = true, glow = true),
    ICE("Speed Ice", 0xFF80DEEA, 0xFF4DD0E1),
    JUMP_PAD("Super Jump Pad", 0xFFFFEA00, 0xFFFFAB00, bounce = 1.35f, isInteractive = true, glow = true),
    CHECKPOINT("Checkpoint Flag", 0xFF00E676, 0xFF00C853, isCheckpoint = true, glow = true),
    PORTAL("Finish Portal", 0xFFAA00FF, 0xFF7B1FA2, isFinish = true, glow = true),
    TNT("Explosive TNT", 0xFFFF1744, 0xD50000, isInteractive = true)
}

data class VoxelCoord(val x: Int, val y: Int, val z: Int)

data class VoxelBlock(
    val x: Int,
    val y: Int,
    val z: Int,
    val type: BlockType
)

data class AvatarCustomization(
    val headItem: String = "Classic Cap",
    val faceItem: String = "Cool Sunglasses",
    val torsoItem: String = "Cyber Hoodie",
    val pantsItem: String = "Dark Joggers",
    val backItem: String = "Voxel Katana",
    val skinToneHex: String = "#FFDBAC",
    val skinColor: Long = 0xFFFFDBAC,
    val shirtColor: Long = 0xFF2563EB,
    val pantsColor: Long = 0xFF1E293B
)

data class WorldDefinition(
    val id: String,
    val title: String,
    val description: String,
    val mode: GameMode,
    val creatorName: String,
    val imageDrawableRes: Int,
    val maxPlayers: Int = 30,
    val currentOnline: Int = 18,
    val rating: Float = 4.9f,
    val playsCount: String = "450K",
    val gravity: Float = 0.035f,
    val jumpForce: Float = 0.42f,
    val isCustom: Boolean = false
)

data class ConnectedPlayer(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    var z: Float,
    var yaw: Float = 0f,
    var pitch: Float = 0f,
    var isMoving: Boolean = false,
    var isJumping: Boolean = false,
    var animFrame: Float = 0f,
    val platform: PlatformType = PlatformType.PC,
    val pingMs: Int = 24,
    val avatar: AvatarCustomization = AvatarCustomization(),
    var health: Int = 100,
    var score: Int = 0
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderPlatform: PlatformType,
    val text: String,
    val isSystem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ShopItem(
    val id: String,
    val category: String, // "Headwear", "Faces", "Outfits", "Pants", "Back Gear", "Trails"
    val name: String,
    val costCoins: Int,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val progress: Int,
    val target: Int,
    val rewardCoins: Int,
    val isClaimed: Boolean
)

data class BloxCoinPackage(
    val id: String,
    val title: String,
    val coinsAmount: Int,
    val bonusCoins: Int,
    val priceUsd: String,
    val tag: String? = null,
    val iconEmoji: String = "🪙",
    val highlightColor: Long = 0xFFFFC107
) {
    val totalCoins: Int get() = coinsAmount + bonusCoins
}
