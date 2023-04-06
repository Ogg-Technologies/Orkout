package com.oggtechnologies.orkout.model.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity
data class WorkoutTemplateEntity(
    @PrimaryKey val id: Int,
    val name: String
)

@Entity
data class ExerciseTemplateEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val hasWeight: Boolean,
    val hasReps: Boolean,
    val hasTime: Boolean,
    val hasDistance: Boolean,
)

@Entity(
    primaryKeys = ["exerciseTemplate", "workoutTemplate"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseTemplate"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutTemplate"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SuggestedExerciseEntity(
    val workoutTemplate: Int,
    val exerciseTemplate: Int,
    val listIndex: Int,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutTemplate"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class WorkoutEntity(
    @PrimaryKey val id: Int,
    val startTime: Long,
    val endTime: Long?,
    val workoutTemplate: Int?,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseTemplate"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val listIndex: Int,
    val workout: Int,
    val exerciseTemplate: Int,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SetEntity(
    @PrimaryKey val id: Int,
    val listIndex: Int,
    val exercise: Int,
    val weight: Double?,
    val reps: Int?,
    val time: Int?,
    val distance: Double?,
)

/**
 * Same as [SetEntity] but without the [SetEntity.listIndex] field.
 * Used to update sets without changing the index.
 */
data class SetWithoutIndex(
    val id: Int,
    val exercise: Int,
    val weight: Double?,
    val reps: Int?,
    val time: Int?,
    val distance: Double?,

    )

data class WorkoutTimes(
    val id: Int,
    val startTime: Long,
    val endTime: Long,
)

@Dao
interface AppDao {
    @Insert
    fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplateEntity)

    @Update
    fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplateEntity)

    @Query("Delete from WorkoutTemplateEntity where id = :id")
    fun deleteWorkoutTemplate(id: Int)

    @Insert
    fun insertSuggestedExercise(suggestedExercise: SuggestedExerciseEntity)

    @Query("SELECT MAX(listIndex)+1 FROM SuggestedExerciseEntity WHERE workoutTemplate = :workoutTemplateId")
    fun getNewSuggestedExerciseIndex(workoutTemplateId: Int): Int

    @Transaction
    fun insertSuggestedExerciseLast(workoutTemplateId: Int, exerciseTemplateId: Int) {
        val index = getNewSuggestedExerciseIndex(workoutTemplateId)
        insertSuggestedExercise(
            SuggestedExerciseEntity(
                workoutTemplateId,
                exerciseTemplateId,
                index
            )
        )
    }

    @Update
    fun updateSuggestedExercises(suggestedExercises: List<SuggestedExerciseEntity>)

    @Query("SELECT * FROM SuggestedExerciseEntity WHERE workoutTemplate = :workoutTemplateId ORDER BY listIndex")
    fun getSuggestedExercises(workoutTemplateId: Int): List<SuggestedExerciseEntity>

    @Transaction
    fun moveSuggestedExercise(suggestedExercise: SuggestedExerciseEntity) {
        val mutable =
            getSuggestedExercises(suggestedExercise.workoutTemplate).toMutableList()
        mutable.removeAll { it.exerciseTemplate == suggestedExercise.exerciseTemplate }
        mutable.add(suggestedExercise.listIndex, suggestedExercise)
        val newSuggestedExercises = mutable.mapIndexed { index, it -> it.copy(listIndex = index) }
        updateSuggestedExercises(newSuggestedExercises)
    }

    @Query("Delete from SuggestedExerciseEntity where workoutTemplate = :workoutTemplateId and exerciseTemplate = :exerciseTemplateId")
    fun deleteSuggestedExercise(workoutTemplateId: Int, exerciseTemplateId: Int)

    @Query("SELECT * FROM WorkoutTemplateEntity ORDER BY name")
    fun loadFullWorkoutTemplates(): Flow<List<FullWorkoutTemplate>>

    @Insert
    fun insertExerciseTemplate(exerciseTemplate: ExerciseTemplateEntity)

    @Update
    fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplateEntity)

    @Query("SELECT * FROM ExerciseTemplateEntity ORDER BY name")
    fun loadExerciseTemplates(): Flow<List<ExerciseTemplateEntity>>

    @Insert
    fun insertWorkout(workout: WorkoutEntity)

    @Insert
    fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT MAX(listIndex)+1 FROM ExerciseEntity WHERE workout = :workoutId")
    fun getNextExerciseIndex(workoutId: Int): Int

    @Insert
    fun insertSet(set: SetEntity)

    @Query("SELECT MAX(listIndex)+1 FROM SetEntity WHERE exercise = :exerciseId")
    fun getNextSetIndex(exerciseId: Int): Int

    @Query("DELETE FROM WorkoutEntity WHERE id = :id")
    fun deleteWorkout(id: Int)

    @Query("DELETE FROM ExerciseEntity WHERE id = :id")
    fun deleteExercise(id: Int)

    @Query("DELETE FROM SetEntity WHERE id = :id")
    fun deleteSet(id: Int)

    @Update(entity = WorkoutEntity::class)
    fun updateWorkoutTimes(workoutTimes: WorkoutTimes)

    @Query("UPDATE WorkoutEntity SET workoutTemplate = :workoutTemplateId WHERE id = :workoutId")
    fun updateWorkoutTemplateForWorkout(workoutId: Int, workoutTemplateId: Int?)

    @Update(entity = SetEntity::class)
    fun updateSet(set: SetWithoutIndex)

    @Transaction
    @Query("SELECT * FROM WorkoutEntity ORDER BY startTime DESC")
    fun loadFullWorkouts(): Flow<List<FullWorkout>>
}

data class FullWorkoutTemplate(
    @Embedded val workoutTemplate: WorkoutTemplateEntity,
    @Relation(
        entity = SuggestedExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutTemplate",
    )
    val fullSuggestedExercises: List<FullSuggestedExercise>
)

data class FullSuggestedExercise(
    @Embedded val suggestedExercise: SuggestedExerciseEntity,
    @Relation(
        parentColumn = "exerciseTemplate",
        entityColumn = "id"
    )
    val exerciseTemplate: ExerciseTemplateEntity
)

data class FullWorkout(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = ExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workout",
    )
    val fullExercises: List<FullExercise>,
    @Relation(
        entity = WorkoutTemplateEntity::class,
        parentColumn = "workoutTemplate",
        entityColumn = "id",
    )
    val fullWorkoutTemplate: FullWorkoutTemplate?
)

data class FullExercise(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exercise",
    )
    val sets: List<SetEntity>,
    @Relation(
        parentColumn = "exerciseTemplate",
        entityColumn = "id"
    )
    val exerciseTemplate: ExerciseTemplateEntity
)

@Database(
    entities = [
        WorkoutTemplateEntity::class,
        ExerciseTemplateEntity::class,
        SuggestedExerciseEntity::class,
        WorkoutEntity::class,
        ExerciseEntity::class,
        SetEntity::class
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 3, to = 4),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS SuggestedExerciseEntity")
        database.execSQL("CREATE TABLE IF NOT EXISTS SuggestedExerciseEntity (exerciseTemplate INTEGER NOT NULL REFERENCES ExerciseTemplateEntity(id) ON DELETE CASCADE, workoutTemplate INTEGER NOT NULL REFERENCES WorkoutTemplateEntity(id) ON DELETE CASCADE, listIndex INTEGER NOT NULL, PRIMARY KEY(exerciseTemplate, workoutTemplate))")
    }
}