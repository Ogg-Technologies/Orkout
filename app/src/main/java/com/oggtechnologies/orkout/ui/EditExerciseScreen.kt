package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch

@Composable
fun EditExerciseScreen(screen: Screen.EditExercise, state: State, dispatch: Dispatch) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    val exercise = state.activeWorkout?.exercises?.get(screen.exerciseIndex)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit ${exercise?.name}")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            val template = exercise.template
            if (template == null || exercise == null) {
                Text("Unknown exercise")
                return@Scaffold
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .height(0.dp)
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexedWithDividers(exercise.sets) { setIndex, set ->
                        SetView(set, template, edit = { newSet ->
                            dispatch(EditSet(screen.exerciseIndex, setIndex, newSet))
                        }, remove = {
                            dispatch(RemoveSet(screen.exerciseIndex, setIndex))
                        })
                    }
                }
                Button(onClick = {
                    addSet(screen.exerciseIndex, dispatch)
                }) {
                    Text("Add set");
                }
            }
        }
    )
}

@Composable
fun SetView(
    set: ExerciseSet,
    template: ExerciseTemplate,
    edit: (ExerciseSet) -> Unit,
    remove: () -> Unit
) {
    Column {
        for (field in template.fields) {
            TextField(
                value = when (field) {
                    is SetDataField.Reps -> set.reps
                    is SetDataField.Weight -> set.weight
                    is SetDataField.Time -> set.time
                    is SetDataField.Distance -> set.distance
                }?.toString() ?: "",
                onValueChange = {
                    edit(
                        when (field) {
                            is SetDataField.Reps -> set.copy(reps = it.toIntOrNull())
                            is SetDataField.Weight -> set.copy(weight = it.toDoubleOrNull())
                            is SetDataField.Time -> set.copy(time = it.toIntOrNull())
                            is SetDataField.Distance -> set.copy(distance = it.toDoubleOrNull())
                        }
                    )
                },
                label = { Text(field.name) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
            )
        }
        Button(onClick = {
            remove()
        }) {
            Text("Delete set");
        }
    }
}

fun addSet(exerciseIndex: Int, dispatch: Dispatch) {
    dispatch(AddSet(exerciseIndex, ExerciseSet()))
}