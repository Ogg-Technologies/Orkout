package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActiveWorkoutScreen(activeWorkout: Workout, state: State, dispatch: Dispatch) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Active Workout")
                },
                navigationIcon = {
                    BackButton(dispatch)
                },
                actions = {
                    SimpleStringOverflowMenu {
                        "Cancel Workout" does {
                            dispatch(doNavigateBack())
                            dispatch(SetActiveWorkout(null))
                        }
                    }
                }
            )
        },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Started at ${
                        SimpleDateFormat("HH:mm", Locale.ENGLISH).format(
                            activeWorkout.startTime
                        )
                    }"
                )
                PerformedExercisesList(activeWorkout, state, dispatch)
                FinishWorkoutButton(activeWorkout, state, dispatch)
            }
        }
    )
}

@Composable
private fun PerformedExercisesList(activeWorkout: Workout, state: State, dispatch: Dispatch) {
    LazyColumn {
        itemsWithDividers(activeWorkout.exercises) { exercise ->
            Text(text = exercise.name)
        }
    }
}

@Composable
private fun FinishWorkoutButton(activeWorkout: Workout, state: State, dispatch: Dispatch) {
    Button(
        onClick = {
            dispatch(doNavigateHome())
            dispatch(AddWorkoutToHistory(activeWorkout))
            dispatch(SetActiveWorkout(null))
        }
    ) {
        Text(text = "Finish Workout")
    }
}
