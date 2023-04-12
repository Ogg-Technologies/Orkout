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
import androidx.compose.ui.platform.LocalContext
import com.oggtechnologies.orkout.model.database.DBView
import com.oggtechnologies.orkout.model.database.JsonDatabase
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch
import com.oggtechnologies.orkout.ui.screens.*
import com.oggtechnologies.orkout.ui.theme.OrkoutTheme
import com.oggtechnologies.orkout.ui.toast
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
    val context = LocalContext.current
    val screen = state.currentScreen
    OrkoutTheme {
        when (screen) {
            is Screen.Main -> MainScreen(state, dispatch)
            is Screen.WorkoutTemplates -> WorkoutTemplatesScreen(state, dispatch)
            is Screen.EditWorkoutTemplate -> {
                val template =
                    state.getWorkoutTemplate(screen.workoutTemplateId)
                if (template == null) ErrorScreen(dispatch) else EditWorkoutTemplateScreen(
                    template,
                    state,
                    dispatch
                )
            }
            is Screen.PickExerciseTemplateForWorkoutTemplate -> PickExerciseScreen(
                state,
                dispatch,
                onExercisePicked = { exerciseTemplate ->
                    val workoutTemplate = state.getWorkoutTemplate(screen.workoutTemplateId)!!
                    if (workoutTemplate.suggestedExercises.any { it.id == exerciseTemplate.id }) {
                        context.toast("${exerciseTemplate.name} is already added")
                    } else {
                        dispatch(
                            doAddSuggestedExercise(
                                workoutTemplate.id,
                                exerciseTemplate.id
                            )
                        )
                    }
                })
            is Screen.ExerciseTemplates -> ExerciseTemplatesScreen(state, dispatch)
            is Screen.EditExerciseTemplate -> EditExerciseTemplateScreen(
                screen,
                state,
                dispatch,
            )
            is Screen.WorkoutHistory -> WorkoutHistoryScreen(state, dispatch)
            is Screen.ActiveWorkout -> {
                val activeWorkout = state.activeWorkout
                if (activeWorkout == null) ErrorScreen(dispatch) else ActiveWorkoutScreen(
                    activeWorkout,
                    state,
                    dispatch,
                )
            }
            is Screen.PickExerciseInActiveWorkout -> PickExerciseScreen(
                state,
                dispatch,
                onExercisePicked = { dispatch(doStartExercise(state.activeWorkoutId!!, it)) })
            is Screen.EditExercise -> EditExerciseScreen(
                screen,
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