package com.oggtechnologies.orkout.model.store

import com.oggtechnologies.orkout.model.database.DBView
import com.oggtechnologies.orkout.redux.*

val appStore = Store(
    initialState = State(),
    rootReducer = ::rootReducer,
    middlewares = listOf(
        loggerMiddleware,
        thunkMiddleware,
        persistentStorageMiddleware,
    )
)

data class State(
    val activeWorkoutId: Int? = null,
    val navigationStack: NavigationStack = listOf(Screen.Main),
    val workoutHistory: List<Workout> = emptyList(),
    val workoutTemplates: List<WorkoutTemplate> = emptyList(),
    val exerciseTemplates: List<ExerciseTemplate> = emptyList(),
)

val State.activeWorkout: Workout?
    get() = activeWorkoutId?.let { id -> workoutHistory.find { it.id == id } }

fun State.getWorkoutTemplate(id: Int): WorkoutTemplate? = workoutTemplates.find { it.id == id }


data class SetState(val state: State) : Action

fun doAddOrUpdateExerciseTemplate(exerciseTemplate: ExerciseTemplate) = Thunk { state, _ ->
    val trimmedName = exerciseTemplate.name.trim()
    val template = exerciseTemplate.copy(name = trimmedName)
    if (state.exerciseTemplates.any { it.id == template.id }) {
        DBView.updateExerciseTemplate(template)
    } else {
        DBView.addExerciseTemplate(template)
    }
}

data class SetExerciseTemplates(val exerciseTemplates: List<ExerciseTemplate>) : Action

data class SetWorkoutHistory(val workoutHistory: List<Workout>) : Action

data class SetWorkoutTemplates(val workoutTemplates: List<WorkoutTemplate>) : Action

fun rootReducer(state: State, action: Action): State = when (action) {
    is SetState -> action.state
    is SetExerciseTemplates -> state.copy(exerciseTemplates = action.exerciseTemplates)
    is SetWorkoutHistory -> state.copy(workoutHistory = action.workoutHistory)
    is SetWorkoutTemplates -> state.copy(workoutTemplates = action.workoutTemplates)
    is SetActiveWorkoutId -> state.copy(activeWorkoutId = action.id)
    is NavAction -> state.copy(
        navigationStack = navigationReducer(
            state.navigationStack,
            action
        )
    )
    is ScreenAction -> state.copy(navigationStack = state.navigationStack.editLast {
        screenReducer(it, action)
    })
    else -> state
}


