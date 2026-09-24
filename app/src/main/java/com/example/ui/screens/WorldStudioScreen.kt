package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.CustomWorldEntity
import com.example.model.GameMode
import com.example.model.WorldDefinition
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantEmerald
import com.example.ui.theme.VibrantAmber
import com.example.ui.viewmodel.BlockWorldsViewModel

@Composable
fun WorldStudioScreen(
    viewModel: BlockWorldsViewModel,
    onPlayWorld: (WorldDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    val customWorlds by viewModel.customWorlds.collectAsState()

    var worldName by remember { mutableStateOf("") }
    var worldDescription by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(GameMode.SANDBOX) }
    var maxPlayersSlider by remember { mutableFloatStateOf(16f) }

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
                        text = "WORLD STUDIO",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = NeonCyan
                    )
                    Text(
                        text = "Create, Build & Share Cross-Platform Games",
                        fontSize = 12.sp,
                        color = Color(0xFF8B949E)
                    )
                }

                Surface(
                    color = Color(0xFF161B22),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D))
                ) {
                    Text(
                        text = "🛠️ Level Editor",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // World Creator Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Create New World",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = worldName,
                        onValueChange = { worldName = it },
                        label = { Text("World Name", color = Color(0xFF8B949E)) },
                        placeholder = { Text("e.g. Cyber Sky Castle", color = Color(0xFF6E7681)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF30363D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_world_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = worldDescription,
                        onValueChange = { worldDescription = it },
                        label = { Text("Description", color = Color(0xFF8B949E)) },
                        placeholder = { Text("Describe obstacles or building rules...", color = Color(0xFF6E7681)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF30363D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Game Template / Mode", color = Color(0xFF8B949E), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(GameMode.values()) { mode ->
                            val isSel = mode == selectedMode
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedMode = mode },
                                label = { Text(mode.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF21262D),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Max Cross-Platform Players: ${maxPlayersSlider.toInt()}", color = Color(0xFF8B949E), fontSize = 12.sp)
                    Slider(
                        value = maxPlayersSlider,
                        onValueChange = { maxPlayersSlider = it },
                        valueRange = 4f..50f,
                        steps = 22
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val title = if (worldName.isNotBlank()) worldName.trim() else "My Custom ${selectedMode.label}"
                            val desc = if (worldDescription.isNotBlank()) worldDescription.trim() else "Custom multiplayer block world"
                            viewModel.saveCustomWorld(
                                title = title,
                                desc = desc,
                                mode = selectedMode,
                                maxPlayers = maxPlayersSlider.toInt()
                            )

                            // Launch immediately into play
                            val worldDef = WorldDefinition(
                                id = "custom_${System.currentTimeMillis()}",
                                title = title,
                                description = desc,
                                mode = selectedMode,
                                creatorName = "You",
                                imageDrawableRes = when (selectedMode) {
                                    GameMode.NORMAL_ELEVATOR -> R.drawable.img_normal_elevator
                                    GameMode.OBBY -> R.drawable.img_world_obby
                                    GameMode.SANDBOX -> R.drawable.img_world_sandbox
                                    GameMode.LAVA_SURVIVAL -> R.drawable.img_world_lava
                                    GameMode.BATTLE_ARENA -> R.drawable.img_world_obby
                                },
                                maxPlayers = maxPlayersSlider.toInt(),
                                isCustom = true
                            )
                            onPlayWorld(worldDef)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_world_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CREATE & PLAY WORLD",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // My Saved Worlds Section
        item {
            Text(
                text = "My Custom Worlds (${customWorlds.size})",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (customWorlds.isEmpty()) {
            item {
                Surface(
                    color = Color(0xFF161B22),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No custom worlds created yet.", color = Color(0xFF8B949E), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Build your first level using the form above!", color = Color(0xFF6E7681), fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(customWorlds) { entity ->
                CustomWorldCard(
                    world = entity,
                    onPlay = {
                        val mode = try { GameMode.valueOf(entity.gameMode) } catch (e: Exception) { GameMode.SANDBOX }
                        val def = WorldDefinition(
                            id = "custom_${entity.id}",
                            title = entity.title,
                            description = entity.description,
                            mode = mode,
                            creatorName = "You",
                            imageDrawableRes = when (mode) {
                                GameMode.NORMAL_ELEVATOR -> R.drawable.img_normal_elevator
                                GameMode.OBBY -> R.drawable.img_world_obby
                                GameMode.SANDBOX -> R.drawable.img_world_sandbox
                                GameMode.LAVA_SURVIVAL -> R.drawable.img_world_lava
                                GameMode.BATTLE_ARENA -> R.drawable.img_world_obby
                            },
                            maxPlayers = entity.maxPlayers,
                            isCustom = true
                        )
                        onPlayWorld(def)
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
fun CustomWorldCard(
    world: CustomWorldEntity,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mode = try { GameMode.valueOf(world.gameMode) } catch (e: Exception) { GameMode.SANDBOX }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = world.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${mode.label} • Max ${world.maxPlayers} players",
                    fontSize = 12.sp,
                    color = Color(0xFF8B949E)
                )
            }

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("PLAY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
