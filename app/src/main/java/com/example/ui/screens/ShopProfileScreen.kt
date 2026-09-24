package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AchievementItem
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantEmerald
import com.example.ui.theme.VibrantAmber
import com.example.ui.viewmodel.BlockWorldsViewModel

@Composable
fun ShopProfileScreen(
    viewModel: BlockWorldsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coins by viewModel.bloxCoins.collectAsState()
    val profile by viewModel.profileEntity.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    var dailyClaimed by remember { mutableStateOf(false) }

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
                        text = "PROFILE & REWARDS",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = NeonCyan
                    )
                    Text(
                        text = "Level, BloxCoins & Achievements",
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

        // Profile Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(2.dp, NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = NeonCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = profile?.username ?: "BlockBuilder_99",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF238636),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "LVL ${profile?.level ?: 8}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "3,400 / 5,000 XP",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8B949E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { 0.68f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = Color(0xFF21262D)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(title = "Obbys Won", value = "${profile?.obbysCompleted ?: 12}", icon = "🏁")
                        StatItem(title = "Blocks Placed", value = "${profile?.blocksPlaced ?: 430}", icon = "🧱")
                        StatItem(title = "Lava Escapes", value = "${profile?.survivalWins ?: 5}", icon = "🌋")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Daily Reward Chest
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F2C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VibrantAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = VibrantAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Daily Reward",
                                    tint = VibrantAmber,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Daily Login Chest",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (dailyClaimed) "Claimed for today! Come back tomorrow" else "+250 BloxCoins free reward",
                                fontSize = 12.sp,
                                color = if (dailyClaimed) RadiantEmerald else VibrantAmber
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (!dailyClaimed) {
                                viewModel.claimDailyCoins()
                                dailyClaimed = true
                                Toast.makeText(context, "Claimed 250 BloxCoins!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dailyClaimed) Color(0xFF21262D) else VibrantAmber
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !dailyClaimed,
                        modifier = Modifier.testTag("claim_daily_reward_button")
                    ) {
                        Text(
                            text = if (dailyClaimed) "CLAIMED" else "CLAIM",
                            color = if (dailyClaimed) Color(0xFF8B949E) else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // BloxCoins Store Promo Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(VibrantAmber, NeonCyan)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.openStore() }
                    .testTag("open_bloxcoins_store_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = VibrantAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🪙", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "GET BLOXCOINS",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Packs from 500 to 25,000 Coins",
                                fontSize = 11.sp,
                                color = VibrantAmber
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.openStore() },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantAmber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "BUY",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Achievements Section
        item {
            Text(
                text = "Badges & Achievements",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(achievements) { item ->
            AchievementCardItem(
                item = item,
                onClaim = { viewModel.claimAchievement(item.id) },
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
    }
}

@Composable
fun StatItem(title: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
        Text(text = title, fontSize = 11.sp, color = Color(0xFF8B949E))
    }
}

@Composable
fun AchievementCardItem(
    item: AchievementItem,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isComplete = item.progress >= item.target

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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color(0xFF21262D),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = item.emoji, fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        color = Color(0xFF8B949E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Reward: 🪙 ${item.rewardCoins} BloxCoins",
                        fontSize = 11.sp,
                        color = VibrantAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        item.isClaimed -> Color(0xFF21262D)
                        isComplete -> NeonCyan
                        else -> Color(0xFF30363D)
                    }
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                enabled = isComplete && !item.isClaimed
            ) {
                Text(
                    text = when {
                        item.isClaimed -> "CLAIMED ✓"
                        isComplete -> "CLAIM"
                        else -> "${item.progress}/${item.target}"
                    },
                    color = if (isComplete && !item.isClaimed) Color.Black else Color(0xFF8B949E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
