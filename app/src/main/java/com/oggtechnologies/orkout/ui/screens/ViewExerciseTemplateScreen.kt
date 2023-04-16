package com.oggtechnologies.orkout.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.redux.Dispatch
import com.oggtechnologies.orkout.ui.BackButton
import com.oggtechnologies.orkout.ui.views.ExerciseInfoCardView
import com.oggtechnologies.orkout.ui.views.SelectableGraph
import com.oggtechnologies.orkout.ui.views.toGraphDataPoints
import java.time.LocalDateTime

data class TimedExercise(
    val exercise: Exercise,
    val dateTime: LocalDateTime
)

@Composable
fun ViewExerciseTemplateScreen(
    exerciseTemplate: ExerciseTemplate,
    state: State,
    dispatch: Dispatch
) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    val history = state.getTimedExerciseHistory(exerciseTemplate)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${exerciseTemplate.name} history")
                },
                navigationIcon = {
                    BackButton(dispatch)
                },
                actions = {
                    IconButton(onClick = {
                        dispatch(doNavigateTo(Screen.EditExerciseTemplate(exerciseTemplate)))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Exercise Template"
                        )
                    }
                }
            )
        },
        content = {
            LazyColumn {
                if (SetDataField.Weight in exerciseTemplate.fields && SetDataField.Reps in exerciseTemplate.fields && history.size > 1) {
                    item {
                        ExerciseGraph(
                            history = history,
                        )
                    }
                }
                items(history) { (exercise, dateTime) ->
                    val dateString = dateTime.toLocalDate().toString()
                    ExerciseInfoCardView(
                        header = dateString,
                        exercise = exercise,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

@Composable
fun ExerciseGraph(
    history: List<TimedExercise>
) {
    val points = history.toGraphDataPoints()
    SelectableGraph(
        points = points,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(2.dp),
        contentPadding = PaddingValues(vertical = 50.dp, horizontal = 40.dp)
    )
}