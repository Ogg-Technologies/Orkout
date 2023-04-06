package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
fun EditWorkoutTemplateScreen(
    template: WorkoutTemplate,
    state: State,
    dispatch: Dispatch
) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Workout Template")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = template.name,
                    onValueChange = { dispatch(doRenameWorkoutTemplate(template.id, it)) },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexedWithDividers(template.suggestedExercises) { index, exerciseTemplate ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(text = exerciseTemplate.name, fontSize = 20.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            SimpleStringOverflowMenu {
                                "Delete" does {
                                    dispatch(
                                        doRemoveSuggestedExercise(
                                            template.id,
                                            exerciseTemplate.id
                                        )
                                    )
                                }
                                if (index > 0) "Move up" does {
                                    dispatch(
                                        doMoveSuggestedExercise(
                                            template.id,
                                            exerciseTemplate.id,
                                            index - 1
                                        )
                                    )
                                }
                                if (index < template.suggestedExercises.size - 1) "Move down" does {
                                    dispatch(
                                        doMoveSuggestedExercise(
                                            template.id,
                                            exerciseTemplate.id,
                                            index + 1
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dispatch(doNavigateTo(Screen.PickExerciseTemplateForWorkoutTemplate(template.id)))
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Exercise Template")
            }
        }
    )
}