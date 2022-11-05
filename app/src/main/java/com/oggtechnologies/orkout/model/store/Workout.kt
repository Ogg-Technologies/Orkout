package com.oggtechnologies.orkout.model.store

import com.example.gshop.redux.Thunk
import com.oggtechnologies.orkout.redux.Action
import kotlinx.serialization.Serializable

typealias ID = Int

@Serializable
data class ExerciseTemplate(
    val name: String,
    val id: ID,
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
        is SetDataField.Reps -> "reps"
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
    val id: ID,
    val suggestedExercises: List<ExerciseTemplate>,
)

@Serializable
data class Exercise(
    val templateId: ID,
    val sets: List<ExerciseSet>,
)

val Exercise?.template: ExerciseTemplate? get() = if (this == null) null else appStore.state.exerciseTemplates.find { it.id == templateId }
val Exercise?.name: String get() = this.template?.name ?: "Unknown exercise"

@Serializable
data class ExerciseSet(
    val weight: Double? = null,
    val reps: Int? = null,
    val time: Int? = null,
    val distance: Double? = null,
)

@Serializable
data class Workout(
    val templateId: ID,
    val startTime: Long,
    val endTime: Long?,
    val exercises: List<Exercise>,
)

fun doStartWorkout() = Thunk { state, dispatch ->
    val workout = Workout(
        templateId = 0,
        startTime = System.currentTimeMillis(),
        endTime = null,
        exercises = emptyList(),
    )
    if (state.activeWorkout == null) {
        dispatch(SetActiveWorkout(workout))
    }
}

sealed class ActiveWorkoutAction : Action

data class SetActiveWorkout(val workout: Workout?) : ActiveWorkoutAction()

data class AddExercise(val exercise: Exercise) : ActiveWorkoutAction()

fun doStartExercise(exerciseTemplate: ExerciseTemplate): ActiveWorkoutAction {
    val exercise = Exercise(
        templateId = exerciseTemplate.id,
        sets = emptyList(),
    )
    return AddExercise(exercise)
}

data class RemoveExercise(val exerciseIndex: Int) : ActiveWorkoutAction()

data class AddSet(val exerciseIndex: Int, val set: ExerciseSet) : ActiveWorkoutAction()

data class EditSet(val exerciseIndex: Int, val setIndex: Int, val set: ExerciseSet) :
    ActiveWorkoutAction()

data class RemoveSet(val exerciseIndex: Int, val set: Int) : ActiveWorkoutAction()

