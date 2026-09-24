package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        CustomWorldEntity::class,
        ServerHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BlockWorldsDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun customWorldDao(): CustomWorldDao
    abstract fun serverHistoryDao(): ServerHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: BlockWorldsDatabase? = null

        fun getInstance(context: Context): BlockWorldsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlockWorldsDatabase::class.java,
                    "blockworlds.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
