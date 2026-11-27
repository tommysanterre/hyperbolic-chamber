package ca.tommysanterre.snackloop

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "completions")
data class Completion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: String,
    val amount: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "strength_sessions")
data class StrengthSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: String,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val note: String = ""
)

@Entity(
    tableName = "strength_set_logs",
    indices = [Index("sessionId")]
)
data class StrengthSetLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val stepIndex: Int,
    val exerciseId: String,
    val setNumber: Int,
    val amount: Int,
    val setting: String,
    val completedAt: Long = System.currentTimeMillis()
)

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions ORDER BY completedAt DESC, id DESC")
    fun observeAll(): Flow<List<Completion>>

    @Insert suspend fun insert(completion: Completion)

    @Query("SELECT * FROM completions ORDER BY completedAt DESC, id DESC LIMIT 1")
    suspend fun last(): Completion?

    @Delete suspend fun delete(completion: Completion)
}

@Dao
interface StrengthDao {
    @Query("SELECT * FROM strength_sessions ORDER BY startedAt DESC, id DESC")
    fun observeSessions(): Flow<List<StrengthSession>>

    @Query("SELECT * FROM strength_set_logs ORDER BY completedAt ASC, id ASC")
    fun observeSetLogs(): Flow<List<StrengthSetLog>>

    @Insert suspend fun insertSession(session: StrengthSession): Long

    @Query("UPDATE strength_sessions SET finishedAt = :finishedAt, note = :note WHERE id = :sessionId")
    suspend fun finishSession(sessionId: Long, finishedAt: Long, note: String)

    @Query("DELETE FROM strength_set_logs WHERE sessionId = :sessionId")
    suspend fun deleteSetLogsForSession(sessionId: Long)

    @Query("DELETE FROM strength_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Transaction
    suspend fun abandonSession(sessionId: Long) {
        deleteSetLogsForSession(sessionId)
        deleteSessionById(sessionId)
    }

    @Insert suspend fun insertSetLog(log: StrengthSetLog)

    @Query("SELECT * FROM strength_set_logs WHERE sessionId = :sessionId ORDER BY stepIndex DESC, id DESC LIMIT 1")
    suspend fun lastSetLogForSession(sessionId: Long): StrengthSetLog?

    @Delete suspend fun deleteSetLog(log: StrengthSetLog)
}

@Database(
    entities = [Completion::class, StrengthSession::class, StrengthSetLog::class],
    version = SnackLoopDatabase.VERSION,
    exportSchema = false
)
abstract class SnackLoopDatabase : RoomDatabase() {
    abstract fun completionDao(): CompletionDao
    abstract fun strengthDao(): StrengthDao

    companion object {
        const val VERSION = 6

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `strength_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `templateId` TEXT NOT NULL,
                        `startedAt` INTEGER NOT NULL,
                        `finishedAt` INTEGER,
                        `note` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `strength_set_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `stepIndex` INTEGER NOT NULL,
                        `exerciseId` TEXT NOT NULL,
                        `setNumber` INTEGER NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `setting` TEXT NOT NULL,
                        `completedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_strength_set_logs_sessionId` ON `strength_set_logs` (`sessionId`)"
                )
            }
        }

        @Volatile private var instance: SnackLoopDatabase? = null
        fun get(context: Context): SnackLoopDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, SnackLoopDatabase::class.java, "snackloop.db")
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build().also { instance = it }
        }
    }
}
