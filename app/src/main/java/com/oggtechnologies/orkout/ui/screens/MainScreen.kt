package com.oggtechnologies.orkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.AsyncThunk
import com.oggtechnologies.orkout.redux.Dispatch
import kotlinx.coroutines.delay

@Composable
fun MainScreen(state: State, dispatch: Dispatch) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Orkout")
                }
            )
        },
        content = { MainMenuList(state, dispatch) }
    )
}

@Composable
private fun MainMenuList(state: State, dispatch: Dispatch) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MainMenuButton(
            onClick = { dispatch(doNavigateTo(Screen.WorkoutTemplates)) }
        ) { Text(text = "Workout Templates") }
        MainMenuButton(
            onClick = { dispatch(doNavigateTo(Screen.ExerciseTemplates)) }
        ) { Text(text = "Exercise Templates") }
        MainMenuButton(
            onClick = { dispatch(doNavigateTo(Screen.WorkoutHistory)) }
        ) { Text(text = "Workout History") }
        Spacer(modifier = Modifier.weight(1f))
        StartButton(state.activeWorkout, dispatch)
        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun startWorkout(dispatch: Dispatch) {
    dispatch(AsyncThunk { _, _ ->
        delay(SCREEN_CHANGE_DELAY)
        dispatch(doStartWorkout())
        dispatch(NavAction.Goto(Screen.ActiveWorkout))
    })
}

@Composable
fun StartButton(activeWorkout: Workout?, dispatch: Dispatch) {
    val (text, onClick) = when (activeWorkout) {
        null -> "Start Workout" to { startWorkout(dispatch) }
        else -> "Continue Workout" to { dispatch(doNavigateTo(Screen.ActiveWorkout)) }
    }
    Button(
        onClick = onClick,
        content = { Text(text = text) },
        contentPadding = PaddingValues(60.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp)
    )
}

@Composable
private fun MainMenuButton(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        content = content,
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}