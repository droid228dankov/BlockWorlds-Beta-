package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.VoxelEngine3D
import com.example.model.ChatMessage
import com.example.model.ConnectedPlayer
import com.example.model.ElevatorState
import com.example.model.GameMode
import com.example.model.WorldDefinition
import com.example.ui.components.CameraTouchLookZone
import com.example.ui.components.CreativeModeHotbar
import com.example.ui.components.JumpActionButton
import com.example.ui.components.VirtualJoystick
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantEmerald
import com.example.ui.theme.VibrantAmber
import com.example.ui.viewmodel.BlockWorldsViewModel
import kotlinx.coroutines.isActive

@Composable
fun GamePlayScreen(
    viewModel: BlockWorldsViewModel,
    world: WorldDefinition,
    engine: VoxelEngine3D,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val avatar by viewModel.avatarState.collectAsState()
    val bloxCoins by viewModel.bloxCoins.collectAsState()
    val currentRoom by viewModel.multiplayerEngine.currentRoom.collectAsState()
    val peers by viewModel.multiplayerEngine.connectedPlayers.collectAsState()
    val chatMessages by viewModel.multiplayerEngine.chatMessages.collectAsState()

    var moveForward by remember { mutableFloatStateOf(0f) }
    var moveStrafe by remember { mutableFloatStateOf(0f) }
    var jumpTrigger by remember { mutableStateOf(false) }

    var showRosterDialog by remember { mutableStateOf(false) }
    var showChatExpanded by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }
    var frameCount by remember { mutableStateOf(0L) }

    // Sync collected coins in 3D world to persistent BloxCoins
    var lastSavedCoins by remember { mutableStateOf(0) }
    LaunchedEffect(engine.coinsCollected) {
        val diff = engine.coinsCollected - lastSavedCoins
        if (diff > 0) {
            lastSavedCoins = engine.coinsCollected
            viewModel.addEarnedCoins(diff)
        }
    }

    // Game loop running at 60 FPS
    LaunchedEffect(engine) {
        while (isActive) {
            withFrameNanos {
                engine.updatePhysics(
                    moveForward = moveForward,
                    moveStrafe = moveStrafe,
                    jumpRequested = jumpTrigger,
                    gravityVal = world.gravity,
                    jumpForceVal = world.jumpForce
                )
                jumpTrigger = false
                frameCount++
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewWidth = constraints.maxWidth.toFloat()
        val viewHeight = constraints.maxHeight.toFloat()

        // 1. 3D World Canvas Viewport
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_3d_canvas")
        ) {
            val dummyRead = frameCount // recomposition dependency for 60fps loop
            engine.renderScene(
                drawScope = this,
                viewWidth = viewWidth,
                viewHeight = viewHeight,
                localAvatar = avatar,
                localPlayerName = "You",
                multiplayerPeers = peers
            )
        }

        // 2. Camera Touch Drag Area (covers right half of screen)
        CameraTouchLookZone(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 140.dp, bottom = 120.dp, top = 80.dp),
            onRotate = { dYaw, dPitch ->
                engine.camera.yaw = (engine.camera.yaw + dYaw) % 360f
                engine.camera.pitch = (engine.camera.pitch + dPitch).coerceIn(-40f, 65f)
            }
        )

        var showEscapeMenu by remember { mutableStateOf(false) }
        var showHotbar by remember { mutableStateOf(true) }
        var hotbarSelectedItem by remember { mutableStateOf(0) }

        // 3. Iconic Roblox Top Bar (Horizontal Landscape Optimized)
        RobloxTopBar(
            world = world,
            roomCode = currentRoom?.roomCode ?: "BW-4029",
            playerCount = peers.size + 1,
            health = engine.health,
            coins = bloxCoins,
            isElevator = world.mode == GameMode.NORMAL_ELEVATOR,
            elevatorNotification = engine.elevatorNotification,
            onOpenEscapeMenu = { showEscapeMenu = true },
            onToggleChat = { showChatExpanded = !showChatExpanded },
            onOpenStore = { viewModel.openStore() },
            onOpenRoster = { showRosterDialog = true },
            onToggleCamera = {
                engine.camera.isFirstPerson = !engine.camera.isFirstPerson
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3b. The Normal Elevator Floor Announcement Banner
        if (world.mode == GameMode.NORMAL_ELEVATOR) {
            ElevatorFloorBanner(
                notification = engine.elevatorNotification,
                elevatorState = engine.elevatorState,
                floorIndex = engine.elevatorFloorIndex,
                timerSeconds = engine.elevatorTimer.coerceAtLeast(0f).toInt(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp)
            )
        }

        // 4. In-Game Live Chat Overlay (Roblox style upper-left overlay)
        InGameChatOverlay(
            messages = chatMessages.takeLast(5),
            isExpanded = showChatExpanded,
            inputText = chatInputText,
            onInputTextChange = { chatInputText = it },
            onToggleExpand = { showChatExpanded = !showChatExpanded },
            onSendMessage = { text ->
                viewModel.multiplayerEngine.sendChatMessage(text, "You")
                chatInputText = ""
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 52.dp, start = 12.dp)
        )

        // 5. Roblox Hotbar (Bottom Center)
        if (showHotbar && world.mode != GameMode.SANDBOX) {
            RobloxHotbar(
                selectedIndex = hotbarSelectedItem,
                onSelectItem = { index ->
                    hotbarSelectedItem = index
                    when (index) {
                        0 -> {
                            // BloxCola drink effect
                            engine.addSpawnParticles(engine.playerX, engine.playerY + 1.2f, engine.playerZ, 0xFF38BDF8)
                            viewModel.multiplayerEngine.sendChatMessage("*drinks BloxCola - SPEED BOOST!*", "You")
                            Toast.makeText(context, "🥤 Slurp! BloxCola refreshed!", Toast.LENGTH_SHORT).show()
                        }
                        1 -> {
                            // Glowstick
                            engine.addSpawnParticles(engine.playerX, engine.playerY + 0.8f, engine.playerZ, 0xFF22C55E)
                            Toast.makeText(context, "✨ Glowing neon stick equipped!", Toast.LENGTH_SHORT).show()
                        }
                        2 -> {
                            // Emote / Dance
                            engine.addSpawnParticles(engine.playerX, engine.playerY + 1.4f, engine.playerZ, 0xFFEC4899)
                            viewModel.multiplayerEngine.sendChatMessage("*dances energetically*", "You")
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

        // 5. Quick Emote Bar
        QuickEmoteBar(
            onEmote = { emoteName ->
                viewModel.multiplayerEngine.sendChatMessage("*$emoteName*", "You")
                engine.addSpawnParticles(engine.playerX, engine.playerY + 1.2f, engine.playerZ, 0xFFEC4899)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 74.dp, end = 12.dp)
        )

        // 6. Bottom Controls
        // Left Virtual Joystick
        VirtualJoystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 28.dp),
            onMove = { f, s ->
                moveForward = f
                moveStrafe = s
            }
        )

        // Right Jump Action Button
        JumpActionButton(
            isGrounded = engine.isGrounded,
            onJump = {
                jumpTrigger = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 36.dp)
        )

        // Creative Mode Hotbar (for Creative / Sandbox mode)
        if (world.mode == GameMode.SANDBOX) {
            CreativeModeHotbar(
                selectedBlock = engine.activePlaceBlockType,
                onSelectBlock = { engine.activePlaceBlockType = it },
                onMine = {
                    val broke = engine.breakTargetedBlock()
                    if (broke) {
                        viewModel.multiplayerEngine.broadcastBlockEvent(
                            engine.targetedBlockCoord?.x ?: 0,
                            engine.targetedBlockCoord?.y ?: 0,
                            engine.targetedBlockCoord?.z ?: 0,
                            "AIR",
                            false,
                            "You"
                        )
                    }
                },
                onPlace = {
                    val placed = engine.placeBlockAtTarget(engine.activePlaceBlockType)
                    if (placed) {
                        viewModel.multiplayerEngine.broadcastBlockEvent(
                            engine.targetPlaceCoord?.x ?: 0,
                            engine.targetPlaceCoord?.y ?: 0,
                            engine.targetPlaceCoord?.z ?: 0,
                            engine.activePlaceBlockType.name,
                            true,
                            "You"
                        )
                    }
                },
                isFlyMode = engine.isFlyMode,
                onToggleFly = { engine.isFlyMode = !engine.isFlyMode },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }

        // 7. Victory / Stage Finish Modal
        if (engine.isFinished) {
            VictoryCelebrationDialog(
                coinsEarned = 150,
                score = engine.score,
                onPlayAgain = {
                    engine.isFinished = false
                    engine.resetToCheckpoint()
                },
                onExitToLobby = onExit
            )
        }

        // 8. Multiplayer Connected Players Roster Dialog
        if (showRosterDialog) {
            MultiplayerRosterDialog(
                roomCode = currentRoom?.roomCode ?: "BW-4029",
                peers = peers,
                onDismiss = { showRosterDialog = false }
            )
        }

        // 9. Authentic Roblox Escape Menu Dialog
        if (showEscapeMenu) {
            RobloxEscapeMenuDialog(
                worldTitle = world.title,
                roomCode = currentRoom?.roomCode ?: "BW-4029",
                onResume = { showEscapeMenu = false },
                onReset = {
                    engine.resetCharacter()
                    showEscapeMenu = false
                    Toast.makeText(context, "💥 Oof! Character reset!", Toast.LENGTH_SHORT).show()
                },
                onLeave = onExit,
                onDismiss = { showEscapeMenu = false }
            )
        }
    }
}

@Composable
fun RobloxLogoIcon(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = -14f }
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding((size.value * 0.28f).dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF161B22))
        )
    }
}

@Composable
fun RobloxTopBar(
    world: WorldDefinition,
    roomCode: String,
    playerCount: Int,
    health: Int,
    coins: Int,
    isElevator: Boolean,
    elevatorNotification: String,
    onOpenEscapeMenu: () -> Unit,
    onToggleChat: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenRoster: () -> Unit,
    onToggleCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xD910141D),
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(width = 0.5.dp, color = Color(0x33FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Roblox Logo (Escape Menu) & Chat Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tilted Roblox Logo Button
                IconButton(
                    onClick = onOpenEscapeMenu,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("roblox_escape_menu_button")
                ) {
                    RobloxLogoIcon(size = 20.dp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Chat Toggle Button
                IconButton(
                    onClick = onToggleChat,
                    modifier = Modifier.size(34.dp).testTag("top_bar_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Center: World Title / Elevator Banner
            Surface(
                color = Color(0x66000000),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isElevator) "🛗 The Normal Elevator" else world.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            // Right: Health Bar + BloxCoins + Players + Camera
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Health Indicator
                Surface(
                    color = Color(0x66000000),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "❤️ $health",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (health > 35) RadiantEmerald else Color(0xFFFF4D4D)
                        )
                    }
                }

                // BloxCoins Interactive Badge
                com.example.ui.components.BloxCoinBadge(
                    coins = coins,
                    onBuyClick = onOpenStore
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Leaderboard / Players Icon
                Surface(
                    color = Color(0xFF1F242C),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenRoster() }
                        .testTag("open_roster_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Players",
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$playerCount",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Camera Toggle
                IconButton(
                    onClick = onToggleCamera,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Toggle Camera",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ElevatorFloorBanner(
    notification: String,
    elevatorState: ElevatorState,
    floorIndex: Int,
    timerSeconds: Int,
    modifier: Modifier = Modifier
) {
    val bgColor = when (elevatorState) {
        ElevatorState.BOARDING -> Color(0xDD0D5B3A)
        ElevatorState.TRANSIT -> Color(0xDD2A1B4E)
        ElevatorState.FLOOR_ACTIVE -> if (timerSeconds <= 5) Color(0xDDB91C1C) else Color(0xDD1E293B)
        ElevatorState.DOORS_CLOSING -> Color(0xDDB91C1C)
        ElevatorState.DOORS_OPENING -> Color(0xDD0D5B3A)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, VibrantAmber),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = notification,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RobloxHotbar(
    selectedIndex: Int,
    onSelectItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        Pair("🥤", "BloxCola"),
        Pair("🔦", "Glowstick"),
        Pair("💃", "Dance")
    )

    Surface(
        color = Color(0xB3161B22),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tools.forEachIndexed { index, tool ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0x6638BDF8) else Color(0x44000000))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NeonCyan else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectItem(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = tool.first, fontSize = 18.sp)
                        Text(text = "[${index + 1}]", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RobloxEscapeMenuDialog(
    worldTitle: String,
    roomCode: String,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xF2161B22),
        modifier = Modifier.width(360.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RobloxLogoIcon(size = 22.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = worldTitle,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8B949E))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Server Info Chip
                Surface(
                    color = Color(0xFF21262D),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Server: $roomCode", color = Color(0xFF8B949E), fontSize = 11.sp)
                        Text(text = "🌐 Cross-Play (Ping: 22ms)", color = RadiantEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Resume Game Button (Roblox Green/Cyan Style)
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Resume Experience", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Reset Character Button (Roblox "Oof!")
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Reset Character (Oof!)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Leave Game Button (Roblox Red Style)
                Button(
                    onClick = onLeave,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Leave Experience", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun InGameChatOverlay(
    messages: List<ChatMessage>,
    isExpanded: Boolean,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onToggleExpand: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickPills = listOf("GG!", "Nice jump! 🚀", "Help! 😅", "Follow me! 👉", "Watch out! 🔥")

    Column(modifier = modifier.width(220.dp)) {
        // Toggle Chat Icon
        IconButton(
            onClick = onToggleExpand,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0x99161B22))
                .testTag("toggle_chat_button")
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Chat",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Recent messages stream
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x880D1117))
                .padding(8.dp)
        ) {
            messages.forEach { msg ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${msg.senderPlatform.icon} ${msg.senderName}: ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (msg.isSystem) NeonCyan else Color(0xFF58A6FF)
                    )
                    Text(
                        text = msg.text,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Expanded text input & quick pills
        if (isExpanded) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0xEE161B22),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Quick Chat Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickPills) { pill ->
                            Text(
                                text = pill,
                                fontSize = 9.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF21262D))
                                    .clickable { onSendMessage(pill) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = onInputTextChange,
                            placeholder = { Text("Chat...", fontSize = 10.sp, color = Color(0xFF8B949E)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF30363D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("chat_input_field")
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { if (inputText.isNotBlank()) onSendMessage(inputText) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickEmoteBar(
    onEmote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emotes = listOf("👋", "🕺", "🎉", "😂")

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x99161B22))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        emotes.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF21262D))
                    .clickable { onEmote(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun VictoryCelebrationDialog(
    coinsEarned: Int,
    score: Int,
    onPlayAgain: () -> Unit,
    onExitToLobby: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = Color(0xFF161B22),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = "🎉 STAGE COMPLETE! 🎉", color = VibrantAmber, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "You conquered the course!", color = Color(0xFF8B949E), fontSize = 12.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "+$coinsEarned BloxCoins", color = VibrantAmber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Final Score: $score pts", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "🏆 Achievement Unlocked: Galaxy Runner", color = RadiantEmerald, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("PLAY AGAIN", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onExitToLobby) {
                Text("LOBBY", color = Color(0xFF8B949E))
            }
        }
    )
}

@Composable
fun MultiplayerRosterDialog(
    roomCode: String,
    peers: List<ConnectedPlayer>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connected Players (${peers.size + 1})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Surface(color = Color(0xFF21262D), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = roomCode,
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Local player row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📱 You (Host)", color = RadiantEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "12ms", color = RadiantEmerald, fontSize = 11.sp)
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF30363D)))

                peers.forEach { peer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${peer.platform.icon} ${peer.name}",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${peer.pingMs}ms",
                            color = Color(0xFF8B949E),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = NeonCyan)
            }
        }
    )
}
