package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import com.oggtechnologies.orkout.model.store.State
import com.oggtechnologies.orkout.model.store.doNavigateBack
import com.oggtechnologies.orkout.redux.Dispatch

@Composable
fun WorkoutHistoryScreen(state: State, dispatch: Dispatch) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Workout History")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            LazyColumn {
                itemsWithDividers(state.workoutHistory) { workout ->
                    Text(text = workout.toString())
                }
            }
        }
    )
}