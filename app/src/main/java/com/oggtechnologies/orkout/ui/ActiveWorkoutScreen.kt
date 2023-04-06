package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.model.store.State
import com.oggtechnologies.orkout.redux.AsyncThunk
import com.oggtechnologies.orkout.redux.Dispatch
import kotlinx.coroutines.delay

@Composable
fun ActiveWorkoutScreen(activeWorkout: Workout, state: State, dispatch: Dispatch) =
    ConfirmationHandler {
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
                                showConfirmDialog("Are you sure you want to cancel the workout?") {
                                    dispatch(AsyncThunk { _, _ ->
                                        delay(SCREEN_CHANGE_DELAY)
                                        dispatch(NavAction.Back)
                                        dispatch(doRemoveWorkout(activeWorkout.id))
                                        dispatch(SetActiveWorkoutId(null))
                                    })
                                }
                            }
                            "Finish Workout" does {
                                showConfirmDialog("Are you sure you want to finish?") {
                                    dispatch(AsyncThunk { _, _ ->
                                        delay(SCREEN_CHANGE_DELAY)
                                        dispatch(NavAction.Home)
                                        dispatch(doFinishActiveWorkout())
                                    })
                                }
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
                    HeaderRow(activeWorkout)
                    PerformedExercisesList(
                        activeWorkout, state, dispatch, modifier = Modifier
                            .height(0.dp)
                            .weight(1f)
                    )
                    AddExerciseButton(dispatch)
                }
            }
        )
    }

@Composable
fun HeaderRow(activeWorkout: Workout) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Column {
                Text(text = "Started: ${activeWorkout.startTime.format("HH:mm")}")
                val duration = timeSince(activeWorkout.startTime)
                Text(text = "Duration: ${formatDuration(duration)}")
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "${activeWorkout.exercises.size} exercises")
                Text(text = "${activeWorkout.exercises.sumOf { it.sets.size }} sets")
            }
        }
    }
    Divider()
}

@Composable
fun timeSince(startTime: Long): Long {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    return currentTime - startTime
}

@Composable
private fun PerformedExercisesList(
    activeWorkout: Workout,
    state: State,
    dispatch: Dispatch,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        itemsIndexedWithDividers(activeWorkout.exercises) { index, exercise ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        dispatch(doNavigateTo(Screen.EditExercise(index)))
                    }
                    .padding(18.dp)
            ) {
                Text(exercise.name)
                Spacer(modifier = Modifier.weight(1f))
                Text("${exercise.sets.size} sets")
                Spacer(modifier = Modifier.width(16.dp))
                SimpleStringOverflowMenu {
                    "Delete" does {
                        dispatch(doRemoveExercise(exercise.id))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddExerciseButton(dispatch: Dispatch) {
    Button(
        onClick = {
            dispatch(doNavigateTo(Screen.PickExerciseInActiveWorkout))
        }
    ) {
        Text(text = "Add Exercise")
    }
}