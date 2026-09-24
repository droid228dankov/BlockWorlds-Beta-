package com.example.engine

import com.example.model.BlockType
import com.example.model.GameMode
import com.example.model.VoxelBlock
import com.example.model.VoxelCoord
import kotlin.random.Random

object WorldGenerator {

    fun generateWorld(mode: GameMode, seed: Long = 42L): Map<VoxelCoord, VoxelBlock> {
        val blocks = mutableMapOf<VoxelCoord, VoxelBlock>()

        fun addBlock(x: Int, y: Int, z: Int, type: BlockType) {
            val coord = VoxelCoord(x, y, z)
            blocks[coord] = VoxelBlock(x, y, z, type)
        }

        fun fillBox(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, type: BlockType) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    for (z in minZ..maxZ) {
                        addBlock(x, y, z, type)
                    }
                }
            }
        }

        when (mode) {
            GameMode.NORMAL_ELEVATOR -> {
                // 1. Elevator Cabin (Safe Zone with metal walls, glass, lighting)
                // Floor (-3 to 3, Z = -6 to -1)
                for (x in -3..3) {
                    for (z in -6..-1) {
                        val tileType = if ((x + z) % 2 == 0) BlockType.STONE else BlockType.BRICK
                        addBlock(x, 0, z, tileType)
                    }
                }
                // Back wall (Z = -6)
                fillBox(-3, 1, -6, 3, 4, -6, BlockType.STONE)
                // Left wall (X = -3)
                fillBox(-3, 1, -5, -3, 4, -1, BlockType.STONE)
                // Right wall (X = 3)
                fillBox(3, 1, -5, 3, 4, -1, BlockType.STONE)
                // Ceiling with lights
                fillBox(-3, 5, -6, 3, 5, -1, BlockType.STONE)
                addBlock(0, 5, -4, BlockType.NEON_YELLOW)
                addBlock(0, 5, -2, BlockType.NEON_YELLOW)
                // Elevator Buttons & Display Panel
                addBlock(2, 2, -2, BlockType.NEON_CYAN)
                addBlock(2, 1, -2, BlockType.GOLD)
                // Elevator Door Frame (X = -3 to 3 at Z = -1)
                addBlock(-3, 1, -1, BlockType.STONE)
                addBlock(-3, 2, -1, BlockType.STONE)
                addBlock(-3, 3, -1, BlockType.STONE)
                addBlock(3, 1, -1, BlockType.STONE)
                addBlock(3, 2, -1, BlockType.STONE)
                addBlock(3, 3, -1, BlockType.STONE)
                fillBox(-3, 4, -1, 3, 4, -1, BlockType.STONE)
                // Door frame sign
                addBlock(0, 4, -1, BlockType.NEON_YELLOW)

                // 2. Initial Floor: Welcome Hall & Parkour Garden (Floor 0)
                generateElevatorFloor(blocks, floorIndex = 0)
            }

            GameMode.OBBY -> {
                // Spawn platform
                fillBox(-3, 0, -3, 3, 0, 3, BlockType.STONE)
                fillBox(-2, 0, -2, 2, 0, 2, BlockType.NEON_CYAN)

                // Rainbow stepping stones
                val rainbowColors = listOf(
                    BlockType.NEON_PINK,
                    BlockType.NEON_YELLOW,
                    BlockType.GRASS,
                    BlockType.NEON_CYAN,
                    BlockType.GOLD,
                    BlockType.DIAMOND
                )
                var currZ = 6
                var currY = 0
                for (i in 0 until 12) {
                    val color = rainbowColors[i % rainbowColors.size]
                    val xOffset = if (i % 2 == 0) -1 else 1
                    fillBox(xOffset - 1, currY, currZ, xOffset + 1, currY, currZ + 2, color)
                    currZ += 4
                    if (i % 3 == 0) currY += 1
                }

                // Checkpoint 1
                fillBox(-2, currY, currZ, 2, currY, currZ + 3, BlockType.CHECKPOINT)
                currZ += 5

                // Lava jump hazard section
                fillBox(-4, currY - 1, currZ, 4, currY - 1, currZ + 14, BlockType.LAVA)
                for (step in 0..3) {
                    val pX = if (step % 2 == 0) -2 else 2
                    fillBox(pX, currY, currZ + (step * 3) + 1, pX + 1, currY, currZ + (step * 3) + 2, BlockType.STONE)
                }
                currZ += 16

                // Super Jump Pad section
                addBlock(0, currY, currZ, BlockType.JUMP_PAD)
                addBlock(0, currY, currZ + 1, BlockType.JUMP_PAD)
                currZ += 5

                // Sky High Neon Island
                currY += 4
                fillBox(-3, currY, currZ, 3, currY, currZ + 6, BlockType.DIAMOND)
                currZ += 8

                // Narrow beam bridge
                for (b in 0..5) {
                    addBlock(0, currY, currZ + b, BlockType.NEON_PINK)
                }
                currZ += 8

                // Finish portal platform
                fillBox(-4, currY, currZ, 4, currY, currZ + 6, BlockType.GOLD)
                // Portal arch
                for (py in (currY + 1)..(currY + 4)) {
                    addBlock(-2, py, currZ + 3, BlockType.PORTAL)
                    addBlock(2, py, currZ + 3, BlockType.PORTAL)
                }
                for (px in -2..2) {
                    addBlock(px, currY + 5, currZ + 3, BlockType.PORTAL)
                }
            }

            GameMode.SANDBOX -> {
                // Wide creative terrain
                val radius = 10
                for (x in -radius..radius) {
                    for (z in -radius..radius) {
                        addBlock(x, 0, z, BlockType.GRASS)
                        addBlock(x, -1, z, BlockType.DIRT)
                    }
                }

                // Castle walls & towers
                fillBox(-6, 1, -6, -4, 4, -4, BlockType.BRICK) // Tower NW
                fillBox(4, 1, -6, 6, 4, -4, BlockType.BRICK)   // Tower NE
                fillBox(-6, 1, 4, -4, 4, 6, BlockType.BRICK)   // Tower SW
                fillBox(4, 1, 4, 6, 4, 6, BlockType.BRICK)     // Tower SE

                // Wall connections
                fillBox(-3, 1, -6, 3, 2, -6, BlockType.STONE)
                fillBox(-3, 1, 6, 3, 2, 6, BlockType.STONE)
                fillBox(-6, 1, -3, -6, 2, 3, BlockType.STONE)
                fillBox(6, 1, -3, 6, 2, 3, BlockType.STONE)

                // Central Golden Monument
                fillBox(-1, 1, -1, 1, 3, 1, BlockType.GOLD)
                addBlock(0, 4, 0, BlockType.DIAMOND)

                // Jump pads for fun parkour around castle
                addBlock(-5, 5, -5, BlockType.JUMP_PAD)
                addBlock(5, 5, -5, BlockType.JUMP_PAD)
                addBlock(0, 1, 5, BlockType.JUMP_PAD)
            }

            GameMode.LAVA_SURVIVAL -> {
                // Floor lava pit
                val rad = 8
                for (x in -rad..rad) {
                    for (z in -rad..rad) {
                        addBlock(x, 0, z, BlockType.LAVA)
                    }
                }

                // Rising stepped platforms
                val rng = Random(seed)
                for (tier in 1..8) {
                    val tierY = tier * 2
                    val count = (8 - tier).coerceAtLeast(2)
                    for (i in 0 until count) {
                        val px = rng.nextInt(-6, 7)
                        val pz = rng.nextInt(-6, 7)
                        val blockType = if (i == 0) BlockType.JUMP_PAD else if (tier == 8) BlockType.PORTAL else BlockType.STONE
                        fillBox(px, tierY, pz, px + 1, tierY, pz + 1, blockType)
                    }
                }
            }

            GameMode.BATTLE_ARENA -> {
                // Cyberpunk battle ring
                val rad = 9
                for (x in -rad..rad) {
                    for (z in -rad..rad) {
                        val dist = x * x + z * z
                        if (dist <= rad * rad) {
                            val color = if (dist % 4 == 0) BlockType.NEON_CYAN else BlockType.STONE
                            addBlock(x, 0, z, color)
                        }
                    }
                }

                // Arena obstacles and bounce pads
                addBlock(-4, 1, -4, BlockType.NEON_PINK)
                addBlock(-4, 2, -4, BlockType.JUMP_PAD)

                addBlock(4, 1, -4, BlockType.NEON_PINK)
                addBlock(4, 2, -4, BlockType.JUMP_PAD)

                addBlock(0, 1, 0, BlockType.GOLD)
                addBlock(0, 2, 0, BlockType.PORTAL)

                // Neon barriers
                for (x in -3..3) {
                    if (x != 0) {
                        addBlock(x, 1, -5, BlockType.NEON_YELLOW)
                        addBlock(x, 1, 5, BlockType.NEON_YELLOW)
                    }
                }
            }
        }

        return blocks
    }

    fun setElevatorDoors(blocks: MutableMap<VoxelCoord, VoxelBlock>, open: Boolean) {
        for (x in -2..2) {
            for (y in 1..3) {
                val coord = VoxelCoord(x, y, -1)
                if (open) {
                    blocks.remove(coord)
                } else {
                    blocks[coord] = VoxelBlock(x, y, -1, BlockType.STONE)
                }
            }
        }
    }

    fun generateElevatorFloor(blocks: MutableMap<VoxelCoord, VoxelBlock>, floorIndex: Int) {
        // Remove old floor blocks in outside chamber (Z >= 0)
        val toRemove = blocks.keys.filter { it.z >= 0 }
        toRemove.forEach { blocks.remove(it) }

        fun addBlock(x: Int, y: Int, z: Int, type: BlockType) {
            blocks[VoxelCoord(x, y, z)] = VoxelBlock(x, y, z, type)
        }

        fun fillBox(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, type: BlockType) {
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    for (z in minZ..maxZ) {
                        addBlock(x, y, z, type)
                    }
                }
            }
        }

        // Room perimeter walls for outside floor (Z = 0 to 18, X = -8 to 8, Y = 0 to 6)
        fillBox(-8, 1, 18, 8, 5, 18, BlockType.STONE) // Far back wall
        fillBox(-8, 1, 0, -4, 5, 0, BlockType.STONE)  // Front left
        fillBox(4, 1, 0, 8, 5, 0, BlockType.STONE)    // Front right
        fillBox(-8, 1, 0, -8, 5, 18, BlockType.STONE) // Left wall
        fillBox(8, 1, 0, 8, 5, 18, BlockType.STONE)   // Right wall

        when (floorIndex % 6) {
            0 -> {
                // Floor 0: Welcome Lobby
                fillBox(-7, 0, 0, 7, 0, 17, BlockType.GRASS)
                fillBox(-2, 0, 0, 2, 0, 16, BlockType.STONE)
                // Welcome fountain / statue
                fillBox(-1, 1, 7, 1, 2, 9, BlockType.GOLD)
                addBlock(0, 3, 8, BlockType.DIAMOND)
                addBlock(-4, 0, 6, BlockType.JUMP_PAD)
                addBlock(4, 0, 6, BlockType.JUMP_PAD)
            }
            1 -> {
                // Floor 1: Lava Floor - Don't Touch The Lava!
                fillBox(-7, 0, 0, 7, 0, 17, BlockType.LAVA)
                // Safe walkway at elevator entrance
                fillBox(-3, 0, 0, 3, 0, 2, BlockType.STONE)
                // Island stepping stones
                val islandCoords = listOf(
                    Triple(-2, 0, 5), Triple(2, 0, 6), Triple(0, 1, 8),
                    Triple(-3, 1, 11), Triple(3, 1, 12), Triple(0, 2, 14),
                    Triple(-1, 2, 16), Triple(1, 2, 16)
                )
                for (pt in islandCoords) {
                    fillBox(pt.first - 1, pt.second, pt.third - 1, pt.first + 1, pt.second, pt.third + 1, BlockType.STONE)
                    addBlock(pt.first, pt.second + 1, pt.third, BlockType.GOLD)
                }
                addBlock(0, 3, 16, BlockType.PORTAL)
            }
            2 -> {
                // Floor 2: Alien Neon Disco Rave
                val discoTiles = listOf(
                    BlockType.NEON_CYAN, BlockType.NEON_PINK,
                    BlockType.NEON_YELLOW, BlockType.DIAMOND
                )
                for (x in -7..7) {
                    for (z in 0..17) {
                        val tile = discoTiles[kotlin.math.abs((x * 3 + z * 2)) % discoTiles.size]
                        addBlock(x, 0, z, tile)
                    }
                }
                // Disco Pillars & Bounce Pads
                addBlock(-4, 1, 5, BlockType.JUMP_PAD)
                addBlock(4, 1, 5, BlockType.JUMP_PAD)
                addBlock(0, 1, 10, BlockType.JUMP_PAD)
                fillBox(-1, 5, 8, 1, 5, 10, BlockType.NEON_YELLOW) // Overhead disco ball
            }
            3 -> {
                // Floor 3: Giant Classic Blox Noob Room
                fillBox(-7, 0, 0, 7, 0, 17, BlockType.WOOD)
                // Giant Yellow Noob in center (Z = 9 to 11)
                // Legs (Green pants)
                fillBox(-2, 1, 9, -1, 3, 11, BlockType.GRASS)
                fillBox(1, 1, 9, 2, 3, 11, BlockType.GRASS)
                // Torso (Blue shirt)
                fillBox(-3, 4, 9, 3, 6, 11, BlockType.DIAMOND)
                // Arms (Yellow)
                fillBox(-5, 4, 9, -4, 6, 11, BlockType.GOLD)
                fillBox(4, 4, 9, 5, 6, 11, BlockType.GOLD)
                // Giant Head (Yellow block)
                fillBox(-2, 7, 9, 2, 9, 11, BlockType.NEON_YELLOW)
                // Face eyes
                addBlock(-1, 8, 8, BlockType.STONE)
                addBlock(1, 8, 8, BlockType.STONE)
                // Jump pads to parkour up the Giant Noob
                addBlock(-6, 0, 8, BlockType.JUMP_PAD)
                addBlock(6, 0, 8, BlockType.JUMP_PAD)
            }
            4 -> {
                // Floor 4: Zero Gravity Space Chamber
                fillBox(-7, 0, 0, 7, 0, 17, BlockType.ICE)
                // Floating asteroids
                fillBox(-4, 2, 4, -2, 2, 6, BlockType.STONE)
                fillBox(2, 3, 6, 4, 3, 8, BlockType.STONE)
                fillBox(-3, 4, 10, -1, 4, 12, BlockType.DIAMOND)
                fillBox(1, 4, 13, 3, 4, 15, BlockType.GOLD)
                addBlock(0, 1, 3, BlockType.JUMP_PAD)
                addBlock(0, 3, 9, BlockType.JUMP_PAD)
                addBlock(2, 5, 14, BlockType.PORTAL)
            }
            5 -> {
                // Floor 5: Laser Speedway & Golden Vault
                fillBox(-7, 0, 0, 7, 0, 17, BlockType.ICE)
                // Laser beam barriers (red glowing hazards)
                for (x in -5..5) {
                    addBlock(x, 1, 5, BlockType.LAVA)
                    addBlock(x, 1, 10, BlockType.LAVA)
                }
                // Safe step blocks
                addBlock(-2, 2, 5, BlockType.STONE)
                addBlock(2, 2, 10, BlockType.STONE)
                // Giant golden vault at the end
                fillBox(-3, 1, 14, 3, 3, 16, BlockType.GOLD)
                addBlock(0, 4, 15, BlockType.PORTAL)
            }
        }
    }
}
