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
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.AsyncThunk
import com.oggtechnologies.orkout.redux.Dispatch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActiveWorkoutScreen(activeWorkout: Workout, state: State, dispatch: Dispatch) = ConfirmationHandler {
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
                Text(
                    text = "Started at ${
                        SimpleDateFormat("HH:mm", Locale.ENGLISH).format(
                            activeWorkout.startTime
                        )
                    }"
                )
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
            dispatch(doNavigateTo(Screen.PickExercise))
        }
    ) {
        Text(text = "Add Exercise")
    }
}