package com.oggtechnologies.orkout.model.database

import androidx.room.*
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
    val exerciseTemplate: Int,
    val workoutTemplate: Int,
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExercise(exercise: ExerciseEntity)

    @Query("SELECT MAX(listIndex)+1 FROM ExerciseEntity WHERE workout = :workoutId")
    fun getNextExerciseIndex(workoutId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Update(entity = SetEntity::class)
    fun updateSet(set: SetWithoutIndex)

    @Transaction
    @Query("SELECT * FROM WorkoutEntity ORDER BY startTime DESC")
    fun loadFullWorkouts(): Flow<List<FullWorkout>>
}

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

data class FullWorkoutTemplate(
    @Embedded val workoutTemplate: WorkoutTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SuggestedExerciseEntity::class,
            parentColumn = "workoutTemplate",
            entityColumn = "exerciseTemplate"
        )
    )
    val suggestedExercises: List<ExerciseTemplateEntity>
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
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
