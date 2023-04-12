package com.oggtechnologies.orkout.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.model.store.State
import com.oggtechnologies.orkout.redux.AsyncThunk
import com.oggtechnologies.orkout.redux.Dispatch
import com.oggtechnologies.orkout.ui.*
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
                    TemplateSelector(activeWorkout, state, dispatch)
                    HeaderRow(activeWorkout)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                    ) {
                        PerformedExercisesList(activeWorkout, dispatch)
                        AddExerciseView(state, activeWorkout, dispatch)
                    }
                }
            }
        )
    }

@Composable
private fun AddExerciseView(
    state: State,
    activeWorkout: Workout,
    dispatch: Dispatch
) {
    state.getWorkoutTemplate(activeWorkout.templateId)
        ?.let { template ->
            val quickAddExercises = template.suggestedExercises.filter { suggestedExercise ->
                activeWorkout.exercises.none { it.template!!.id == suggestedExercise.id }
            }
            Card(
                modifier = Modifier
                    .padding(18.dp)
            ) {
                Column {
                    quickAddExercises.forEach { suggestedExercise ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dispatch(
                                        doStartExercise(
                                            activeWorkout.id,
                                            suggestedExercise
                                        )
                                    )
                                }
                                .padding(18.dp)
                        ) {
                            Text(suggestedExercise.name)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Add exercise"
                            )
                        }
                    }
                }
            }
        }
    Button(
        onClick = {
            dispatch(doNavigateTo(Screen.PickExerciseInActiveWorkout))
        }
    ) {
        Text(text = "Add Exercise")
    }
}

@Composable
private fun PerformedExercisesList(
    activeWorkout: Workout,
    dispatch: Dispatch
) {
    activeWorkout.exercises.forEachIndexed { index, exercise ->
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

@Composable
fun TemplateSelector(activeWorkout: Workout, state: State, dispatch: Dispatch) {
    val templateOptions = listOf(null) + state.workoutTemplates
    val items = templateOptions.map { it?.name ?: "No Template" }
    // indexOfFirst returns -1 if not found, so we add 1 to get the correct index
    val selected =
        state.workoutTemplates.indexOfFirst { it.id == state.activeWorkout?.templateId } + 1
    Spinner(
        items = items,
        selected = selected,
        onSelected = {
            dispatch(doSetWorkoutTemplateForWorkout(activeWorkout.id, templateOptions[it]?.id))
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

