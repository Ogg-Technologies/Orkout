package com.oggtechnologies.orkout.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
import com.oggtechnologies.orkout.ui.BackButton
import com.oggtechnologies.orkout.ui.views.SearchableExerciseTemplatesListView
import com.oggtechnologies.orkout.ui.views.SimpleGraph
import com.oggtechnologies.orkout.ui.views.toGraphDataPoints
import java.time.LocalDate

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
                    dispatch(doNavigateTo(Screen.ViewExerciseTemplate(exerciseTemplate.id)))
                },
                templateRowContent = { exerciseTemplate ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 72.dp)
                            .padding(start = 16.dp, end = 8.dp)
                    ) {
                        val history = state.getTimedExerciseHistory(exerciseTemplate)
                        Text(text = exerciseTemplate.name, fontSize = 20.sp, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        if (exerciseTemplate.hasWeightAndReps() && history.size > 1) {
                            SimpleGraph(
                                points = history
                                    .toGraphDataPoints(),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val fontSize = 13.sp
                            val quantity = history.size
                            Text("$quantity Q", fontSize = fontSize)
                            val daysSinceLast = if (history.isEmpty()) null else LocalDate.now().toEpochDay() - history.first().dateTime.toLocalDate().toEpochDay()
                            Text("${daysSinceLast ?: "?"} E", fontSize = fontSize)
                            Text(exerciseTemplate.fieldsToString(), fontSize = fontSize)
                        }
                    }
                }
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