package com.example.multiplayer

import com.example.model.AvatarCustomization
import com.example.model.ChatMessage
import com.example.model.ConnectedPlayer
import com.example.model.GameMode
import com.example.model.PlatformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class MultiplayerEngine(
    private val scope: CoroutineScope
) {
    private val _currentRoom = MutableStateFlow<ServerRoom?>(null)
    val currentRoom: StateFlow<ServerRoom?> = _currentRoom.asStateFlow()

    private val _connectedPlayers = MutableStateFlow<List<ConnectedPlayer>>(emptyList())
    val connectedPlayers: StateFlow<List<ConnectedPlayer>> = _connectedPlayers.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _networkBlockEvents = MutableStateFlow<NetworkBlockEvent?>(null)
    val networkBlockEvents: StateFlow<NetworkBlockEvent?> = _networkBlockEvents.asStateFlow()

    private val _publicServers = MutableStateFlow<List<ServerRoom>>(defaultPublicServers())
    val publicServers: StateFlow<List<ServerRoom>> = _publicServers.asStateFlow()

    private var networkSimJob: Job? = null
    private var chatSimJob: Job? = null

    companion object {
        fun defaultPublicServers(): List<ServerRoom> = listOf(
            ServerRoom(
                id = "srv_1",
                name = "🌈 Rainbow Galaxy Mega Obby [Cross-Play]",
                roomCode = "BW-7128",
                gameMode = GameMode.OBBY,
                hostPlayerName = "ApexBuilder_PC",
                hostPlatform = PlatformType.PC,
                currentPlayers = 24,
                maxPlayers = 35,
                pingMs = 28,
                region = "US-East",
                isCrossPlay = true,
                pvpEnabled = false
            ),
            ServerRoom(
                id = "srv_2",
                name = "🏰 Voxel City & Castle Creative [Free Build]",
                roomCode = "BW-4091",
                gameMode = GameMode.SANDBOX,
                hostPlayerName = "VoxelQueen_Web",
                hostPlatform = PlatformType.WEB,
                currentPlayers = 16,
                maxPlayers = 24,
                pingMs = 35,
                region = "EU-Central",
                isCrossPlay = true,
                pvpEnabled = false
            ),
            ServerRoom(
                id = "srv_3",
                name = "🌋 Rising Lava Tower Escape [Hardcore 60 FPS]",
                roomCode = "BW-9382",
                gameMode = GameMode.LAVA_SURVIVAL,
                hostPlayerName = "SpeedDemon_Mobile",
                hostPlatform = PlatformType.ANDROID,
                currentPlayers = 29,
                maxPlayers = 30,
                pingMs = 42,
                region = "US-West",
                isCrossPlay = true,
                pvpEnabled = true
            ),
            ServerRoom(
                id = "srv_4",
                name = "⚔️ Neon Cyber Tag & Sword Arena",
                roomCode = "BW-1104",
                gameMode = GameMode.BATTLE_ARENA,
                hostPlayerName = "GlitchKnight_Console",
                hostPlatform = PlatformType.CONSOLE,
                currentPlayers = 12,
                maxPlayers = 20,
                pingMs = 31,
                region = "Asia-Tokyo",
                isCrossPlay = true,
                pvpEnabled = true
            )
        )
    }

    fun joinRoom(room: ServerRoom, localPlayerName: String) {
        _currentRoom.value = room
        _chatMessages.value = listOf(
            ChatMessage(
                id = "sys_1",
                senderName = "System",
                senderPlatform = PlatformType.PC,
                text = "Connected to ${room.name} [${room.roomCode}]. Cross-platform sync active (Tick 30Hz).",
                isSystem = true
            ),
            ChatMessage(
                id = "sys_2",
                senderName = "System",
                senderPlatform = PlatformType.WEB,
                text = "Cross-play enabled across Mobile, PC, Web, and Console.",
                isSystem = true
            )
        )

        // Spawn multiplayer co-players with cross-platform badges
        val peerBots = generatePeerPlayers(room.gameMode)
        _connectedPlayers.value = peerBots

        startNetworkLoop()
        startChatSimulation(room)
    }

    fun hostServer(
        name: String,
        mode: GameMode,
        maxPlayers: Int,
        isCrossPlay: Boolean,
        pvpEnabled: Boolean,
        localPlayerName: String
    ): ServerRoom {
        val code = "BW-${Random.nextInt(1000, 9999)}"
        val newRoom = ServerRoom(
            id = "host_${System.currentTimeMillis()}",
            name = name,
            roomCode = code,
            gameMode = mode,
            hostPlayerName = localPlayerName,
            hostPlatform = PlatformType.ANDROID,
            currentPlayers = 1,
            maxPlayers = maxPlayers,
            pingMs = 12,
            region = "Global (Host)",
            isCrossPlay = isCrossPlay,
            pvpEnabled = pvpEnabled
        )

        // Add to public list
        _publicServers.update { listOf(newRoom) + it }
        joinRoom(newRoom, localPlayerName)
        return newRoom
    }

    fun joinByRoomCode(code: String, localPlayerName: String): Boolean {
        val cleanCode = code.trim().uppercase()
        val found = _publicServers.value.firstOrNull { it.roomCode.equals(cleanCode, ignoreCase = true) }
        val target = found ?: ServerRoom(
            id = "custom_${cleanCode}",
            name = "Private Cross-Platform Lobby [$cleanCode]",
            roomCode = cleanCode,
            gameMode = GameMode.OBBY,
            hostPlayerName = "GlobalHost",
            hostPlatform = PlatformType.PC,
            currentPlayers = 6,
            maxPlayers = 24,
            pingMs = 38,
            region = "Direct P2P",
            isCrossPlay = true
        )
        joinRoom(target, localPlayerName)
        return true
    }

    fun leaveCurrentRoom() {
        networkSimJob?.cancel()
        chatSimJob?.cancel()
        _currentRoom.value = null
        _connectedPlayers.value = emptyList()
        _chatMessages.value = emptyList()
    }

    fun sendChatMessage(text: String, localPlayerName: String, platform: PlatformType = PlatformType.ANDROID) {
        if (text.isBlank()) return
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_${Random.nextInt(100)}",
            senderName = localPlayerName,
            senderPlatform = platform,
            text = text.trim()
        )
        _chatMessages.update { (it + newMsg).takeLast(40) }
    }

    fun broadcastBlockEvent(x: Int, y: Int, z: Int, typeName: String, isPlacement: Boolean, playerId: String) {
        _networkBlockEvents.value = NetworkBlockEvent(
            x = x,
            y = y,
            z = z,
            blockTypeName = typeName,
            isPlacement = isPlacement,
            playerId = playerId
        )
    }

    private fun startNetworkLoop() {
        networkSimJob?.cancel()
        networkSimJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(100) // 10Hz network transform update
                _connectedPlayers.update { list ->
                    list.map { player ->
                        // Smoothly wander or jump slightly in the obstacle course
                        val wanderDx = (Random.nextFloat() - 0.5f) * 0.12f
                        val wanderDz = (Random.nextFloat() - 0.5f) * 0.12f
                        val isMoving = Random.nextFloat() > 0.35f
                        val shouldJump = Random.nextFloat() > 0.88f

                        var newY = player.y
                        if (shouldJump && !player.isJumping) {
                            newY += 0.8f
                        } else if (newY > 1.2f) {
                            newY = (newY - 0.2f).coerceAtLeast(1.0f)
                        }

                        player.copy(
                            x = (player.x + if (isMoving) wanderDx else 0f).coerceIn(-12f, 12f),
                            z = (player.z + if (isMoving) wanderDz else 0f).coerceIn(0f, 60f),
                            y = newY,
                            yaw = (player.yaw + (if (isMoving) Random.nextFloat() * 10f - 5f else 0f)) % 360f,
                            isMoving = isMoving,
                            isJumping = shouldJump,
                            animFrame = (player.animFrame + 0.3f) % 6.28f
                        )
                    }
                }
            }
        }
    }

    private fun startChatSimulation(room: ServerRoom) {
        chatSimJob?.cancel()
        chatSimJob = scope.launch(Dispatchers.Default) {
            val sampleChats = listOf(
                Pair("Alex_Pro", "Watch out for the spinning lava beam! 🔥"),
                Pair("NeonVoxel_99", "Nice jump bro!! GG!"),
                Pair("PixelKnight_PC", "Who wants to team up for stage 4?"),
                Pair("SkyWalker_Web", "The speed pad shortcut is so good! 🚀"),
                Pair("BlockMaster", "Check out my castle near spawn!"),
                Pair("GamerGirl_Console", "Wait for me guys haha"),
                Pair("CyberSamurai", "Almost hit the checkpoint! 🚩")
            )

            while (isActive) {
                delay(Random.nextLong(6000, 14000))
                if (_currentRoom.value == null) break
                val (name, text) = sampleChats.random()
                val platforms = listOf(PlatformType.PC, PlatformType.WEB, PlatformType.CONSOLE, PlatformType.IOS)
                val msg = ChatMessage(
                    id = "sim_${System.currentTimeMillis()}",
                    senderName = name,
                    senderPlatform = platforms.random(),
                    text = text
                )
                _chatMessages.update { (it + msg).takeLast(40) }
            }
        }
    }

    private fun generatePeerPlayers(mode: GameMode): List<ConnectedPlayer> {
        return listOf(
            ConnectedPlayer(
                id = "peer_1",
                name = "Krono_PC",
                x = 1.5f,
                y = 1.0f,
                z = 6.0f,
                platform = PlatformType.PC,
                pingMs = 24,
                avatar = AvatarCustomization(
                    headItem = "Cyber Visor",
                    torsoItem = "Cyber Hoodie",
                    shirtColor = 0xFF00E5FF,
                    pantsColor = 0xFF1E293B
                )
            ),
            ConnectedPlayer(
                id = "peer_2",
                name = "SunnyVoxel_Web",
                x = -2.0f,
                y = 1.0f,
                z = 10.5f,
                platform = PlatformType.WEB,
                pingMs = 38,
                avatar = AvatarCustomization(
                    headItem = "Golden Crown",
                    torsoItem = "Tuxedo",
                    shirtColor = 0xFFFFD700,
                    pantsColor = 0xFF0F172A
                )
            ),
            ConnectedPlayer(
                id = "peer_3",
                name = "VoxelBlade_Console",
                x = 0.5f,
                y = 1.0f,
                z = 15.0f,
                platform = PlatformType.CONSOLE,
                pingMs = 32,
                avatar = AvatarCustomization(
                    headItem = "Ninja Mask",
                    torsoItem = "Pixel Knight",
                    shirtColor = 0xFFEC4899,
                    pantsColor = 0xFF334155
                )
            ),
            ConnectedPlayer(
                id = "peer_4",
                name = "Aero_Mobile",
                x = -1.2f,
                y = 1.0f,
                z = 3.5f,
                platform = PlatformType.ANDROID,
                pingMs = 45,
                avatar = AvatarCustomization(
                    headItem = "Classic Cap",
                    torsoItem = "Classic Blue",
                    shirtColor = 0xFF10B981,
                    pantsColor = 0xFF1E293B
                )
            )
        )
    }
}
