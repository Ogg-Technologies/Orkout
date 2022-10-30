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

data class AddExerciseTemplate(val exerciseTemplate: ExerciseTemplate) : Action

fun rootReducer(state: State, action: Action): State = when (action) {
    is SetState -> action.state
    is SetActiveWorkout -> state.copy(activeWorkout = action.workout)
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
    is AddExerciseTemplate -> state.copy(exerciseTemplates = state.exerciseTemplates + action.exerciseTemplate)
    else -> state
}


