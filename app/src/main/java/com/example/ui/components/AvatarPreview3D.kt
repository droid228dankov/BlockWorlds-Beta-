package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AvatarCustomization
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AvatarPreview3D(
    avatar: AvatarCustomization,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rotationAngle = (rotationAngle + dragAmount.x * 0.8f) % 360f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f + 20f

            val rad = Math.toRadians(rotationAngle.toDouble())
            val cosRot = cos(rad).toFloat()
            val sinRot = sin(rad).toFloat()

            // Podium / Pedestal
            drawOval(
                color = Color(0x3300E5FF),
                topLeft = Offset(centerX - 80f, centerY + 65f),
                size = Size(160f, 40f)
            )
            drawOval(
                color = Color(0xFF00E5FF),
                topLeft = Offset(centerX - 70f, centerY + 70f),
                size = Size(140f, 30f),
                style = Stroke(width = 2f)
            )

            val skinColor = Color(avatar.skinColor)
            val shirtColor = Color(avatar.shirtColor)
            val pantsColor = Color(avatar.pantsColor)

            // Dynamic depth offset for limbs based on rotation
            val armOffsetX = cosRot * 35f
            val armDepth = sinRot * 20f

            // Legs
            val legW = 24f
            val legH = 50f
            // Left leg
            drawRect(
                color = pantsColor,
                topLeft = Offset(centerX - legW - 3f, centerY + 15f),
                size = Size(legW, legH)
            )
            // Right leg
            drawRect(
                color = pantsColor,
                topLeft = Offset(centerX + 3f, centerY + 15f),
                size = Size(legW, legH)
            )

            // Torso
            val torsoW = 60f
            val torsoH = 55f
            drawRect(
                color = shirtColor,
                topLeft = Offset(centerX - torsoW / 2f, centerY - 40f),
                size = Size(torsoW, torsoH)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(centerX - torsoW / 2f, centerY - 40f),
                size = Size(torsoW, torsoH),
                style = Stroke(width = 1.5f)
            )

            // Arms (rendered with rotation perspective)
            val armW = 18f
            val armH = 50f
            // Left arm
            drawRect(
                color = skinColor,
                topLeft = Offset(centerX - torsoW / 2f - armW + armOffsetX * 0.15f, centerY - 38f),
                size = Size(armW, armH)
            )
            // Right arm
            drawRect(
                color = skinColor,
                topLeft = Offset(centerX + torsoW / 2f - armOffsetX * 0.15f, centerY - 38f),
                size = Size(armW, armH)
            )

            // Head
            val headSize = 46f
            val headX = centerX - headSize / 2f
            val headY = centerY - 88f
            drawRect(
                color = skinColor,
                topLeft = Offset(headX, headY),
                size = Size(headSize, headSize)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(headX, headY),
                size = Size(headSize, headSize),
                style = Stroke(width = 1.5f)
            )

            // Face details (Sunglasses or eyes - changes position with rotation)
            val faceAlpha = (cosRot * 0.5f + 0.5f).coerceIn(0f, 1f)
            val eyeX = headX + (headSize / 2f) + (sinRot * 12f)
            if (faceAlpha > 0.1f) {
                drawRect(
                    color = Color.Black.copy(alpha = faceAlpha),
                    topLeft = Offset(eyeX - 14f, headY + 18f),
                    size = Size(10f, 7f)
                )
                drawRect(
                    color = Color.Black.copy(alpha = faceAlpha),
                    topLeft = Offset(eyeX + 4f, headY + 18f),
                    size = Size(10f, 7f)
                )
            }

            // Headwear Accent
            val hatColor = when {
                avatar.headItem.contains("Visor") -> Color(0xFF00E5FF)
                avatar.headItem.contains("Crown") -> Color(0xFFFFD700)
                avatar.headItem.contains("Ninja") -> Color(0xFF1E293B)
                else -> Color(0xFFEF4444)
            }
            drawRect(
                color = hatColor,
                topLeft = Offset(headX - 3f, headY - 4f),
                size = Size(headSize + 6f, 15f)
            )

            // Back Accessory (e.g. Katana / Jetpack / Wings)
            if (avatar.backItem.isNotBlank() && avatar.backItem != "None") {
                val accColor = if (avatar.backItem.contains("Katana")) Color(0xFFE2E8F0) else Color(0xFFFF9900)
                drawLine(
                    color = accColor,
                    start = Offset(centerX - 20f, centerY - 50f),
                    end = Offset(centerX + 25f, centerY - 10f),
                    strokeWidth = 5f
                )
            }
        }

        // 360 Drag hint label
        Text(
            text = "↻ Drag to rotate avatar in 3D",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
