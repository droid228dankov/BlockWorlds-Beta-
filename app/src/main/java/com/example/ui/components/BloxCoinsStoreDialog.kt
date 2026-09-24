package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BloxCoinPackage
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantEmerald
import com.example.ui.theme.VibrantAmber
import kotlinx.coroutines.delay

@Composable
fun BloxCoinBadge(
    coins: Int,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF161B22),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantAmber.copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onBuyClick() }
            .testTag("bloxcoins_badge")
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🪙", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "%,d".format(coins),
                fontWeight = FontWeight.Bold,
                color = VibrantAmber,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(VibrantAmber),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Buy BloxCoins",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BloxCoinsStoreDialog(
    currentCoins: Int,
    packages: List<BloxCoinPackage>,
    onPurchase: (BloxCoinPackage) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPackage by remember { mutableStateOf<BloxCoinPackage?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var purchaseSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(900) // Realistic Google Play checkout simulation
            isProcessing = false
            purchaseSuccess = true
            selectedPackage?.let { onPurchase(it) }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) {
                onDismiss()
            }
        },
        containerColor = Color(0xFF161B22),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bloxcoins_store_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪙 BLOXCOINS STORE",
                        color = VibrantAmber,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF8B949E)
                    )
                }
            }
        },
        text = {
            if (purchaseSuccess) {
                // Success Confirmation Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = RadiantEmerald,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "PURCHASE SUCCESSFUL!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "+%,d BloxCoins added to your balance!".format(selectedPackage?.totalCoins ?: 0),
                        color = VibrantAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Transaction complete via Google Play. Enjoy your new block gear, avatars, and cosmetics!",
                        color = Color(0xFF8B949E),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            purchaseSuccess = false
                            selectedPackage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RadiantEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("AWESOME!", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (selectedPackage != null) {
                // Checkout Confirmation Sheet
                val pkg = selectedPackage!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Confirm In-App Purchase",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = pkg.iconEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = pkg.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "%,d BloxCoins".format(pkg.totalCoins),
                                        color = VibrantAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Text(
                                text = pkg.priceUsd,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Payment Method
                    Surface(
                        color = Color(0xFF0D1117),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = "Payment",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Google Play Billing", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("1-Tap Buy • Instant Delivery", color = Color(0xFF8B949E), fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isProcessing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Processing purchase...", color = Color.White, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = { isProcessing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("confirm_buy_button")
                        ) {
                            Text(
                                text = "1-TAP BUY (${pkg.priceUsd})",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { selectedPackage = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Choose Different Pack", color = Color(0xFF8B949E), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Package Catalog
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Current balance header & Trust badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current: 🪙 %,d".format(currentCoins),
                                color = VibrantAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Secure",
                                    tint = RadiantEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Secure Checkout",
                                    color = RadiantEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    items(packages) { pkg ->
                        PackageStoreItemCard(
                            pkg = pkg,
                            onClick = { selectedPackage = pkg }
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun PackageStoreItemCard(
    pkg: BloxCoinPackage,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF21262D)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (pkg.tag != null) Color(pkg.highlightColor).copy(alpha = 0.6f) else Color(0xFF30363D),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .testTag("package_${pkg.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color(0xFF161B22),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = pkg.iconEmoji, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pkg.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        if (pkg.tag != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(pkg.highlightColor),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = pkg.tag,
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🪙 %,d".format(pkg.coinsAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = VibrantAmber
                        )
                        if (pkg.bonusCoins > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+%,d Free!".format(pkg.bonusCoins),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = RadiantEmerald
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(pkg.highlightColor)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = pkg.priceUsd,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}
