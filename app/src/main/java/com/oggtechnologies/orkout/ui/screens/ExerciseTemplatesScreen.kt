package com.oggtechnologies.orkout.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch
import com.oggtechnologies.orkout.ui.BackButton
import com.oggtechnologies.orkout.ui.SearchableExerciseTemplatesListView

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
            SearchableExerciseTemplatesListView(
                exerciseTemplates = state.getExerciseTemplatesSortedByRecency(),
                onItemClick = { exerciseTemplate ->
                    dispatch(doNavigateTo(Screen.EditExerciseTemplate(exerciseTemplate)))
                },
            )
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
        fields = emptyList()
    )
    dispatch(doNavigateTo(Screen.EditExerciseTemplate(newExerciseTemplate)))
}