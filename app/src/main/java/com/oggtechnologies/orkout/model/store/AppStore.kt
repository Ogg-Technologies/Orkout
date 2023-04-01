package com.oggtechnologies.orkout.model.store

import com.example.gshop.redux.*
import com.oggtechnologies.orkout.model.database.DBView
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
    val activeWorkoutId: Int? = null,
    val navigationStack: NavigationStack = listOf(Screen.Main),
    val workoutHistory: List<Workout> = emptyList(),
    val exerciseTemplates: List<ExerciseTemplate> = emptyList(),
)

val State.activeWorkout: Workout?
    get() = activeWorkoutId?.let { id -> workoutHistory.find { it.id == id } }


data class SetState(val state: State) : Action

data class AddWorkoutToHistory(val workout: Workout) : Action

fun doAddOrUpdateExerciseTemplate(exerciseTemplate: ExerciseTemplate) = Thunk { _, _ ->
    DBView.addOrUpdateExerciseTemplate(exerciseTemplate)
}

data class SetExerciseTemplates(val exerciseTemplates: List<ExerciseTemplate>) : Action

data class SetWorkoutHistory(val workoutHistory: List<Workout>) : Action

/*
fun activeWorkoutReducer(activeWorkout: Workout?, action: WorkoutAction): Workout? {
    if (action is SetActiveWorkout) return action.workout
    if (activeWorkout == null) return null
    return activeWorkout.copy(exercises = exercisesReducer(activeWorkout.exercises, action))
}
 */

/*
fun exercisesReducer(exercises: List<Exercise>, action: WorkoutAction): List<Exercise> = when (action) {
    is AddExercise -> exercises + action.exercise
    is RemoveExercise -> exercises.remove(action.exerciseIndex)
    is NewSet -> exercises.edit(action.exerciseIndex) { it.copy(sets = it.sets + newSetFrom(it.sets)) }
    is EditSet -> exercises.edit(action.exerciseIndex) { it.copy(sets = it.sets.set(action.setIndex, action.set)) }
    is RemoveSet -> exercises.edit(action.exerciseIndex) { it.copy(sets = it.sets.remove(action.set)) }
    else -> exercises
}
 */

fun rootReducer(state: State, action: Action): State = when (action) {
    is SetState -> action.state
    is SetExerciseTemplates -> state.copy(exerciseTemplates = action.exerciseTemplates)
    is SetWorkoutHistory -> state.copy(workoutHistory = action.workoutHistory)
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
    //is AddWorkoutToHistory -> state.copy(workoutHistory = state.workoutHistory + action.workout)
    else -> state
}


