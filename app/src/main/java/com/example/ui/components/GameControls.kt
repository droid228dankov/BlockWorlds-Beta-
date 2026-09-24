package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockType
import com.example.model.GameMode
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.VibrantAmber
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    onMove: (forward: Float, strafe: Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(IntOffset.Zero) }
    val maxRadiusPx = 130f

    Box(
        modifier = modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(Color(0x66161B22))
            .border(2.dp, Color(0x6600E5FF), CircleShape)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        thumbOffset = IntOffset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = IntOffset.Zero
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = thumbOffset.x + dragAmount.x
                        val newY = thumbOffset.y + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)

                        if (dist > maxRadiusPx) {
                            val ratio = maxRadiusPx / dist
                            thumbOffset = IntOffset((newX * ratio).toInt(), (newY * ratio).toInt())
                        } else {
                            thumbOffset = IntOffset(newX.toInt(), newY.toInt())
                        }

                        // Forward is negative Y in screen coordinates, strafe is X
                        val normForward = (-thumbOffset.y / maxRadiusPx).coerceIn(-1f, 1f)
                        val normStrafe = (thumbOffset.x / maxRadiusPx).coerceIn(-1f, 1f)
                        onMove(normForward, normStrafe)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Center stick thumb
        Box(
            modifier = Modifier
                .offset { thumbOffset }
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan, Color(0xFF0097A7))
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
    }
}

@Composable
fun CameraTouchLookZone(
    modifier: Modifier = Modifier,
    onRotate: (deltaYaw: Float, deltaPitch: Float) -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Dragging right increases yaw (rotates camera right)
                    // Dragging down increases pitch
                    val dYaw = dragAmount.x * 0.35f
                    val dPitch = dragAmount.y * 0.35f
                    onRotate(dYaw, dPitch)
                }
            }
    )
}

@Composable
fun JumpActionButton(
    isGrounded: Boolean,
    onJump: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (isGrounded) NeonCyan else Color(0xFF334155),
                        if (isGrounded) Color(0xFF0284C7) else Color(0xFF1E293B)
                    )
                )
            )
            .border(2.dp, if (isGrounded) Color.White else Color(0x66FFFFFF), CircleShape)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onJump()
            }
            .testTag("jump_button"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Jump",
                tint = if (isGrounded) Color.Black else Color.White,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = "JUMP",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = if (isGrounded) Color.Black else Color.White
            )
        }
    }
}

@Composable
fun CreativeModeHotbar(
    selectedBlock: BlockType,
    onSelectBlock: (BlockType) -> Unit,
    onMine: () -> Unit,
    onPlace: () -> Unit,
    isFlyMode: Boolean,
    onToggleFly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val palette = listOf(
        BlockType.GRASS,
        BlockType.STONE,
        BlockType.BRICK,
        BlockType.WOOD,
        BlockType.GOLD,
        BlockType.DIAMOND,
        BlockType.NEON_CYAN,
        BlockType.NEON_PINK,
        BlockType.JUMP_PAD,
        BlockType.PORTAL,
        BlockType.LAVA,
        BlockType.TNT
    )

    Surface(
        color = Color(0xDD111827),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151)),
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mine button
                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onMine()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.8f))
                            .testTag("mine_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Mine Block",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Place button
                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onPlace()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.85f))
                            .testTag("place_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Place Block",
                            tint = Color.Black
                        )
                    }
                }

                // Fly mode toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFlyMode) Color(0xFF10B981) else Color(0xFF374151))
                        .clickable { onToggleFly() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = "Fly Mode",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFlyMode) "FLYING" else "WALK",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Block selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(palette) { block ->
                    val isSelected = block == selectedBlock
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(block.topColor))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSelectBlock(block)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}
