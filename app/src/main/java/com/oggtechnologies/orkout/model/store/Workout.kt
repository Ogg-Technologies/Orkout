package com.oggtechnologies.orkout.model.store

import com.example.gshop.redux.Thunk
import com.oggtechnologies.orkout.model.database.DBView
import com.oggtechnologies.orkout.redux.Action
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseTemplate(
    val name: String,
    val id: Int,
    val fields: List<SetDataField>,
)

@Serializable
sealed class SetDataField {
    @Serializable
    object Weight : SetDataField()

    @Serializable
    object Reps : SetDataField()

    @Serializable
    object Time : SetDataField()

    @Serializable
    object Distance : SetDataField()
}

val setDataFields = listOf(
    SetDataField.Weight,
    SetDataField.Reps,
    SetDataField.Time,
    SetDataField.Distance,
)

val SetDataField.name
    get() = when (this) {
        is SetDataField.Weight -> "Weight"
        is SetDataField.Reps -> "Reps"
        is SetDataField.Time -> "Time"
        is SetDataField.Distance -> "Distance"
    }

val SetDataField.unit
    get() = when (this) {
        is SetDataField.Weight -> "kg"
        is SetDataField.Reps -> "nr"
        is SetDataField.Time -> "s"
        is SetDataField.Distance -> "m"
    }

val SetDataField.key
    get() = when (this) {
        is SetDataField.Weight -> "weight"
        is SetDataField.Reps -> "reps"
        is SetDataField.Time -> "time"
        is SetDataField.Distance -> "distance"
    }

@Serializable
data class WorkoutTemplate(
    val name: String,
    val id: Int,
    val suggestedExercises: List<ExerciseTemplate>,
)

@Serializable
data class Exercise(
    val id: Int,
    val templateId: Int,
    val sets: List<ExerciseSet>,
)

val Exercise?.template: ExerciseTemplate? get() = if (this == null) null else appStore.state.exerciseTemplates.find { it.id == templateId }
val Exercise?.name: String get() = this.template?.name ?: "Unknown exercise"

@Serializable
data class ExerciseSet(
    val id: Int,
    val weight: Double? = null, // kg
    val reps: Int? = null, // reps
    val time: Int? = null, // s
    val distance: Double? = null, // m
)

@Serializable
data class Workout(
    val id: Int,
    val templateId: Int,
    val startTime: Long,
    val endTime: Long?,
    val exercises: List<Exercise>,
)

fun doStartWorkout() = Thunk { state, dispatch ->
    val workout = Workout(
        id = generateId(),
        templateId = 0,
        startTime = System.currentTimeMillis(),
        endTime = null,
        exercises = emptyList(),
    )
    if (state.activeWorkout == null) {
        dispatch(doAddWorkout(workout))
        dispatch(SetActiveWorkoutId(workout.id))
    }
}

data class SetActiveWorkoutId(val id: Int?) : Action

sealed class WorkoutAction : Action

fun doAddWorkout(workout: Workout) = Thunk { _, _  ->
    DBView.addWorkout(workout)
}

fun doRemoveWorkout(workoutId: Int) = Thunk { _, _  ->
    DBView.removeWorkout(workoutId)
}

fun doAddExercise(workoutId: Int, exercise: Exercise) = Thunk { state, _  ->
    // TODO: refactor this to find the index some other way
    state.workoutHistory.find { it.id == workoutId }!!.let {workout ->
        DBView.addExercise(workoutId, workout.exercises.size, exercise)
    }
}

fun doStartExercise(workoutId: Int, exerciseTemplate: ExerciseTemplate) = doAddExercise(
    workoutId,
    Exercise(
        id = generateId(),
        templateId = exerciseTemplate.id,
        sets = emptyList(),
    )
)

fun doRemoveExercise(exerciseId: Int) = Thunk { _, _  ->
    DBView.removeExercise(exerciseId)
}

private fun newSetFrom(sets: List<ExerciseSet>): ExerciseSet {
    if (sets.isEmpty()) return ExerciseSet(generateId())
    val lastSet = sets.last()
    return ExerciseSet(
        id = generateId(),
        weight = lastSet.weight,
        reps = lastSet.reps,
        time = lastSet.time,
        distance = lastSet.distance,
    )
}

fun doNewSet(workoutId: Int, exerciseIndex: Int) = Thunk { state, _  ->
    state.workoutHistory.find { it.id == workoutId }!!.let {workout ->
        workout.exercises[exerciseIndex].let {exercise ->
            DBView.addSet(exercise.id, exercise.sets.size, newSetFrom(exercise.sets))
        }
    }
}

fun doEditSet(exerciseId: Int, setIndex: Int, set: ExerciseSet) = Thunk { _, _  ->
    DBView.editSet(exerciseId, setIndex, set)
}

fun doRemoveSet(setId: Int) = Thunk { _, _  ->
    DBView.removeSet(setId)
}

