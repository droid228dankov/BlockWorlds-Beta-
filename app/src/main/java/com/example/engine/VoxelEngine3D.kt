package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.AvatarCustomization
import com.example.model.BlockType
import com.example.model.ConnectedPlayer
import com.example.model.GameMode
import com.example.model.PlatformType
import com.example.model.VoxelBlock
import com.example.model.VoxelCoord
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class Camera3D(
    var x: Float = 0f,
    var y: Float = 3f,
    var z: Float = -6f,
    var pitch: Float = 25f, // degrees down
    var yaw: Float = 0f,    // degrees rotation around Y
    var distance: Float = 5.5f,
    var isFirstPerson: Boolean = false
)

data class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var color: Long,
    var life: Float = 1.0f,
    val maxLife: Float = 1.0f
)

data class VoxelCoin(
    val x: Float,
    val y: Float,
    val z: Float,
    var collected: Boolean = false,
    var rotAngle: Float = 0f
)

class VoxelEngine3D(
    val gameMode: GameMode,
    initialBlocks: Map<VoxelCoord, VoxelBlock>
) {
    val blocks = HashMap<VoxelCoord, VoxelBlock>(initialBlocks)
    val coinsList = mutableListOf<VoxelCoin>()

    // Local player state
    var playerX = 0f
    var playerY = 2f
    var playerZ = 0f
    var playerVx = 0f
    var playerVy = 0f
    var playerVz = 0f
    var playerYaw = 0f
    var isGrounded = false
    var isJumping = false
    var walkAnim = 0f
    var health = 100
    var score = 0
    var coinsCollected = 0
    var checkpointX = 0f
    var checkpointY = 2f
    var checkpointZ = 0f
    var isFinished = false
    var isGameOver = false
    var isFlyMode = false

    // Lava survival dynamic height
    var lavaHeight = -1f

    // The Normal Elevator States
    val elevatorFloors = listOf(
        com.example.model.ElevatorFloorInfo(0, "Lobby Floor", "Board the elevator! Doors closing soon!", "Safe", 12, 0xFF4CAF50, "Elevator Bossa"),
        com.example.model.ElevatorFloorInfo(1, "Floor 1: Lava Pit Survival", "The floor is hot molten lava! Leap on pillars!", "High", 20, 0xFFFF3D00, "Lava Rush"),
        com.example.model.ElevatorFloorInfo(2, "Floor 2: Alien Neon Disco", "Disco lights and raining BloxCoin bonuses!", "Safe", 20, 0xFFE91E63, "Funky Synth"),
        com.example.model.ElevatorFloorInfo(3, "Floor 3: Giant Noob Room", "A 20-foot classic Noob! Climb the giant arms!", "Medium", 20, 0xFFFFEB3B, "Noob March"),
        com.example.model.ElevatorFloorInfo(4, "Floor 4: Zero-G Moon Walk", "Low gravity! Leap 3x higher into diamond asteroids!", "Safe", 20, 0xFF00E5FF, "Cosmic Float"),
        com.example.model.ElevatorFloorInfo(5, "Floor 5: Laser Speedway", "Dodge the deadly lasers and grab the treasure!", "High", 20, 0xFFFF1744, "Speed Trap")
    )
    var elevatorFloorIndex = 0
    var elevatorState = com.example.model.ElevatorState.BOARDING
    var elevatorTimer = 12f
    var elevatorDoorsOpen = true
    var elevatorNotification = "🔔 DING! Doors closing in 12s..."

    val camera = Camera3D()
    val particles = mutableListOf<Particle3D>()

    // Targeted block for creative mode
    var targetedBlockCoord: VoxelCoord? = null
    var targetPlaceCoord: VoxelCoord? = null

    // Selected block to place in creative mode
    var activePlaceBlockType: BlockType = BlockType.GRASS

    init {
        // Find safe spawn
        if (gameMode == GameMode.NORMAL_ELEVATOR) {
            // Spawn inside the elevator cabin
            playerX = 0f
            playerY = 1.1f
            playerZ = -3.5f
            checkpointX = 0f
            checkpointY = 1.1f
            checkpointZ = -3.5f
        } else {
            playerX = 0f
            playerY = 2f
            playerZ = 0f
            checkpointX = playerX
            checkpointY = playerY
            checkpointZ = playerZ
        }

        // Populate 3D collectible coins in the world
        for ((coord, block) in blocks) {
            if (block.type == BlockType.GOLD || block.type == BlockType.DIAMOND ||
                block.type == BlockType.CHECKPOINT || (coord.x == 0 && coord.z % 6 == 0 && coord.z > 2)
            ) {
                val aboveCoord = VoxelCoord(coord.x, coord.y + 1, coord.z)
                if (!blocks.containsKey(aboveCoord)) {
                    coinsList.add(VoxelCoin(coord.x + 0.5f, coord.y + 1.4f, coord.z + 0.5f))
                }
            }
        }
    }

    fun resetCharacter() {
        health = 0
        addSpawnParticles(playerX, playerY, playerZ, 0xFFFF3D00)
        resetToCheckpoint()
    }

    fun resetToCheckpoint() {
        playerX = checkpointX
        playerY = checkpointY + 1f
        playerZ = checkpointZ
        playerVx = 0f
        playerVy = 0f
        playerVz = 0f
        health = 100
        addSpawnParticles(playerX, playerY, playerZ, 0xFF00E5FF)
    }

    fun updatePhysics(
        moveForward: Float,
        moveStrafe: Float,
        jumpRequested: Boolean,
        gravityVal: Float = 0.035f,
        jumpForceVal: Float = 0.38f
    ) {
        if (isGameOver || isFinished) return

        // Camera follow player
        val radYaw = (camera.yaw * PI / 180.0).toFloat()
        val radPitch = (camera.pitch * PI / 180.0).toFloat()

        if (camera.isFirstPerson) {
            camera.x = playerX
            camera.y = playerY + 1.2f
            camera.z = playerZ
        } else {
            val dist = camera.distance
            val camOffsetX = -sin(radYaw) * cos(radPitch) * dist
            val camOffsetY = sin(radPitch) * dist + 1.5f
            val camOffsetZ = -cos(radYaw) * cos(radPitch) * dist

            camera.x = playerX + camOffsetX
            camera.y = playerY + camOffsetY
            camera.z = playerZ + camOffsetZ
        }

        // Movement velocity calculation from input vector and camera yaw
        val speed = if (isFlyMode) 0.25f else 0.14f
        val cosY = cos(radYaw)
        val sinY = sin(radYaw)

        // Forward/back is along camera facing; strafe is perpendicular
        val dx = (sinY * moveForward + cosY * moveStrafe) * speed
        val dz = (cosY * moveForward - sinY * moveStrafe) * speed

        if (isFlyMode) {
            playerVx = dx
            playerVz = dz
            if (jumpRequested) playerVy = 0.2f else playerVy = 0f
            playerX += playerVx
            playerY += playerVy
            playerZ += playerVz
            return
        }

        // Horizontal movement with collision
        val newX = playerX + dx
        val newZ = playerZ + dz

        if (isBlockAt(newX, playerY, playerZ) == null && isBlockAt(newX, playerY + 0.9f, playerZ) == null) {
            playerX = newX
        }
        if (isBlockAt(playerX, playerY, newZ) == null && isBlockAt(playerX, playerY + 0.9f, newZ) == null) {
            playerZ = newZ
        }

        // Turning player facing move direction
        if (abs(dx) > 0.01f || abs(dz) > 0.01f) {
            walkAnim = (walkAnim + 0.4f) % (2f * PI.toFloat())
            playerYaw = (Math.toDegrees(kotlin.math.atan2(dx.toDouble(), dz.toDouble()))).toFloat()
        }

        // Gravity and Vertical Collision
        playerVy -= gravityVal

        val targetY = playerY + playerVy
        val blockUnder = isBlockAt(playerX, targetY, playerZ)

        if (blockUnder != null) {
            // Landed on block
            val blockTopY = blockUnder.y + 1f
            if (playerVy <= 0 && playerY >= blockTopY - 0.5f) {
                playerY = blockTopY
                playerVy = 0f
                isGrounded = true

                // Trigger special block effects
                handleBlockCollision(blockUnder)
            } else if (playerVy > 0) {
                // Hit head
                playerVy = 0f
            }
        } else {
            playerY = targetY
            isGrounded = false
        }

        // Jump Handling
        if (jumpRequested && isGrounded) {
            playerVy = jumpForceVal
            isGrounded = false
            addSpawnParticles(playerX, playerY, playerZ, 0xFFFFFFFF)
        }

        // Void Fall / Lava Hazard Check
        if (playerY < -8f || (gameMode == GameMode.LAVA_SURVIVAL && playerY < lavaHeight)) {
            resetToCheckpoint()
        }

        // Update rising lava in survival mode
        if (gameMode == GameMode.LAVA_SURVIVAL) {
            lavaHeight += 0.008f
        }

        // Update particles
        val pIt = particles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            p.x += p.vx
            p.y += p.vy
            p.z += p.vz
            p.life -= 0.04f
            if (p.life <= 0) {
                pIt.remove()
            }
        }

        // Update 3D Collectible Coins
        for (coin in coinsList) {
            if (!coin.collected) {
                coin.rotAngle = (coin.rotAngle + 4f) % 360f
                val dx = abs(playerX - coin.x)
                val dy = abs(playerY - coin.y)
                val dz = abs(playerZ - coin.z)
                if (dx < 0.95f && dy < 1.4f && dz < 0.95f) {
                    coin.collected = true
                    coinsCollected += 25
                    score += 100
                    addSpawnParticles(coin.x, coin.y, coin.z, 0xFFFFD700)
                }
            }
        }

        // Update targeted block
        updateTargetedBlock()

        // 4. Update The Normal Elevator Game Mode
        if (gameMode == GameMode.NORMAL_ELEVATOR) {
            elevatorTimer -= 0.016f
            when (elevatorState) {
                com.example.model.ElevatorState.BOARDING -> {
                    val s = elevatorTimer.coerceAtLeast(0f).toInt()
                    elevatorNotification = "Doors closing in ${s}s... Get ready!"
                    if (elevatorTimer <= 0) {
                        elevatorState = com.example.model.ElevatorState.TRANSIT
                        elevatorTimer = 5f
                        elevatorDoorsOpen = false
                        WorldGenerator.setElevatorDoors(blocks, open = false)
                        if (playerZ > -1f) {
                            playerX = 0f; playerY = 1.1f; playerZ = -3.5f
                            playerVx = 0f; playerVy = 0f; playerVz = 0f
                        }
                        elevatorNotification = "🔔 DING! Elevator traveling to next floor..."
                    }
                }
                com.example.model.ElevatorState.TRANSIT -> {
                    val s = elevatorTimer.coerceAtLeast(0f).toInt()
                    elevatorNotification = "Traveling between dimensions... (${s}s)"
                    if (elevatorTimer <= 0) {
                        elevatorFloorIndex = (elevatorFloorIndex + 1) % elevatorFloors.size
                        val currentFloor = elevatorFloors[elevatorFloorIndex]
                        WorldGenerator.generateElevatorFloor(blocks, elevatorFloorIndex)
                        // Repopulate floor coins
                        coinsList.clear()
                        for ((coord, blk) in blocks) {
                            if (coord.z >= 0 && (blk.type == BlockType.GOLD || blk.type == BlockType.DIAMOND || blk.type == BlockType.JUMP_PAD)) {
                                coinsList.add(VoxelCoin(coord.x + 0.5f, coord.y + 1.2f, coord.z + 0.5f))
                            }
                        }
                        WorldGenerator.setElevatorDoors(blocks, open = true)
                        elevatorDoorsOpen = true
                        elevatorState = com.example.model.ElevatorState.FLOOR_ACTIVE
                        elevatorTimer = currentFloor.durationSeconds.toFloat()
                        coinsCollected += 40
                        score += 200
                        elevatorNotification = "🔔 DING! ${currentFloor.title}!"
                        addSpawnParticles(0f, 2f, -1f, 0xFFFFD700)
                    }
                }
                com.example.model.ElevatorState.FLOOR_ACTIVE -> {
                    val s = elevatorTimer.coerceAtLeast(0f).toInt()
                    val floor = elevatorFloors[elevatorFloorIndex]
                    if (s <= 5) {
                        elevatorNotification = "⚠️ DOORS CLOSING in ${s}s! RUN TO ELEVATOR!"
                    } else {
                        elevatorNotification = "${floor.title} (${s}s left)"
                    }
                    if (elevatorTimer <= 0) {
                        elevatorState = com.example.model.ElevatorState.DOORS_CLOSING
                        elevatorTimer = 2.5f
                        WorldGenerator.setElevatorDoors(blocks, open = false)
                        elevatorDoorsOpen = false
                        elevatorNotification = "🚪 Doors closing! Floor survived!"
                    }
                }
                com.example.model.ElevatorState.DOORS_CLOSING -> {
                    if (elevatorTimer <= 0) {
                        if (playerZ > -1f) {
                            // Warp surviving player back into elevator safely
                            playerX = 0f; playerY = 1.1f; playerZ = -3.5f
                            playerVx = 0f; playerVy = 0f; playerVz = 0f
                        }
                        elevatorState = com.example.model.ElevatorState.TRANSIT
                        elevatorTimer = 5f
                        coinsCollected += 25
                        score += 150
                        elevatorNotification = "🔔 Floor survived! Next floor ascending..."
                    }
                }
                com.example.model.ElevatorState.DOORS_OPENING -> {}
            }
        }
    }

    private fun handleBlockCollision(block: VoxelBlock) {
        when {
            block.type.isHazard -> {
                // Stepped on lava
                health -= 35
                addSpawnParticles(playerX, playerY, playerZ, 0xFFFF3D00)
                if (health <= 0) {
                    resetToCheckpoint()
                }
            }
            block.type.bounce > 0 -> {
                // Super Jump Pad launch
                playerVy = 0.55f * block.type.bounce
                isGrounded = false
                addSpawnParticles(playerX, playerY, playerZ, 0xFFFFEA00)
            }
            block.type.isCheckpoint -> {
                // Checkpoint registered
                checkpointX = block.x.toFloat()
                checkpointY = (block.y + 1).toFloat()
                checkpointZ = block.z.toFloat()
                addSpawnParticles(checkpointX, checkpointY, checkpointZ, 0xFF00E676)
            }
            block.type.isFinish -> {
                // Reached victory portal!
                isFinished = true
                score += 500
                coinsCollected += 150
                addSpawnParticles(playerX, playerY + 1f, playerZ, 0xFFAA00FF)
            }
        }
    }

    private fun isBlockAt(wx: Float, wy: Float, wz: Float): VoxelBlock? {
        val bx = floor(wx).toInt()
        val by = floor(wy).toInt()
        val bz = floor(wz).toInt()
        return blocks[VoxelCoord(bx, by, bz)]
    }

    private fun updateTargetedBlock() {
        val radYaw = (camera.yaw * PI / 180.0).toFloat()
        val radPitch = (camera.pitch * PI / 180.0).toFloat()
        val dirX = sin(radYaw) * cos(radPitch)
        val dirY = -sin(radPitch)
        val dirZ = cos(radYaw) * cos(radPitch)

        var foundTarget: VoxelCoord? = null
        var placeTarget: VoxelCoord? = null

        var prevCoord: VoxelCoord? = null
        for (step in 1..25) {
            val dist = step * 0.2f
            val cx = floor(camera.x + dirX * dist).toInt()
            val cy = floor(camera.y + dirY * dist).toInt()
            val cz = floor(camera.z + dirZ * dist).toInt()
            val current = VoxelCoord(cx, cy, cz)

            if (blocks.containsKey(current)) {
                foundTarget = current
                placeTarget = prevCoord
                break
            }
            prevCoord = current
        }

        targetedBlockCoord = foundTarget
        targetPlaceCoord = placeTarget
    }

    fun breakTargetedBlock(): Boolean {
        val target = targetedBlockCoord ?: return false
        val removed = blocks.remove(target)
        if (removed != null) {
            addSpawnParticles(target.x + 0.5f, target.y + 0.5f, target.z + 0.5f, removed.type.topColor)
            return true
        }
        return false
    }

    fun placeBlockAtTarget(type: BlockType): Boolean {
        val target = targetPlaceCoord ?: return false
        // Don't place inside player body
        val pCoord = VoxelCoord(floor(playerX).toInt(), floor(playerY).toInt(), floor(playerZ).toInt())
        val pCoordHead = VoxelCoord(floor(playerX).toInt(), floor(playerY + 1f).toInt(), floor(playerZ).toInt())
        if (target == pCoord || target == pCoordHead) return false

        blocks[target] = VoxelBlock(target.x, target.y, target.z, type)
        addSpawnParticles(target.x + 0.5f, target.y + 0.5f, target.z + 0.5f, type.topColor)
        return true
    }

    fun addSpawnParticles(x: Float, y: Float, z: Float, color: Long) {
        val rng = Random
        for (i in 0..12) {
            particles.add(
                Particle3D(
                    x = x,
                    y = y,
                    z = z,
                    vx = (rng.nextFloat() - 0.5f) * 0.15f,
                    vy = rng.nextFloat() * 0.15f + 0.05f,
                    vz = (rng.nextFloat() - 0.5f) * 0.15f,
                    color = color,
                    life = 1.0f
                )
            )
        }
    }

    // Projection mathematics
    fun project3D(
        wx: Float,
        wy: Float,
        wz: Float,
        viewWidth: Float,
        viewHeight: Float
    ): Triple<Float, Float, Float>? {
        // Transform relative to camera
        val relX = wx - camera.x
        val relY = wy - camera.y
        val relZ = wz - camera.z

        val radYaw = (camera.yaw * PI / 180.0).toFloat()
        val radPitch = (camera.pitch * PI / 180.0).toFloat()

        val cosYaw = cos(radYaw)
        val sinYaw = sin(radYaw)
        val cosPitch = cos(radPitch)
        val sinPitch = sin(radPitch)

        // Rotate around Y (yaw)
        val x1 = relX * cosYaw - relZ * sinYaw
        val z1 = relX * sinYaw + relZ * cosYaw

        // Rotate around X (pitch)
        val y2 = relY * cosPitch - z1 * sinPitch
        val z2 = relY * sinPitch + z1 * cosPitch

        // Near plane clipping
        if (z2 < 0.5f) return null

        val fov = if (viewWidth > viewHeight) viewHeight * 0.96f else viewWidth * 0.90f
        val screenX = viewWidth / 2f + (x1 / z2) * fov
        val screenY = viewHeight * 0.50f - (y2 / z2) * fov

        return Triple(screenX, screenY, z2)
    }

    fun renderScene(
        drawScope: DrawScope,
        viewWidth: Float,
        viewHeight: Float,
        localAvatar: AvatarCustomization,
        localPlayerName: String,
        multiplayerPeers: List<ConnectedPlayer>
    ) {
        // Render sky & horizon
        drawSkybox(drawScope, viewWidth, viewHeight)

        // Render Rising Lava plane if in Lava Survival
        if (gameMode == GameMode.LAVA_SURVIVAL && lavaHeight > -2f) {
            renderLavaFloor(drawScope, viewWidth, viewHeight)
        }

        // Collect all 3D renderables and sort by depth (Painter's Algorithm)
        val renderList = mutableListOf<RenderItem>()

        // 1. Blocks within render distance of camera
        val maxDistSq = 35f * 35f
        for ((_, block) in blocks) {
            val distSq = (block.x - camera.x) * (block.x - camera.x) +
                         (block.y - camera.y) * (block.y - camera.y) +
                         (block.z - camera.z) * (block.z - camera.z)
            if (distSq <= maxDistSq) {
                renderList.add(RenderItem.BlockItem(block, distSq))
            }
        }

        // 1b. 3D Collectible Coins within render distance
        for (coin in coinsList) {
            if (!coin.collected) {
                val distSq = (coin.x - camera.x) * (coin.x - camera.x) +
                             (coin.y - camera.y) * (coin.y - camera.y) +
                             (coin.z - camera.z) * (coin.z - camera.z)
                if (distSq <= maxDistSq) {
                    renderList.add(RenderItem.CoinItem(coin, distSq))
                }
            }
        }

        // 2. Local Player Humanoid
        if (!camera.isFirstPerson) {
            val distSq = (playerX - camera.x) * (playerX - camera.x) +
                         (playerY - camera.y) * (playerY - camera.y) +
                         (playerZ - camera.z) * (playerZ - camera.z)
            renderList.add(
                RenderItem.PlayerItem(
                    id = "local",
                    name = localPlayerName,
                    x = playerX,
                    y = playerY,
                    z = playerZ,
                    yaw = playerYaw,
                    avatar = localAvatar,
                    walkAnim = walkAnim,
                    isLocal = true,
                    platform = PlatformType.ANDROID,
                    depth = distSq
                )
            )
        }

        // 3. Multiplayer Connected Peers
        for (peer in multiplayerPeers) {
            val distSq = (peer.x - camera.x) * (peer.x - camera.x) +
                         (peer.y - camera.y) * (peer.y - camera.y) +
                         (peer.z - camera.z) * (peer.z - camera.z)
            if (distSq <= maxDistSq) {
                renderList.add(
                    RenderItem.PlayerItem(
                        id = peer.id,
                        name = peer.name,
                        x = peer.x,
                        y = peer.y,
                        z = peer.z,
                        yaw = peer.yaw,
                        avatar = peer.avatar,
                        walkAnim = peer.animFrame,
                        isLocal = false,
                        platform = peer.platform,
                        depth = distSq
                    )
                )
            }
        }

        // Sort descending by depth: furthest rendered first
        renderList.sortByDescending { it.depth }

        // Draw sorted items
        for (item in renderList) {
            when (item) {
                is RenderItem.BlockItem -> drawVoxelBlock(drawScope, item.block, viewWidth, viewHeight)
                is RenderItem.CoinItem -> draw3DCoin(drawScope, item.coin, viewWidth, viewHeight)
                is RenderItem.PlayerItem -> drawHumanoidAvatar(drawScope, item, viewWidth, viewHeight)
            }
        }

        // Render 3D Particles
        drawParticles(drawScope, viewWidth, viewHeight)

        // Draw Targeted Block Highlight
        drawTargetHighlight(drawScope, viewWidth, viewHeight)
    }

    private fun drawSkybox(drawScope: DrawScope, w: Float, h: Float) {
        val skyTop = when (gameMode) {
            GameMode.NORMAL_ELEVATOR -> Color(0xFF161522)
            GameMode.OBBY -> Color(0xFF0D1B2A)
            GameMode.SANDBOX -> Color(0xFF1B263B)
            GameMode.LAVA_SURVIVAL -> Color(0xFF2A0800)
            GameMode.BATTLE_ARENA -> Color(0xFF120E2E)
        }
        val skyBottom = when (gameMode) {
            GameMode.NORMAL_ELEVATOR -> Color(0xFF322846)
            GameMode.OBBY -> Color(0xFF415A77)
            GameMode.SANDBOX -> Color(0xFF4FC3F7)
            GameMode.LAVA_SURVIVAL -> Color(0xFF7F1D1D)
            GameMode.BATTLE_ARENA -> Color(0xFF4A148C)
        }

        drawScope.drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(skyTop, skyBottom)
            ),
            size = androidx.compose.ui.geometry.Size(w, h)
        )
    }

    private fun renderLavaFloor(drawScope: DrawScope, w: Float, h: Float) {
        val p1 = project3D(-20f, lavaHeight, -20f, w, h)
        val p2 = project3D(20f, lavaHeight, -20f, w, h)
        val p3 = project3D(20f, lavaHeight, 60f, w, h)
        val p4 = project3D(-20f, lavaHeight, 60f, w, h)

        if (p1 != null && p2 != null && p3 != null && p4 != null) {
            val path = Path().apply {
                moveTo(p1.first, p1.second)
                lineTo(p2.first, p2.second)
                lineTo(p3.first, p3.second)
                lineTo(p4.first, p4.second)
                close()
            }
            drawScope.drawPath(path, Color(0xCCFF3D00), style = Fill)
        }
    }

    private fun drawVoxelBlock(drawScope: DrawScope, block: VoxelBlock, w: Float, h: Float) {
        val bx = block.x.toFloat()
        val by = block.y.toFloat()
        val bz = block.z.toFloat()

        // 8 vertices of cube
        val v000 = project3D(bx, by, bz, w, h) ?: return
        val v100 = project3D(bx + 1f, by, bz, w, h) ?: return
        val v110 = project3D(bx + 1f, by + 1f, bz, w, h) ?: return
        val v010 = project3D(bx, by + 1f, bz, w, h) ?: return
        val v001 = project3D(bx, by, bz + 1f, w, h) ?: return
        val v101 = project3D(bx + 1f, by, bz + 1f, w, h) ?: return
        val v111 = project3D(bx + 1f, by + 1f, bz + 1f, w, h) ?: return
        val v011 = project3D(bx, by + 1f, bz + 1f, w, h) ?: return

        val topColor = Color(block.type.topColor)
        val sideColor = Color(block.type.sideColor)
        val darkSideColor = Color(
            red = sideColor.red * 0.72f,
            green = sideColor.green * 0.72f,
            blue = sideColor.blue * 0.72f,
            alpha = sideColor.alpha
        )
        val lightSideColor = Color(
            red = sideColor.red * 0.88f,
            green = sideColor.green * 0.88f,
            blue = sideColor.blue * 0.88f,
            alpha = sideColor.alpha
        )

        // Top Face (010, 110, 111, 011) - visible if camera is above
        if (camera.y > by + 0.05f) {
            val topPath = Path().apply {
                moveTo(v010.first, v010.second)
                lineTo(v110.first, v110.second)
                lineTo(v111.first, v111.second)
                lineTo(v011.first, v011.second)
                close()
            }
            drawScope.drawPath(topPath, topColor)
            drawScope.drawPath(topPath, Color(0x33000000), style = Stroke(width = 1.2f))

            // Jump Pad symbol on top
            if (block.type == BlockType.JUMP_PAD) {
                val centerPt = project3D(bx + 0.5f, by + 1.02f, bz + 0.5f, w, h)
                if (centerPt != null) {
                    drawScope.drawCircle(
                        color = Color(0xFFFFD700),
                        radius = (120f / centerPt.third).coerceIn(4f, 20f),
                        center = Offset(centerPt.first, centerPt.second)
                    )
                }
            }
        }

        // Front Face (000, 100, 110, 010) - facing -Z (visible when camera is in front)
        if (camera.z < bz + 0.5f) {
            val frontPath = Path().apply {
                moveTo(v000.first, v000.second)
                lineTo(v100.first, v100.second)
                lineTo(v110.first, v110.second)
                lineTo(v010.first, v010.second)
                close()
            }
            drawScope.drawPath(frontPath, sideColor)
            drawScope.drawPath(frontPath, Color(0x33000000), style = Stroke(width = 1.2f))
        }

        // Back Face (001, 101, 111, 011) - facing +Z (visible when camera is behind)
        if (camera.z > bz + 0.5f) {
            val backPath = Path().apply {
                moveTo(v001.first, v001.second)
                lineTo(v101.first, v101.second)
                lineTo(v111.first, v111.second)
                lineTo(v011.first, v011.second)
                close()
            }
            drawScope.drawPath(backPath, sideColor)
            drawScope.drawPath(backPath, Color(0x33000000), style = Stroke(width = 1.2f))
        }

        // Left Face (000, 001, 011, 010) - facing -X (visible when camera is to the left)
        if (camera.x < bx + 0.5f) {
            val leftPath = Path().apply {
                moveTo(v000.first, v000.second)
                lineTo(v001.first, v001.second)
                lineTo(v011.first, v011.second)
                lineTo(v010.first, v010.second)
                close()
            }
            drawScope.drawPath(leftPath, lightSideColor)
            drawScope.drawPath(leftPath, Color(0x33000000), style = Stroke(width = 1.2f))
        }

        // Right Face (100, 101, 111, 110) - facing +X (visible when camera is to the right)
        if (camera.x > bx + 0.5f) {
            val rightPath = Path().apply {
                moveTo(v100.first, v100.second)
                lineTo(v101.first, v101.second)
                lineTo(v111.first, v111.second)
                lineTo(v110.first, v110.second)
                close()
            }
            drawScope.drawPath(rightPath, darkSideColor)
            drawScope.drawPath(rightPath, Color(0x33000000), style = Stroke(width = 1.2f))
        }
    }

    private fun draw3DCoin(drawScope: DrawScope, coin: VoxelCoin, w: Float, h: Float) {
        val center = project3D(coin.x, coin.y, coin.z, w, h) ?: return
        val rad = (coin.rotAngle * PI / 180.0).toFloat()
        val widthScale = abs(cos(rad)).coerceIn(0.18f, 1f)
        val coinRadius = (160f / center.third).coerceIn(8f, 32f)

        // Outer ambient glow
        drawScope.drawCircle(
            color = Color(0x44FFD700),
            radius = coinRadius * 1.4f,
            center = Offset(center.first, center.second)
        )

        // Coin ellipse
        drawScope.drawOval(
            color = Color(0xFFFFD700),
            topLeft = Offset(center.first - coinRadius * widthScale, center.second - coinRadius),
            size = androidx.compose.ui.geometry.Size(coinRadius * 2f * widthScale, coinRadius * 2f)
        )
        // Coin inner bevel
        drawScope.drawOval(
            color = Color(0xFFF59E0B),
            topLeft = Offset(center.first - coinRadius * widthScale * 0.72f, center.second - coinRadius * 0.72f),
            size = androidx.compose.ui.geometry.Size(coinRadius * 1.44f * widthScale, coinRadius * 1.44f)
        )
        // Coin rim border
        drawScope.drawOval(
            color = Color(0xFFB45309),
            topLeft = Offset(center.first - coinRadius * widthScale, center.second - coinRadius),
            size = androidx.compose.ui.geometry.Size(coinRadius * 2f * widthScale, coinRadius * 2f),
            style = Stroke(width = 1.6f)
        )
    }

    private fun drawHumanoidAvatar(
        drawScope: DrawScope,
        p: RenderItem.PlayerItem,
        w: Float,
        h: Float
    ) {
        val root = project3D(p.x, p.y, p.z, w, h) ?: return
        val headPt = project3D(p.x, p.y + 1.4f, p.z, w, h) ?: return
        val torsoPt = project3D(p.x, p.y + 0.8f, p.z, w, h) ?: return

        val scale = (420f / root.third).coerceIn(12f, 90f)

        // Limb swing animation based on walkAnim
        val armSwing = sin(p.walkAnim) * (scale * 0.25f)
        val legSwing = sin(p.walkAnim) * (scale * 0.28f)

        val skinCol = Color(p.avatar.skinColor)
        val shirtCol = Color(p.avatar.shirtColor)
        val pantsCol = Color(p.avatar.pantsColor)

        // Draw Legs
        val legW = scale * 0.22f
        val legH = scale * 0.45f
        // Left leg
        drawScope.drawRect(
            color = pantsCol,
            topLeft = Offset(root.first - legW - 2f, root.second - legH + legSwing),
            size = androidx.compose.ui.geometry.Size(legW, legH)
        )
        // Right leg
        drawScope.drawRect(
            color = pantsCol,
            topLeft = Offset(root.first + 2f, root.second - legH - legSwing),
            size = androidx.compose.ui.geometry.Size(legW, legH)
        )

        // Draw Torso (Blocky Roblox style)
        val torsoW = scale * 0.55f
        val torsoH = scale * 0.52f
        val torsoX = torsoPt.first - torsoW / 2f
        val torsoY = torsoPt.second - torsoH / 2f
        drawScope.drawRect(
            color = shirtCol,
            topLeft = Offset(torsoX, torsoY),
            size = androidx.compose.ui.geometry.Size(torsoW, torsoH)
        )
        drawScope.drawRect(
            color = Color(0x33000000),
            topLeft = Offset(torsoX, torsoY),
            size = androidx.compose.ui.geometry.Size(torsoW, torsoH),
            style = Stroke(width = 1.5f)
        )

        // Arms
        val armW = scale * 0.18f
        val armH = scale * 0.48f
        // Left arm
        drawScope.drawRect(
            color = skinCol,
            topLeft = Offset(torsoX - armW - 2f, torsoY + armSwing),
            size = androidx.compose.ui.geometry.Size(armW, armH)
        )
        // Right arm
        drawScope.drawRect(
            color = skinCol,
            topLeft = Offset(torsoX + torsoW + 2f, torsoY - armSwing),
            size = androidx.compose.ui.geometry.Size(armW, armH)
        )

        // Head (Square block head)
        val headSize = scale * 0.42f
        val headX = headPt.first - headSize / 2f
        val headY = headPt.second - headSize / 2f
        drawScope.drawRect(
            color = skinCol,
            topLeft = Offset(headX, headY),
            size = androidx.compose.ui.geometry.Size(headSize, headSize)
        )
        drawScope.drawRect(
            color = Color(0x33000000),
            topLeft = Offset(headX, headY),
            size = androidx.compose.ui.geometry.Size(headSize, headSize),
            style = Stroke(width = 1.5f)
        )

        // Headwear Accent (e.g. Visor / Crown / Cap)
        val hatColor = when {
            p.avatar.headItem.contains("Visor") -> Color(0xFF00E5FF)
            p.avatar.headItem.contains("Crown") -> Color(0xFFFFD700)
            p.avatar.headItem.contains("Ninja") -> Color(0xFF1E293B)
            else -> Color(0xFFEF4444)
        }
        drawScope.drawRect(
            color = hatColor,
            topLeft = Offset(headX - 2f, headY - 4f),
            size = androidx.compose.ui.geometry.Size(headSize + 4f, headSize * 0.35f)
        )

        // Face Sunglasses / Eyes
        val eyeW = headSize * 0.2f
        val eyeH = headSize * 0.15f
        val eyeY = headY + headSize * 0.4f
        drawScope.drawRect(
            color = Color.Black,
            topLeft = Offset(headX + headSize * 0.2f, eyeY),
            size = androidx.compose.ui.geometry.Size(eyeW, eyeH)
        )
        drawScope.drawRect(
            color = Color.Black,
            topLeft = Offset(headX + headSize * 0.6f, eyeY),
            size = androidx.compose.ui.geometry.Size(eyeW, eyeH)
        )

        // Floating Nametag with Cross-Platform Badge and Health Bar
        val nametagY = headY - 26f
        val badge = p.platform.icon
        val displayName = "$badge ${p.name}"

        drawScope.drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (scale * 0.22f).coerceIn(18f, 28f)
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
            }
            drawText(displayName, headPt.first, nametagY, paint)
        }

        // Mini health bar under nametag
        val barW = scale * 0.7f
        val barH = 5f
        val barX = headPt.first - barW / 2f
        drawScope.drawRect(
            color = Color(0x88000000),
            topLeft = Offset(barX, nametagY + 4f),
            size = androidx.compose.ui.geometry.Size(barW, barH)
        )
        drawScope.drawRect(
            color = Color(0xFF10B981),
            topLeft = Offset(barX, nametagY + 4f),
            size = androidx.compose.ui.geometry.Size(barW * (if (p.isLocal) health / 100f else 1f), barH)
        )
    }

    private fun drawParticles(drawScope: DrawScope, w: Float, h: Float) {
        for (p in particles) {
            val pt = project3D(p.x, p.y, p.z, w, h) ?: continue
            val radius = (18f / pt.third) * p.life
            drawScope.drawCircle(
                color = Color(p.color).copy(alpha = p.life),
                radius = radius.coerceIn(2f, 12f),
                center = Offset(pt.first, pt.second)
            )
        }
    }

    private fun drawTargetHighlight(drawScope: DrawScope, w: Float, h: Float) {
        val coord = targetedBlockCoord ?: return
        val bx = coord.x.toFloat()
        val by = coord.y.toFloat()
        val bz = coord.z.toFloat()

        val p1 = project3D(bx, by + 1.01f, bz, w, h) ?: return
        val p2 = project3D(bx + 1f, by + 1.01f, bz, w, h) ?: return
        val p3 = project3D(bx + 1f, by + 1.01f, bz + 1f, w, h) ?: return
        val p4 = project3D(bx, by + 1.01f, bz + 1f, w, h) ?: return

        val outline = Path().apply {
            moveTo(p1.first, p1.second)
            lineTo(p2.first, p2.second)
            lineTo(p3.first, p3.second)
            lineTo(p4.first, p4.second)
            close()
        }
        drawScope.drawPath(
            outline,
            Color(0xFF00E5FF),
            style = Stroke(width = 3.5f)
        )
    }
}

sealed class RenderItem(open val depth: Float) {
    data class BlockItem(val block: VoxelBlock, override val depth: Float) : RenderItem(depth)
    data class CoinItem(val coin: VoxelCoin, override val depth: Float) : RenderItem(depth)
    data class PlayerItem(
        val id: String,
        val name: String,
        val x: Float,
        val y: Float,
        val z: Float,
        val yaw: Float,
        val avatar: AvatarCustomization,
        val walkAnim: Float,
        val isLocal: Boolean,
        val platform: PlatformType,
        override val depth: Float
    ) : RenderItem(depth)
}
