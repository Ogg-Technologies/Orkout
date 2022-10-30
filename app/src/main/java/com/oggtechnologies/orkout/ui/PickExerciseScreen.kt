package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import com.oggtechnologies.orkout.model.store.State
import com.oggtechnologies.orkout.model.store.Workout
import com.oggtechnologies.orkout.model.store.doNavigateBack
import com.oggtechnologies.orkout.redux.Dispatch

@Composable
fun PickExerciseScreen(state: State, dispatch: Dispatch) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Pick Exercise")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            Text(text = "Pick Exercise")
        }
    )
}