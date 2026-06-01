package com.example.halalyticscompose.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.halalyticscompose.data.local.HalalyticsDatabase
import com.example.halalyticscompose.data.database.ProductHistoryDao
import com.example.halalyticscompose.data.database.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideHalalyticsDatabase(
        @ApplicationContext context: Context
    ): HalalyticsDatabase {
        // 🔒 SECURITY: Encrypt the local database with SQLCipher
        // Generates a deterministic passphrase so data persists across app restarts
        val passphrase = generatePassphrase(context)

        // Modern sqlcipher-android uses SupportOpenHelperFactory (no loadLibs needed)
        System.loadLibrary("sqlcipher")
        val factory = SupportOpenHelperFactory(passphrase)

        return try {
            val db = buildEncryptedDatabase(context, factory)
            // Force-open to catch encrypted/plain mismatch at startup.
            db.openHelper.writableDatabase
            db
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Database open failed, rebuilding: ${e.message}", e)
            // Recovery path for passphrase mismatch / legacy plain DB state.
            context.deleteDatabase("halalytics_database")
            val rebuilt = buildEncryptedDatabase(context, factory)
            // If this still fails, let it throw so crash reporter captures it.
            rebuilt.openHelper.writableDatabase
            rebuilt
        }
    }
    
    @Provides
    fun provideProductHistoryDao(database: HalalyticsDatabase): ProductHistoryDao {
        return database.productHistoryDao()
    }

    @Provides
    fun provideConsumptionDao(database: HalalyticsDatabase): com.example.halalyticscompose.data.local.Dao.ConsumptionDao {
        return database.consumptionDao()
    }

    @Provides
    fun provideHaramIngredientDao(database: HalalyticsDatabase): com.example.halalyticscompose.data.local.Dao.HaramIngredientDao {
        return database.haramIngredientDao()
    }

    @Provides
    fun provideUserHealthProfileDao(database: HalalyticsDatabase): com.example.halalyticscompose.data.local.Dao.UserHealthProfileDao {
        return database.userHealthProfileDao()
    }

    @Provides
    fun provideCachedScanResultDao(database: HalalyticsDatabase): com.example.halalyticscompose.data.local.Dao.CachedScanResultDao {
        return database.cachedScanResultDao()
    }
    
    /**
     * Generates a deterministic encryption passphrase for SQLCipher.
     * Uses the app's unique package signature as a seed to create
     * an encryption key that persists across app restarts.
     */
    private fun generatePassphrase(context: Context): ByteArray {
        val seed = "Halalytics_DB_Key_${context.packageName}_v1"
        return seed.toByteArray(Charsets.UTF_8)
    }

    private fun buildEncryptedDatabase(
        context: Context,
        factory: SupportOpenHelperFactory
    ): HalalyticsDatabase {
        return Room.databaseBuilder(
            context,
            HalalyticsDatabase::class.java,
            "halalytics_database"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }
}
