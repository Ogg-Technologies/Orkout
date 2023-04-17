package com.oggtechnologies.orkout.model.store

import com.oggtechnologies.orkout.model.database.DBView
import com.oggtechnologies.orkout.redux.Action
import com.oggtechnologies.orkout.redux.Thunk
import com.oggtechnologies.orkout.ui.screens.TimedExercise
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutTemplate(
    val name: String,
    val suggestedExercises: List<ExerciseTemplate>,
    val id: Int = generateId(),
)

@Serializable
data class ExerciseTemplate(
    val name: String,
    val fields: List<SetDataField>,
    val id: Int = generateId(),
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
        is SetDataField.Reps -> "reps"
        is SetDataField.Time -> "s"
        is SetDataField.Distance -> "m"
    }

data class Exercise(
    val id: Int,
    val templateId: Int,
    val sets: List<ExerciseSet>,
)

val Exercise?.template: ExerciseTemplate? get() = if (this == null) null else appStore.state.exerciseTemplates.find { it.id == templateId }
val Exercise?.name: String get() = this.template?.name ?: "Unknown exercise"

fun ExerciseTemplate.prettyPrintSet(set: ExerciseSet): String {
    return fields.map { field ->
        val data = when (field) {
            is SetDataField.Weight -> set.weight
            is SetDataField.Reps -> set.reps
            is SetDataField.Time -> set.time
            is SetDataField.Distance -> set.distance
        }
        "${field.name}: ${data ?: "unknown"} ${field.unit}"
    }.joinToString(", ")
}

data class ExerciseSet(
    val id: Int,
    val weight: Double? = null, // kg
    val reps: Int? = null, // nr
    val time: Int? = null, // s
    val distance: Double? = null, // m
)

data class Workout(
    val id: Int,
    val templateId: Int,
    val startTime: Long,
    val endTime: Long?,
    val exercises: List<Exercise>,
)

fun doAddWorkoutTemplate(workoutTemplate: WorkoutTemplate) = Thunk { _, _ ->
    DBView.addWorkoutTemplate(workoutTemplate)
}

fun doRemoveWorkoutTemplate(workoutTemplateId: Int) = Thunk { _, _ ->
    DBView.removeWorkoutTemplate(workoutTemplateId)
}

fun doRenameWorkoutTemplate(workoutTemplateId: Int, name: String) = Thunk { _, _ ->
    DBView.renameWorkoutTemplate(workoutTemplateId, name)
}

fun doAddSuggestedExercise(workoutTemplateId: Int, exerciseTemplateId: Int) = Thunk { _, _ ->
    DBView.addSuggestedExercise(workoutTemplateId, exerciseTemplateId)
}

fun doRemoveSuggestedExercise(workoutTemplateId: Int, exerciseTemplateId: Int) = Thunk { _, _ ->
    DBView.removeSuggestedExercise(workoutTemplateId, exerciseTemplateId)
}

fun doMoveSuggestedExercise(workoutTemplateId: Int, exerciseTemplateId: Int, newIndex: Int) =
    Thunk { _, _ ->
        DBView.moveSuggestedExercise(workoutTemplateId, exerciseTemplateId, newIndex)
    }

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

fun doFinishActiveWorkout() = Thunk { state, dispatch ->
    state.activeWorkout?.let { workout ->
        dispatch(SetActiveWorkoutId(null))
        DBView.setWorkoutTimes(workout.id, workout.startTime, System.currentTimeMillis())
    }
}

fun doAddWorkout(workout: Workout) = Thunk { _, _ ->
    DBView.addWorkout(workout)
}

fun doRemoveWorkout(workoutId: Int) = Thunk { _, _ ->
    DBView.removeWorkout(workoutId)
}

fun doSetWorkoutTemplateForWorkout(workoutId: Int, workoutTemplateId: Int?) = Thunk { _, _ ->
    DBView.setWorkoutTemplateForWorkout(workoutId, workoutTemplateId)
}

fun doAddExercise(workoutId: Int, exercise: Exercise) = Thunk { state, _ ->
    DBView.addExercise(workoutId, exercise)
}

fun doStartExercise(workoutId: Int, exerciseTemplate: ExerciseTemplate) = doAddExercise(
    workoutId,
    Exercise(
        id = generateId(),
        templateId = exerciseTemplate.id,
        sets = emptyList(),
    )
)

fun doRemoveExercise(exerciseId: Int) = Thunk { _, _ ->
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

fun doNewSet(exercise: Exercise) = Thunk { state, _ ->
    DBView.addSet(exercise.id, newSetFrom(exercise.sets))
}

fun doEditSet(exerciseId: Int, set: ExerciseSet) = Thunk { _, _ ->
    DBView.editSet(exerciseId, set)
}

fun doRemoveSet(setId: Int) = Thunk { _, _ ->
    DBView.removeSet(setId)
}

fun State.getExerciseTemplatesSortedByRecency(): List<ExerciseTemplate> {
    data class Recency(
        val exerciseTemplate: ExerciseTemplate,
        val lastWorkoutTime: Long,
        val indexInWorkout: Int
    )

    val recency = exerciseTemplates.map { exerciseTemplate ->
        for (workout in workoutHistory) { // Most recent workout appears first in list
            if (workout.endTime == null) continue
            for ((index, exercise) in workout.exercises.withIndex().reversed()) {
                if (exercise.templateId == exerciseTemplate.id) {
                    return@map Recency(exerciseTemplate, workout.endTime, index)
                }
            }
        }
        return@map Recency(exerciseTemplate, Long.MIN_VALUE, Int.MIN_VALUE)
    }
    return recency.sortedWith(
        compareByDescending<Recency> { it.lastWorkoutTime }
            .thenByDescending { it.indexInWorkout }
            .thenBy { it.exerciseTemplate.name }
    ).map { it.exerciseTemplate }
}

fun State.getTimedExerciseHistory(exerciseTemplate: ExerciseTemplate): List<TimedExercise> =
    workoutHistory
        .flatMap { workout ->
            workout.exercises
                .reversed()
                .filter { exercise -> exercise.sets.isNotEmpty() }
                .map { exercise ->
                    TimedExercise(exercise, workout.startTime.asMillisToLocalDateTime())
                }
        }
        .filter { it.exercise.templateId == exerciseTemplate.id }

fun ExerciseTemplate.hasWeightAndReps(): Boolean =
    SetDataField.Weight in fields && SetDataField.Reps in fields

fun ExerciseTemplate.fieldsToString() = fields.map { it.name.first() }.joinToString("/")
