package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.model.store.State
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
                        SetView(setIndex, set, template, edit = { newSet ->
                            dispatch(doEditSet(exercise.id, newSet))
                        }, remove = {
                            dispatch(doRemoveSet(set.id))
                        })
                    }
                }
                Button(onClick = {
                    dispatch(doNewSet(exercise))
                }) {
                    Text("Add set")
                }
            }
        }
    )
}

@Composable
fun SetView(
    setIndex: Int,
    set: ExerciseSet,
    template: ExerciseTemplate,
    edit: (ExerciseSet) -> Unit,
    remove: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${setIndex + 1}", fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                remove()
            }) {
                Text("Delete set")
            }
        }
        for (field in template.fields) {
            when (field) {
                is SetDataField.Reps -> DataFieldInt(field, set.reps)
                { edit(set.copy(reps = it)) }
                is SetDataField.Weight -> DataFieldDouble(field, set.weight)
                { edit(set.copy(weight = it)) }
                is SetDataField.Time -> DataFieldInt(field, set.time)
                { edit(set.copy(time = it)) }
                is SetDataField.Distance -> DataFieldDouble(field, set.distance)
                { edit(set.copy(distance = it)) }
            }
        }
    }
}

@Composable
fun DataFieldDouble(
    field: SetDataField,
    value: Double?,
    edit: (Double?) -> Unit
) {
    val str = value?.let {
        if (it % 1 == 0.0) {
            it.toInt().toString()
        } else {
            it.toString()
        }
    } ?: ""
    var text: String by remember { mutableStateOf(str) }
    fun isValid() = text.isEmpty() || text.toDoubleOrNull() != null
    DataField(
        value = text,
        onValueChange = { newString ->
            text = newString
            if (isValid()) edit(newString.toDoubleOrNull())
        },
        field = field,
        isValid = isValid()
    )
}

@Composable
fun DataFieldInt(
    field: SetDataField,
    value: Int?,
    edit: (Int?) -> Unit
) {
    var text: String by remember { mutableStateOf(value?.toString() ?: "") }
    fun isValid() = text.isEmpty() || text.toIntOrNull() != null
    DataField(
        value = text,
        onValueChange = { newString ->
            text = newString
            if (isValid()) edit(newString.toIntOrNull())
        },
        field = field,
        isValid = isValid()
    )
}

@Composable
fun DataField(value: String, field: SetDataField, onValueChange: (String) -> Unit, isValid: Boolean) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("${field.name} (${field.unit})") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        isError = !isValid,
        modifier = Modifier.fillMaxWidth()
    )
}

