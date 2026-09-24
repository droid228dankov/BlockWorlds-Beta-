package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.db.BlockWorldsDatabase
import com.example.data.db.CustomWorldEntity
import com.example.data.db.PlayerProfileEntity
import com.example.data.repository.BlockWorldsRepository
import com.example.engine.VoxelEngine3D
import com.example.engine.WorldGenerator
import com.example.model.AchievementItem
import com.example.model.AvatarCustomization
import com.example.model.BlockType
import com.example.model.GameMode
import com.example.model.PlatformType
import com.example.model.ShopItem
import com.example.model.WorldDefinition
import com.example.multiplayer.MultiplayerEngine
import com.example.multiplayer.ServerRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BlockWorldsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BlockWorldsDatabase.getInstance(application)
    private val repository = BlockWorldsRepository(
        profileDao = db.playerProfileDao(),
        customWorldDao = db.customWorldDao(),
        serverHistoryDao = db.serverHistoryDao()
    )

    val multiplayerEngine = MultiplayerEngine(viewModelScope)

    // Current navigation tab
    val currentTab = MutableStateFlow(0) // 0: Discover, 1: Multiplayer Hub, 2: Avatar Studio, 3: World Studio, 4: Shop

    // Active Game Session
    private val _activeWorld = MutableStateFlow<WorldDefinition?>(null)
    val activeWorld: StateFlow<WorldDefinition?> = _activeWorld.asStateFlow()

    private val _gameEngine = MutableStateFlow<VoxelEngine3D?>(null)
    val gameEngine: StateFlow<VoxelEngine3D?> = _gameEngine.asStateFlow()

    // Room Database Profile
    val profileEntity = repository.profile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val customWorlds = repository.customWorlds.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Player Avatar state
    private val _avatarState = MutableStateFlow(
        AvatarCustomization(
            headItem = "Cyber Visor",
            faceItem = "Cool Sunglasses",
            torsoItem = "Cyber Hoodie",
            pantsItem = "Dark Joggers",
            backItem = "Voxel Katana",
            skinColor = 0xFFFFDBAC,
            shirtColor = 0xFF2563EB,
            pantsColor = 0xFF1E293B
        )
    )
    val avatarState: StateFlow<AvatarCustomization> = _avatarState.asStateFlow()

    // BloxCoins Balance
    val bloxCoins = MutableStateFlow(1250)

    // Shop Catalog
    val shopItems = MutableStateFlow(
        listOf(
            ShopItem("h1", "Headwear", "Cyber Visor", 0, "🥽", isUnlocked = true, isEquipped = true),
            ShopItem("h2", "Headwear", "Golden Crown", 350, "👑", isUnlocked = false, isEquipped = false),
            ShopItem("h3", "Headwear", "Ninja Mask", 250, "🥷", isUnlocked = false, isEquipped = false),
            ShopItem("h4", "Headwear", "Astronaut Helmet", 500, "👨‍🚀", isUnlocked = false, isEquipped = false),
            ShopItem("t1", "Outfits", "Cyber Hoodie", 0, "🥋", isUnlocked = true, isEquipped = true),
            ShopItem("t2", "Outfits", "Pixel Knight Armor", 400, "🛡️", isUnlocked = false, isEquipped = false),
            ShopItem("t3", "Outfits", "Golden Tuxedo", 600, "🤵", isUnlocked = false, isEquipped = false),
            ShopItem("t4", "Outfits", "Space Suit", 450, "🚀", isUnlocked = false, isEquipped = false),
            ShopItem("b1", "Back Gear", "Voxel Katana", 0, "⚔️", isUnlocked = true, isEquipped = true),
            ShopItem("b2", "Back Gear", "Cyber Wings", 450, "🪽", isUnlocked = false, isEquipped = false),
            ShopItem("b3", "Back Gear", "Rocket Jetpack", 700, "🎒", isUnlocked = false, isEquipped = false),
            ShopItem("p1", "Pants", "Dark Joggers", 0, "👖", isUnlocked = true, isEquipped = true),
            ShopItem("p2", "Pants", "Golden Greaves", 300, "✨", isUnlocked = false, isEquipped = false),
            ShopItem("p3", "Pants", "Cyber Tracksuit", 250, "⚡", isUnlocked = false, isEquipped = false)
        )
    )

    // Achievements
    val achievements = MutableStateFlow(
        listOf(
            AchievementItem("a1", "First Jump", "Complete your first Obby course", "🏆", 1, 1, 100, true),
            AchievementItem("a2", "Master Builder", "Place 100 blocks in Creative mode", "🧱", 45, 100, 250, false),
            AchievementItem("a3", "Cross-Play Pioneer", "Join a server with PC, Web & Console players", "🌐", 1, 1, 300, true),
            AchievementItem("a4", "Lava Survivor", "Survive 2 minutes in Lava Escape", "🌋", 1, 2, 400, false)
        )
    )

    // Featured Worlds Catalog
    val featuredWorlds = listOf(
        WorldDefinition(
            id = "w_normal_elevator",
            title = "🛗 The Normal Elevator",
            description = "Step inside the elevator! Survive unpredictable random floors, lava pits, alien disco raves, giant noobs, and space dimensions with friends!",
            mode = GameMode.NORMAL_ELEVATOR,
            creatorName = "NowDoTheHarlemShake",
            imageDrawableRes = R.drawable.img_normal_elevator,
            maxPlayers = 50,
            currentOnline = 48,
            rating = 5.0f,
            playsCount = "5.8M",
            jumpForce = 0.40f
        ),
        WorldDefinition(
            id = "w_rainbow_obby",
            title = "🌈 Rainbow Galaxy Mega Obby",
            description = "Conquer 50 neon platforms, rotating lava beams, jump pads, and reach the finish portal!",
            mode = GameMode.OBBY,
            creatorName = "VoxelGods_Dev",
            imageDrawableRes = R.drawable.img_world_obby,
            maxPlayers = 40,
            currentOnline = 32,
            rating = 4.9f,
            playsCount = "1.2M",
            jumpForce = 0.42f
        ),
        WorldDefinition(
            id = "w_voxel_sandbox",
            title = "🏰 Voxel Kingdom & Castle Builder",
            description = "Infinite creative block canvas. Mine, place 16+ block types, fly, and construct with friends in real-time!",
            mode = GameMode.SANDBOX,
            creatorName = "BlockCraft_Studio",
            imageDrawableRes = R.drawable.img_world_sandbox,
            maxPlayers = 24,
            currentOnline = 19,
            rating = 4.8f,
            playsCount = "890K"
        ),
        WorldDefinition(
            id = "w_lava_survival",
            title = "🌋 Rising Lava Tower Escape",
            description = "The molten floor is constantly rising! Parkour up crumbling towers before the lava catches you!",
            mode = GameMode.LAVA_SURVIVAL,
            creatorName = "DangerZone_Team",
            imageDrawableRes = R.drawable.img_world_lava,
            maxPlayers = 30,
            currentOnline = 26,
            rating = 4.9f,
            playsCount = "620K"
        ),
        WorldDefinition(
            id = "w_battle_arena",
            title = "⚔️ Neon Cyber Tag Arena",
            description = "Fast-paced futuristic arena with jump pads, speed boost portals, and tag battles across platforms!",
            mode = GameMode.BATTLE_ARENA,
            creatorName = "CyberPlay",
            imageDrawableRes = R.drawable.img_world_obby,
            maxPlayers = 20,
            currentOnline = 15,
            rating = 4.7f,
            playsCount = "410K"
        )
    )

    init {
        // Initialize local profile if empty
        viewModelScope.launch {
            repository.profile.collect { prof ->
                if (prof == null) {
                    val defaultProf = PlayerProfileEntity()
                    repository.saveProfile(defaultProf)
                } else {
                    bloxCoins.value = prof.bloxCoins
                    _avatarState.value = AvatarCustomization(
                        headItem = prof.headItem,
                        faceItem = prof.faceItem,
                        torsoItem = prof.torsoItem,
                        pantsItem = prof.pantsItem,
                        backItem = prof.backItem,
                        skinColor = prof.skinColor,
                        shirtColor = prof.shirtColor,
                        pantsColor = prof.pantsColor
                    )
                }
            }
        }
    }

    fun launchWorld(world: WorldDefinition, roomCode: String? = null) {
        val blocks = WorldGenerator.generateWorld(world.mode)
        val engine = VoxelEngine3D(world.mode, blocks)
        _activeWorld.value = world
        _gameEngine.value = engine

        val playerNick = profileEntity.value?.username ?: "BlockBuilder_99"

        // Connect multiplayer engine to room
        if (roomCode != null) {
            multiplayerEngine.joinByRoomCode(roomCode, playerNick)
        } else {
            val server = ServerRoom(
                id = "world_${world.id}",
                name = world.title,
                roomCode = "BW-${world.id.hashCode().toString().takeLast(4)}",
                gameMode = world.mode,
                hostPlayerName = world.creatorName,
                hostPlatform = PlatformType.PC,
                currentPlayers = world.currentOnline,
                maxPlayers = world.maxPlayers,
                pingMs = 28,
                region = "Global Multi-Platform",
                isCrossPlay = true
            )
            multiplayerEngine.joinRoom(server, playerNick)
        }
    }

    fun exitGame() {
        multiplayerEngine.leaveCurrentRoom()
        _gameEngine.value = null
        _activeWorld.value = null
    }

    fun updateAvatar(customization: AvatarCustomization) {
        _avatarState.value = customization
        viewModelScope.launch {
            val curr = profileEntity.value ?: PlayerProfileEntity()
            repository.saveProfile(
                curr.copy(
                    headItem = customization.headItem,
                    faceItem = customization.faceItem,
                    torsoItem = customization.torsoItem,
                    pantsItem = customization.pantsItem,
                    backItem = customization.backItem,
                    skinColor = customization.skinColor,
                    shirtColor = customization.shirtColor,
                    pantsColor = customization.pantsColor
                )
            )
        }
    }

    fun buyOrEquipItem(item: ShopItem) {
        val coins = bloxCoins.value
        if (!item.isUnlocked) {
            if (coins >= item.costCoins) {
                val newBal = coins - item.costCoins
                bloxCoins.value = newBal
                viewModelScope.launch { repository.addCoins(-item.costCoins) }
                shopItems.update { list ->
                    list.map { if (it.id == item.id) it.copy(isUnlocked = true) else it }
                }
            } else {
                return
            }
        }

        // Equip item
        val currentAv = _avatarState.value
        val updatedAv = when (item.category) {
            "Headwear" -> currentAv.copy(headItem = item.name)
            "Outfits" -> {
                val shirtC = when (item.name) {
                    "Pixel Knight Armor" -> 0xFF64748B
                    "Golden Tuxedo" -> 0xFFFFD700
                    "Space Suit" -> 0xFFE2E8F0
                    else -> 0xFF2563EB
                }
                currentAv.copy(torsoItem = item.name, shirtColor = shirtC)
            }
            "Pants" -> {
                val pantsC = when (item.name) {
                    "Golden Greaves" -> 0xFFFFC107
                    "Cyber Tracksuit" -> 0xFF0284C7
                    else -> 0xFF1E293B
                }
                currentAv.copy(pantsItem = item.name, pantsColor = pantsC)
            }
            "Back Gear" -> currentAv.copy(backItem = item.name)
            else -> currentAv
        }
        updateAvatar(updatedAv)
    }

    fun claimDailyCoins() {
        val bonus = 250
        bloxCoins.update { it + bonus }
        viewModelScope.launch {
            repository.addCoins(bonus)
        }
    }

    fun claimAchievement(achievementId: String) {
        val item = achievements.value.firstOrNull { it.id == achievementId } ?: return
        if (!item.isClaimed && item.progress >= item.target) {
            bloxCoins.update { it + item.rewardCoins }
            viewModelScope.launch { repository.addCoins(item.rewardCoins) }
            achievements.update { list ->
                list.map { if (it.id == achievementId) it.copy(isClaimed = true) else it }
            }
        }
    }

    val isStoreOpen = MutableStateFlow(false)

    val bloxCoinPackages = listOf(
        com.example.model.BloxCoinPackage(
            id = "pack_starter",
            title = "Handful of Coins",
            coinsAmount = 500,
            bonusCoins = 0,
            priceUsd = "$0.99",
            iconEmoji = "🪙",
            highlightColor = 0xFF38BDF8
        ),
        com.example.model.BloxCoinPackage(
            id = "pack_bag",
            title = "Bag of Coins",
            coinsAmount = 1200,
            bonusCoins = 200,
            priceUsd = "$1.99",
            tag = "+20% BONUS",
            iconEmoji = "💰",
            highlightColor = 0xFF4ADE80
        ),
        com.example.model.BloxCoinPackage(
            id = "pack_chest",
            title = "Chest of Coins",
            coinsAmount = 3500,
            bonusCoins = 1000,
            priceUsd = "$4.99",
            tag = "MOST POPULAR ⭐",
            iconEmoji = "💎",
            highlightColor = 0xFFF59E0B
        ),
        com.example.model.BloxCoinPackage(
            id = "pack_safe",
            title = "Safe of Wealth",
            coinsAmount = 10000,
            bonusCoins = 3500,
            priceUsd = "$9.99",
            tag = "BEST VALUE 🔥",
            iconEmoji = "🏆",
            highlightColor = 0xFFA855F7
        ),
        com.example.model.BloxCoinPackage(
            id = "pack_vault",
            title = "Voxel Vault of Blox",
            coinsAmount = 25000,
            bonusCoins = 10000,
            priceUsd = "$19.99",
            tag = "VIP MEGA PACK 👑",
            iconEmoji = "👑",
            highlightColor = 0xFFEF4444
        )
    )

    fun openStore() {
        isStoreOpen.value = true
    }

    fun closeStore() {
        isStoreOpen.value = false
    }

    fun addEarnedCoins(amount: Int) {
        bloxCoins.update { it + amount }
        viewModelScope.launch {
            repository.addCoins(amount)
        }
    }

    fun purchaseCoins(pkg: com.example.model.BloxCoinPackage) {
        val total = pkg.totalCoins
        bloxCoins.update { it + total }
        viewModelScope.launch {
            repository.addCoins(total)
        }
        multiplayerEngine.sendChatMessage("🪙 Purchased ${pkg.title} (+$total BloxCoins)!", "System")
    }

    fun saveCustomWorld(title: String, desc: String, mode: GameMode, maxPlayers: Int) {
        viewModelScope.launch {
            val entity = CustomWorldEntity(
                title = title,
                description = desc,
                gameMode = mode.name,
                maxPlayers = maxPlayers
            )
            repository.createCustomWorld(entity)
        }
    }
}
