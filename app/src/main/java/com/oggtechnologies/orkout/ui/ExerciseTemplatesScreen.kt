package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch

@Composable
fun ExerciseTemplatesScreen(state: State, dispatch: Dispatch) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Exercise Templates")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            LazyColumn {
                itemsWithDividers(state.exerciseTemplates) { exerciseTemplate ->
                    Text(text = exerciseTemplate.name)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { createExerciseTemplate(dispatch) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    )
}

fun createExerciseTemplate(dispatch: Dispatch) {
    val newExerciseTemplate = ExerciseTemplate(
        name = "",
        id = generateId(),
        fields = emptyList()
    )
    dispatch(doNavigateTo(Screen.EditExerciseTemplate(newExerciseTemplate)))
}