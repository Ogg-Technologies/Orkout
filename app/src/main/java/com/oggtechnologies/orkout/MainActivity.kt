package com.oggtechnologies.orkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.oggtechnologies.orkout.model.database.DBView
import com.oggtechnologies.orkout.model.database.JsonDatabase
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch
import com.oggtechnologies.orkout.ui.*
import com.oggtechnologies.orkout.ui.theme.OrkoutTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tryLoadSavedState()
        setupDBConnection()

        setContent {
            val state: State by appStore.stateFlow.collectAsState()
            OrkoutApp(state, appStore.dispatch)
        }
    }

    private fun setupDBConnection() {
        MainScope().launch {
            DBView.getExerciseTemplates().collect { exerciseTemplates ->
                appStore.dispatch(SetExerciseTemplates(exerciseTemplates))
            }
        }
        MainScope().launch {
            DBView.getWorkouts().collect { workouts ->
                appStore.dispatch(SetWorkoutHistory(workouts))
            }
        }
        MainScope().launch {
            DBView.getWorkoutTemplates().collect { workoutTemplates ->
                appStore.dispatch(SetWorkoutTemplates(workoutTemplates))
            }
        }
    }

    private fun tryLoadSavedState() {
        val savedState = JsonDatabase.readRelevantState() ?: return
        appStore.dispatch(SetState(savedState))
    }
}

@Composable
private fun OrkoutApp(state: State, dispatch: Dispatch) {
    OrkoutTheme {
        when (state.currentScreen) {
            is Screen.Main -> MainScreen(state, dispatch)
            is Screen.WorkoutTemplates -> WorkoutTemplatesScreen(state, dispatch)
            is Screen.EditWorkoutTemplate -> {
                val template =
                    state.getWorkoutTemplate((state.currentScreen as Screen.EditWorkoutTemplate).workoutTemplateId)
                if (template == null) ErrorScreen(dispatch) else EditWorkoutTemplateScreen(
                    template,
                    state,
                    dispatch
                )
            }
            is Screen.PickExerciseTemplateForWorkoutTemplate -> PickExerciseScreen(
                state,
                dispatch,
                onExercisePicked = {
                    dispatch(
                        doAddSuggestedExercise(
                            (state.currentScreen as Screen.PickExerciseTemplateForWorkoutTemplate).workoutTemplateId,
                            it.id
                        )
                    )
                })
            is Screen.ExerciseTemplates -> ExerciseTemplatesScreen(state, dispatch)
            is Screen.EditExerciseTemplate -> EditExerciseTemplateScreen(
                state.currentScreen as Screen.EditExerciseTemplate,
                state,
                dispatch,
            )
            is Screen.WorkoutHistory -> WorkoutHistoryScreen(state, dispatch)
            is Screen.ActiveWorkout -> if (state.activeWorkout == null) ErrorScreen(dispatch) else ActiveWorkoutScreen(
                state.activeWorkout!!,
                state,
                dispatch,
            )
            is Screen.PickExerciseInActiveWorkout -> PickExerciseScreen(
                state,
                dispatch,
                onExercisePicked = { dispatch(doStartExercise(state.activeWorkoutId!!, it)) })
            is Screen.EditExercise -> EditExerciseScreen(
                state.currentScreen as Screen.EditExercise,
                state,
                dispatch,
            )
        }
    }
}

@Composable
fun ErrorScreen(dispatch: Dispatch) {
    Scaffold {
        Column {
            Text("Error, invalid state")
            Button(onClick = { dispatch(doNavigateBack()) }) {
                Text("Go back")
            }
            Button(onClick = { dispatch(doNavigateHome()) }) {
                Text("Go home")
            }
        }
    }
}