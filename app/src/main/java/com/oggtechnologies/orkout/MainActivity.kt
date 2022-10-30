package com.oggtechnologies.orkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.oggtechnologies.orkout.model.Database
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch
import com.oggtechnologies.orkout.ui.*
import com.oggtechnologies.orkout.ui.theme.OrkoutTheme
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tryLoadSavedState()

        setContent {
            val state: State by appStore.stateFlow.collectAsState()
            OrkoutApp(state, appStore.dispatch)
        }
    }

    private fun tryLoadSavedState() {
        val savedJsonState = Database.readJsonState() ?: return
        try {
            val state = Json.decodeFromString(State.serializer(), savedJsonState)
            appStore.dispatch(SetState(state))
        } catch (e: Exception) {
        }
    }
}

@Composable
private fun OrkoutApp(state: State, dispatch: Dispatch) {
    OrkoutTheme {
        when (state.currentScreen) {
            is Screen.Main -> MainScreen(state, dispatch)
            is Screen.ExerciseTemplates -> ExerciseTemplatesScreen(state, dispatch)
            is Screen.EditExerciseTemplate -> EditExerciseTemplateScreen(
                state.currentScreen as Screen.EditExerciseTemplate,
                state,
                dispatch,
            )
            is Screen.WorkoutHistory -> WorkoutHistoryScreen(state, dispatch)
            is Screen.ActiveWorkout -> if (state.activeWorkout == null) ErrorScreen(dispatch) else ActiveWorkoutScreen(
                state.activeWorkout,
                state,
                dispatch,
            )
            is Screen.PickExercise -> PickExerciseScreen(state, dispatch)
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