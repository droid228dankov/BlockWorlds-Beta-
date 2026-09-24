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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShopItem
import com.example.ui.components.AvatarPreview3D
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantEmerald
import com.example.ui.theme.VibrantAmber
import com.example.ui.viewmodel.BlockWorldsViewModel

@Composable
fun AvatarStudioScreen(
    viewModel: BlockWorldsViewModel,
    modifier: Modifier = Modifier
) {
    val avatar by viewModel.avatarState.collectAsState()
    val coins by viewModel.bloxCoins.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()

    var selectedCategory by remember { mutableStateOf("Headwear") }
    val categories = listOf("Headwear", "Outfits", "Pants", "Back Gear", "Colors")

    val categoryItems = remember(shopItems, selectedCategory) {
        shopItems.filter { it.category == selectedCategory }
    }

    val skinTones = listOf(
        Pair("Fair", 0xFFFFDBAC),
        Pair("Tan", 0xFFE0AC69),
        Pair("Golden", 0xFFFFD700),
        Pair("Deep", 0xFF8D5524),
        Pair("Alien Green", 0xFF00E676),
        Pair("Cyber Blue", 0xFF00E5FF)
    )

    val shirtColors = listOf(
        Pair("Cyber Blue", 0xFF2563EB),
        Pair("Crimson", 0xFFEF4444),
        Pair("Emerald", 0xFF10B981),
        Pair("Neon Purple", 0xFFA855F7),
        Pair("Golden Amber", 0xFFF59E0B),
        Pair("Pitch Black", 0xFF111827)
    )

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
                        text = "AVATAR STUDIO",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = NeonCyan
                    )
                    Text(
                        text = "Customize your 3D Block character",
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

        // 3D Avatar Viewer
        item {
            AvatarPreview3D(avatar = avatar)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Category Tabs
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

        // Colors palette view if "Colors" selected
        if (selectedCategory == "Colors") {
            item {
                Text(
                    text = "Skin Tone",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    skinTones.forEach { (name, hex) ->
                        val isSelected = avatar.skinColor == hex
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) NeonCyan else Color(0xFF30363D),
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.updateAvatar(avatar.copy(skinColor = hex))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (hex == 0xFFFFDBAC || hex == 0xFFFFD700) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Shirt & Torso Color",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    shirtColors.forEach { (name, hex) ->
                        val isSelected = avatar.shirtColor == hex
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) NeonCyan else Color(0xFF30363D),
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.updateAvatar(avatar.copy(shirtColor = hex))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Items for category
            items(categoryItems) { item ->
                val isEquipped = when (item.category) {
                    "Headwear" -> avatar.headItem == item.name
                    "Outfits" -> avatar.torsoItem == item.name
                    "Pants" -> avatar.pantsItem == item.name
                    "Back Gear" -> avatar.backItem == item.name
                    else -> false
                }

                ItemCardRow(
                    item = item,
                    isEquipped = isEquipped,
                    canAfford = coins >= item.costCoins,
                    onAction = { viewModel.buyOrEquipItem(item) },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
fun ItemCardRow(
    item: ShopItem,
    isEquipped: Boolean,
    canAfford: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) Color(0xFF1B2A38) else Color(0xFF161B22)
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isEquipped) NeonCyan else Color(0xFF30363D),
                RoundedCornerShape(14.dp)
            )
            .testTag("avatar_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon emoji
                Surface(
                    color = Color(0xFF21262D),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = item.iconEmoji, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (item.isUnlocked) (if (isEquipped) "Equipped ✓" else "Unlocked") else "🪙 ${item.costCoins} BloxCoins",
                        fontSize = 12.sp,
                        color = if (isEquipped) RadiantEmerald else if (item.isUnlocked) Color(0xFF8B949E) else VibrantAmber
                    )
                }
            }

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isEquipped -> Color(0xFF21262D)
                        item.isUnlocked -> NeonCyan
                        canAfford -> VibrantAmber
                        else -> Color(0xFF30363D)
                    }
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                enabled = item.isUnlocked || canAfford
            ) {
                Text(
                    text = when {
                        isEquipped -> "EQUIPPED"
                        item.isUnlocked -> "EQUIP"
                        canAfford -> "BUY"
                        else -> "LOCKED"
                    },
                    color = if (isEquipped || !item.isUnlocked && !canAfford) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
