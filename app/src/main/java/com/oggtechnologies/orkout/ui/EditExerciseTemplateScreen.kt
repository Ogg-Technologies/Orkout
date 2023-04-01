package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gshop.redux.AsyncThunk
import com.oggtechnologies.orkout.App
import com.oggtechnologies.orkout.model.database.AppDatabase
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
            Column(Modifier.fillMaxSize()) {
                TextField(
                    value = template.name,
                    onValueChange = { dispatch(ScreenAction.SetExerciseTemplateName(it)) },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Bench press") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (field in setDataFields) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                        ) {
                            Checkbox(checked = field in template.fields, onCheckedChange = {
                                dispatch(ScreenAction.ToggleExerciseTemplateField(field))
                            })
                            Text(text = "${field.name} (${field.unit})", fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { saveExerciseTemplate(template, dispatch) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Save")
                }
            }
        }
    )
}

fun saveExerciseTemplate(template: ExerciseTemplate, dispatch: Dispatch) {
    dispatch(doAddOrUpdateExerciseTemplate(template))
    dispatch(doNavigateBack())
}