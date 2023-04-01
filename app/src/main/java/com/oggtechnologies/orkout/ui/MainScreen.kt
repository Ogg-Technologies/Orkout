package com.oggtechnologies.orkout.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oggtechnologies.orkout.redux.AsyncThunk
import com.oggtechnologies.orkout.model.store.*
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
    LazyColumn {
        item {
            MainMenuButton(
                onClick = { dispatch(doNavigateTo(Screen.ExerciseTemplates)) }
            ) { Text(text = "Exercise Templates") }
            MainMenuButton(
                onClick = { dispatch(doNavigateTo(Screen.WorkoutHistory)) }
            ) { Text(text = "Workout History") }
            if (state.activeWorkout == null) {
                MainMenuButton(
                    onClick = { startWorkout(dispatch) }
                ) {
                    Text(text = "Start Workout")
                }
            } else {
                MainMenuButton(
                    onClick = { dispatch(doNavigateTo(Screen.ActiveWorkout)) }
                ) {
                    Text(text = "Continue Workout")
                }
            }
        }
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