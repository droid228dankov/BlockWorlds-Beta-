package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GameMode
import com.example.model.WorldDefinition
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.VibrantAmber
import com.example.ui.viewmodel.BlockWorldsViewModel

@Composable
fun DiscoverScreen(
    viewModel: BlockWorldsViewModel,
    onPlayWorld: (WorldDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Parkour Obby 🌈", "Creative 🏰", "Survival 🌋", "PvP Battle ⚔️")
    val coins by viewModel.bloxCoins.collectAsState()

    val filteredWorlds = remember(selectedCategory) {
        when (selectedCategory) {
            "Parkour Obby 🌈" -> viewModel.featuredWorlds.filter { it.mode == GameMode.OBBY }
            "Creative 🏰" -> viewModel.featuredWorlds.filter { it.mode == GameMode.SANDBOX }
            "Survival 🌋" -> viewModel.featuredWorlds.filter { it.mode == GameMode.LAVA_SURVIVAL }
            "PvP Battle ⚔️" -> viewModel.featuredWorlds.filter { it.mode == GameMode.BATTLE_ARENA }
            else -> viewModel.featuredWorlds
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top App Bar / Currency Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BLOCKWORLDS",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = NeonCyan,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Cross-Platform Metaverse",
                        fontSize = 12.sp,
                        color = Color(0xFF8B949E)
                    )
                }

                // BloxCoins Interactive Buy Badge
                com.example.ui.components.BloxCoinBadge(
                    coins = coins,
                    onBuyClick = { viewModel.openStore() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(20.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_normal_elevator),
                        contentDescription = "The Normal Elevator Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD0D1117))
                                )
                            )
                    )

                    // Hero Content
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFFF9800),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "🔥 #1 TRENDING EXPERIENCE",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "👥 5.8M Plays • 48 Online",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🛗 The Normal Elevator",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onPlayWorld(viewModel.featuredWorlds[0]) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("hero_quick_play_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QUICK PLAY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending Worlds & Games",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${filteredWorlds.size} Worlds",
                    fontSize = 12.sp,
                    color = Color(0xFF8B949E)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Featured Worlds List
        items(filteredWorlds) { world ->
            WorldCardItem(
                world = world,
                onPlay = { onPlayWorld(world) },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun WorldCardItem(
    world: WorldDefinition,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(16.dp))
            .clickable { onPlay() }
            .testTag("world_card_${world.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = world.imageDrawableRes),
                    contentDescription = world.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Mode badge
                Surface(
                    color = Color(world.mode.badgeColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = world.mode.label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Cross-platform badge
                Surface(
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📱💻🌐🎮 Cross-Play",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = world.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = VibrantAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${world.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = world.description,
                    fontSize = 12.sp,
                    color = Color(0xFF8B949E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "by ${world.creatorName}",
                            fontSize = 11.sp,
                            color = Color(0xFF58A6FF)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "👥 ${world.currentOnline}/${world.maxPlayers}",
                            fontSize = 11.sp,
                            color = Color(0xFF3FB950)
                        )
                    }

                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("play_button_${world.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
