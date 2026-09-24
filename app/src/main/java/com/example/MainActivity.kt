package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AvatarStudioScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.MultiplayerHubScreen
import com.example.ui.screens.ShopProfileScreen
import com.example.ui.screens.WorldStudioScreen
import com.example.ui.theme.BlockWorldsTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.viewmodel.BlockWorldsViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BlockWorldsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockWorldsTheme {
                val activeWorld by viewModel.activeWorld.collectAsState()
                val gameEngine by viewModel.gameEngine.collectAsState()
                val currentTab by viewModel.currentTab.collectAsState()

                // Roblox Experience Flow:
                // Menu (Discover, Avatar, Studio, Hub) is vertical (Portrait).
                // 3D Gameplay experiences (The Normal Elevator, Obby, etc.) are horizontal (Landscape).
                LaunchedEffect(activeWorld) {
                    requestedOrientation = if (activeWorld != null) {
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }

                val isStoreOpen by viewModel.isStoreOpen.collectAsState()
                val bloxCoins by viewModel.bloxCoins.collectAsState()

                if (isStoreOpen) {
                    com.example.ui.components.BloxCoinsStoreDialog(
                        currentCoins = bloxCoins,
                        packages = viewModel.bloxCoinPackages,
                        onPurchase = { pkg ->
                            viewModel.purchaseCoins(pkg)
                        },
                        onDismiss = { viewModel.closeStore() }
                    )
                }

                if (activeWorld != null && gameEngine != null) {
                    // Fullscreen 3D Gameplay Mode
                    GamePlayScreen(
                        viewModel = viewModel,
                        world = activeWorld!!,
                        engine = gameEngine!!,
                        onExit = { viewModel.exitGame() }
                    )
                } else {
                    // Main Metaverse Lobby with Bottom Navigation
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color(0xFF0D1117),
                        bottomBar = {
                            MainBottomNavigation(
                                selectedTab = currentTab,
                                onSelectTab = { viewModel.currentTab.value = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                0 -> DiscoverScreen(
                                    viewModel = viewModel,
                                    onPlayWorld = { world -> viewModel.launchWorld(world) }
                                )
                                1 -> MultiplayerHubScreen(
                                    viewModel = viewModel,
                                    onLaunchRoom = { world, code -> viewModel.launchWorld(world, code) }
                                )
                                2 -> AvatarStudioScreen(
                                    viewModel = viewModel
                                )
                                3 -> WorldStudioScreen(
                                    viewModel = viewModel,
                                    onPlayWorld = { world -> viewModel.launchWorld(world) }
                                )
                                4 -> ShopProfileScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainBottomNavigation(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        color = Color(0xFF161B22),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF30363D))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            val navItems = listOf(
                NavigationItem("Discover", Icons.Default.Gamepad, "nav_discover"),
                NavigationItem("Multiplayer", Icons.Default.Public, "nav_multiplayer"),
                NavigationItem("Avatar", Icons.Default.Brush, "nav_avatar"),
                NavigationItem("Studio", Icons.Default.Build, "nav_studio"),
                NavigationItem("Profile", Icons.Default.EmojiEvents, "nav_profile")
            )

            navItems.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectTab(index) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = Color(0xFF8B949E),
                        unselectedTextColor = Color(0xFF8B949E)
                    ),
                    modifier = Modifier.testTag(item.tag)
                )
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
