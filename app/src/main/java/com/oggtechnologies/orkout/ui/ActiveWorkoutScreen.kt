package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gshop.redux.AsyncThunk
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch
import kotlinx.coroutines.delay
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
                    Text("Active Workout")
                },
                navigationIcon = {
                    BackButton(dispatch)
                },
                actions = {
                    SimpleStringOverflowMenu {
                        "Cancel Workout" does {
                            dispatch(AsyncThunk { _, _ ->
                                delay(SCREEN_CHANGE_DELAY)
                                dispatch(NavAction.Back)
                                dispatch(SetActiveWorkout(null))
                            })
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
                PerformedExercisesList(activeWorkout, state, dispatch, modifier = Modifier.height(0.dp).weight(1f))
                AddExerciseButton(dispatch)
                FinishWorkoutButton(activeWorkout, state, dispatch)
            }
        }
    )
}

@Composable
private fun PerformedExercisesList(activeWorkout: Workout, state: State, dispatch: Dispatch, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        itemsIndexedWithDividers(activeWorkout.exercises) { index, exercise ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().clickable {
                    dispatch(doNavigateTo(Screen.EditExercise(index)))
                }.padding(20.dp)
            ) {
                Text(exercise.name)
                Text("${exercise.sets.size} sets")
            }
        }
    }
}

@Composable
private fun AddExerciseButton(dispatch: Dispatch) {
    Button(
        onClick = {
            dispatch(doNavigateTo(Screen.PickExercise))
        }
    ) {
        Text(text = "Add Exercise")
    }
}

@Composable
private fun FinishWorkoutButton(activeWorkout: Workout, state: State, dispatch: Dispatch) {
    Button(
        onClick = {
            dispatch(AsyncThunk { _, _ ->
                delay(SCREEN_CHANGE_DELAY)
                dispatch(NavAction.Home)
                dispatch(AddWorkoutToHistory(activeWorkout))
                dispatch(SetActiveWorkout(null))
            })
        }
    ) {
        Text(text = "Finish Workout")
    }
}
