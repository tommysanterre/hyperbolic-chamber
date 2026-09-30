package ca.tommysanterre.snackloop

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "completions")
data class Completion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: String,
    val amount: Int,
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

@Database(entities = [Completion::class], version = 1, exportSchema = false)
abstract class SnackLoopDatabase : RoomDatabase() {
    abstract fun completionDao(): CompletionDao

    companion object {
        @Volatile private var instance: SnackLoopDatabase? = null
        fun get(context: Context): SnackLoopDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, SnackLoopDatabase::class.java, "snackloop.db")
                .build().also { instance = it }
        }
    }
}
