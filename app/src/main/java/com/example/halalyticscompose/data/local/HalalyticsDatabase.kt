package com.example.halalyticscompose.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.halalyticscompose.data.local.Dao.CachedScanResultDao
import com.example.halalyticscompose.data.local.Dao.ConsumptionDao
import com.example.halalyticscompose.data.local.Dao.HaramIngredientDao
import com.example.halalyticscompose.data.local.Dao.UserHealthProfileDao
import com.example.halalyticscompose.data.local.Entities.CachedScanResult
import com.example.halalyticscompose.data.local.Entities.Consumption
import com.example.halalyticscompose.data.local.Entities.HaramIngredientEntity
import com.example.halalyticscompose.data.local.Entities.UserHealthProfileEntity
import com.example.halalyticscompose.data.database.ProductHistoryDao
import com.example.halalyticscompose.data.database.ProductHistoryEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        CachedScanResult::class, 
        Consumption::class, 
        ProductHistoryEntity::class,
        HaramIngredientEntity::class,
        UserHealthProfileEntity::class,
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HalalyticsDatabase : RoomDatabase() {
    
    abstract fun cachedScanResultDao(): CachedScanResultDao
    abstract fun consumptionDao(): ConsumptionDao
    abstract fun productHistoryDao(): ProductHistoryDao
    abstract fun haramIngredientDao(): HaramIngredientDao
    abstract fun userHealthProfileDao(): UserHealthProfileDao
    
    companion object {
        @Volatile
        private var INSTANCE: HalalyticsDatabase? = null
        
        fun getDatabase(context: Context): HalalyticsDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                // Modern sqlcipher-android: no loadLibs needed, use System.loadLibrary
                try {
                    System.loadLibrary("sqlcipher")
                } catch (e: UnsatisfiedLinkError) {
                    Log.e("HalalyticsDB", "Failed to load sqlcipher native library", e)
                }
                val passphrase = "Halalytics_DB_Key_${appContext.packageName}_v1"
                    .toByteArray(Charsets.UTF_8)
                val factory = SupportOpenHelperFactory(passphrase)
                val instance = try {
                    val db = Room.databaseBuilder(
                        appContext,
                        HalalyticsDatabase::class.java,
                        "halalytics_database"
                    )
                        .openHelperFactory(factory)
                        .fallbackToDestructiveMigration()
                        .build()
                    // Force-open to detect mismatch early and recover.
                    db.openHelper.writableDatabase
                    db
                } catch (e: Exception) {
                    Log.e("HalalyticsDB", "Database open failed, rebuilding", e)
                    // Recovery path for legacy plain/encrypted DB mismatch.
                    appContext.deleteDatabase("halalytics_database")
                    val rebuilt = Room.databaseBuilder(
                        appContext,
                        HalalyticsDatabase::class.java,
                        "halalytics_database"
                    )
                        .openHelperFactory(factory)
                        .fallbackToDestructiveMigration()
                        .build()
                    rebuilt.openHelper.writableDatabase
                    rebuilt
                }
                INSTANCE = instance
                instance
            }
        }
    }
}
