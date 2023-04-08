package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch

@Composable
fun WorkoutTemplatesScreen(state: State, dispatch: Dispatch) = ConfirmationHandler {
    BackHandler {
        dispatch(doNavigateBack())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Workout Templates")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            LazyColumn {
                itemsWithDividers(state.workoutTemplates) { workoutTemplate ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                dispatch(doNavigateTo(Screen.EditWorkoutTemplate(workoutTemplate.id)))
                            }
                            .padding(16.dp)
                    ) {
                        Text(text = workoutTemplate.name, fontSize = 20.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        SimpleStringOverflowMenu {
                            "Delete" does {
                                showConfirmDialog("Delete ${workoutTemplate.name}?") {
                                    dispatch(doRemoveWorkoutTemplate(workoutTemplate.id))
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { createWorkoutTemplate(dispatch) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    )
}

fun createWorkoutTemplate(dispatch: Dispatch) {
    val newWorkoutTemplate = WorkoutTemplate(
        name = "Template",
        suggestedExercises = emptyList()
    )
    dispatch(doAddWorkoutTemplate(newWorkoutTemplate))
    dispatch(doNavigateTo(Screen.EditWorkoutTemplate(newWorkoutTemplate.id)))
}