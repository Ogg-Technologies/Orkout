package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch

@Composable
fun EditExerciseTemplateScreen(
    screen: Screen.EditExerciseTemplate,
    state: State,
    dispatch: Dispatch
) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    val template = screen.exerciseTemplate
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Exercise Template")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            Column(Modifier.fillMaxWidth()) {
                TextField(
                    value = template.name,
                    onValueChange = { dispatch(ScreenAction.SetExerciseTemplateName(it)) })
                Row {
                    for (field in setDataFields) {
                        Button(onClick = { dispatch(ScreenAction.ToggleExerciseTemplateField(field)) }) {
                            Text(field.name)
                        }
                    }
                }
                Button(onClick = { saveExerciseTemplate(template, dispatch) }) {
                    Text("Save")
                }
            }
        }
    )
}

fun saveExerciseTemplate(template: ExerciseTemplate, dispatch: Dispatch) {
    dispatch(AddExerciseTemplate(template))
    dispatch(doNavigateBack())
}