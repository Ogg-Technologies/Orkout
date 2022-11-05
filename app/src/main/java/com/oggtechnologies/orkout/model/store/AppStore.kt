package com.oggtechnologies.orkout.model.store

import com.example.gshop.redux.*
import com.oggtechnologies.orkout.redux.Action
import com.oggtechnologies.orkout.redux.Store
import kotlinx.serialization.Serializable

val appStore = Store(
    initialState = State(),
    rootReducer = ::rootReducer,
    middlewares = listOf(
        loggerMiddleware,
        thunkMiddleware,
        persistentStorageMiddleware,
    )
)

@Serializable
data class State(
    val activeWorkout: Workout? = null,
    val navigationStack: NavigationStack = listOf(Screen.Main),
    val workoutHistory: List<Workout> = emptyList(),
    val exerciseTemplates: List<ExerciseTemplate> = emptyList(),
)

data class SetState(val state: State) : Action

data class AddWorkoutToHistory(val workout: Workout) : Action

data class AddOrUpdateExerciseTemplate(val exerciseTemplate: ExerciseTemplate) : Action

fun exerciseTemplateReducer(exerciseTemplates: List<ExerciseTemplate>, action: Action): List<ExerciseTemplate> = when (action) {
    is AddOrUpdateExerciseTemplate -> {
        val index = exerciseTemplates.indexOfFirst { it.id == action.exerciseTemplate.id }
        if (index == -1) {
            exerciseTemplates + action.exerciseTemplate
        } else {
            exerciseTemplates.toMutableList().apply { this[index] = action.exerciseTemplate }
        }
    }
    else -> exerciseTemplates
}

fun activeWorkoutReducer(activeWorkout: Workout?, action: ActiveWorkoutAction): Workout? {
    if (action is SetActiveWorkout) return action.workout
    if (activeWorkout == null) return null
    return activeWorkout.copy(exercises = exercisesReducer(activeWorkout.exercises, action))
}

fun exercisesReducer(exercises: List<Exercise>, action: ActiveWorkoutAction): List<Exercise> = when (action) {
    is AddExercise -> exercises + action.exercise
    is RemoveExercise -> exercises.remove(action.exerciseIndex)
    is AddSet -> exercises.edit(action.exerciseIndex) { it.copy(sets = it.sets + action.set) }
    is EditSet -> exercises.edit(action.exerciseIndex) { it.copy(sets = it.sets.set(action.setIndex, action.set)) }
    is RemoveSet -> exercises.edit(action.exerciseIndex) { it.copy(sets = it.sets.remove(action.set)) }
    else -> exercises
}

fun rootReducer(state: State, action: Action): State = when (action) {
    is SetState -> action.state
    is ActiveWorkoutAction -> state.copy(activeWorkout = activeWorkoutReducer(state.activeWorkout, action))
    is NavAction -> state.copy(
        navigationStack = navigationReducer(
            state.navigationStack,
            action
        )
    )
    is ScreenAction -> state.copy(navigationStack = state.navigationStack.editLast {
        screenReducer(it, action)
    })
    is AddWorkoutToHistory -> state.copy(workoutHistory = state.workoutHistory + action.workout)
    is AddOrUpdateExerciseTemplate -> state.copy(exerciseTemplates = exerciseTemplateReducer(state.exerciseTemplates, action))
    else -> state
}


