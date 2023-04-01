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
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val index: Int,
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
    val index: Int,
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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExerciseTemplate(exerciseTemplate: ExerciseTemplateEntity)

    @Query("SELECT * FROM ExerciseTemplateEntity")
    fun loadExerciseTemplates(): Flow<List<ExerciseTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSet(set: SetEntity)

    @Delete
    fun deleteWorkout(workout: WorkoutEntity)

    @Delete
    fun deleteExercise(exercise: ExerciseEntity)

    @Delete
    fun deleteSet(set: SetEntity)

    @Update(entity = WorkoutEntity::class)
    fun updateWorkoutTimes(workoutTimes: WorkoutTimes)

    @Update
    fun updateSet(set: SetEntity)

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
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
