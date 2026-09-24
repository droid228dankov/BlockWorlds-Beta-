package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.PlatformType
import com.example.model.WorldDefinition
import com.example.multiplayer.ServerRoom
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantEmerald
import com.example.ui.theme.VibrantAmber
import com.example.ui.viewmodel.BlockWorldsViewModel

@Composable
fun MultiplayerHubScreen(
    viewModel: BlockWorldsViewModel,
    onLaunchRoom: (WorldDefinition, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val publicServers by viewModel.multiplayerEngine.publicServers.collectAsState()
    var roomCodeInput by remember { mutableStateOf("") }
    var selectedPlatformFilter by remember { mutableStateOf("All Platforms") }
    var showHostDialog by remember { mutableStateOf(false) }

    val platformFilters = listOf("All Platforms", "📱 Mobile", "💻 PC", "🌐 Web", "🎮 Console")

    val filteredServers = remember(publicServers, selectedPlatformFilter) {
        when (selectedPlatformFilter) {
            "📱 Mobile" -> publicServers.filter { it.hostPlatform == PlatformType.ANDROID || it.hostPlatform == PlatformType.IOS }
            "💻 PC" -> publicServers.filter { it.hostPlatform == PlatformType.PC }
            "🌐 Web" -> publicServers.filter { it.hostPlatform == PlatformType.WEB }
            "🎮 Console" -> publicServers.filter { it.hostPlatform == PlatformType.CONSOLE }
            else -> publicServers
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MULTIPLAYER HUB",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = NeonCyan
                    )
                    Text(
                        text = "Cross-Play Server Browser & Rooms",
                        fontSize = 12.sp,
                        color = Color(0xFF8B949E)
                    )
                }

                // Global ping indicator
                Surface(
                    color = Color(0xFF161B22),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RadiantEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "28ms • Global",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RadiantEmerald
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Host Server & Quick Actions Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Host a Cross-Platform Server",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "PC, Mobile, Web & Console players can join instantly",
                                fontSize = 11.sp,
                                color = Color(0xFF8B949E)
                            )
                        }

                        Button(
                            onClick = { showHostDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("open_host_server_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Host",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HOST", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF30363D))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Join by Room Code Section
                    Text(
                        text = "Join by Room Code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC9D1D9)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = roomCodeInput,
                            onValueChange = { if (it.length <= 8) roomCodeInput = it.uppercase() },
                            placeholder = { Text("e.g. BW-7128", color = Color(0xFF6E7681), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("room_code_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF30363D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                if (roomCodeInput.isNotBlank()) {
                                    val targetWorld = viewModel.featuredWorlds[0]
                                    viewModel.multiplayerEngine.joinByRoomCode(roomCodeInput, "Player_Mobile")
                                    onLaunchRoom(targetWorld, roomCodeInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("join_by_code_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = "Join",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("JOIN", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Platform Filter Chips
        item {
            Text(
                text = "Live Server Browser",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(platformFilters) { filter ->
                    val isSelected = filter == selectedPlatformFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPlatformFilter = filter },
                        label = { Text(filter, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF161B22),
                            labelColor = Color(0xFFC9D1D9)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyan else Color(0xFF30363D)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Server List
        items(filteredServers) { server ->
            ServerItemCard(
                server = server,
                onJoin = {
                    val matchingWorld = viewModel.featuredWorlds.firstOrNull { it.mode == server.gameMode }
                        ?: viewModel.featuredWorlds[0]
                    viewModel.multiplayerEngine.joinRoom(server, "Player_Mobile")
                    onLaunchRoom(matchingWorld, server.roomCode)
                },
                onCopyLink = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Room Link", "https://blockworlds.gg/join/${server.roomCode}")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Room link copied: blockworlds.gg/join/${server.roomCode}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }

    // Host Server Dialog
    if (showHostDialog) {
        HostServerDialog(
            onDismiss = { showHostDialog = false },
            onConfirmHost = { name, mode, maxPlayers, isCrossPlay, pvp ->
                val newRoom = viewModel.multiplayerEngine.hostServer(
                    name = name,
                    mode = mode,
                    maxPlayers = maxPlayers,
                    isCrossPlay = isCrossPlay,
                    pvpEnabled = pvp,
                    localPlayerName = "Host_Player"
                )
                showHostDialog = false
                val matchingWorld = viewModel.featuredWorlds.firstOrNull { it.mode == mode }
                    ?: viewModel.featuredWorlds[0]
                onLaunchRoom(matchingWorld, newRoom.roomCode)
            }
        )
    }
}

@Composable
fun ServerItemCard(
    server: ServerRoom,
    onJoin: () -> Unit,
    onCopyLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                // Room Code Badge
                Surface(
                    color = Color(0xFF21262D),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D))
                ) {
                    Text(
                        text = server.roomCode,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges row: Game Mode, Host Platform, Region, Ping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(server.gameMode.badgeColor).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = server.gameMode.label,
                        color = Color(server.gameMode.badgeColor),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Host: ${server.hostPlatform.icon} ${server.hostPlayerName}",
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E)
                )

                Text(
                    text = "• ${server.region}",
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E)
                )

                Text(
                    text = "• ${server.pingMs}ms",
                    fontSize = 11.sp,
                    color = RadiantEmerald,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Players count bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👥 ${server.currentPlayers}/${server.maxPlayers}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cross-Play",
                        fontSize = 10.sp,
                        color = NeonCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopyLink,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Room Link",
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("join_server_${server.roomCode}")
                    ) {
                        Text("JOIN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HostServerDialog(
    onDismiss: () -> Unit,
    onConfirmHost: (name: String, mode: GameMode, maxPlayers: Int, isCrossPlay: Boolean, pvp: Boolean) -> Unit
) {
    var serverName by remember { mutableStateOf("My Epic BlockWorld Server") }
    var selectedMode by remember { mutableStateOf(GameMode.OBBY) }
    var maxPlayers by remember { mutableFloatStateOf(24f) }
    var isCrossPlay by remember { mutableStateOf(true) }
    var pvpEnabled by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = {
            Text(
                text = "Host Cross-Platform Server",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text("Server Name", color = Color(0xFF8B949E)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF30363D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Game Mode", color = Color(0xFF8B949E), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GameMode.values()) { mode ->
                        val isSel = mode == selectedMode
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedMode = mode },
                            label = { Text(mode.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF21262D),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Max Players: ${maxPlayers.toInt()}", color = Color(0xFF8B949E), fontSize = 12.sp)
                Slider(
                    value = maxPlayers,
                    onValueChange = { maxPlayers = it },
                    valueRange = 4f..50f,
                    steps = 22
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cross-Platform Play", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Allow PC, Web & Console users", color = Color(0xFF8B949E), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isCrossPlay,
                        onCheckedChange = { isCrossPlay = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmHost(
                        serverName,
                        selectedMode,
                        maxPlayers.toInt(),
                        isCrossPlay,
                        pvpEnabled
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("LAUNCH SERVER", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF8B949E))
            }
        }
    )
}
