package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.oggtechnologies.orkout.model.store.*
import com.oggtechnologies.orkout.model.store.State
import com.oggtechnologies.orkout.redux.Dispatch
import java.time.Instant
import java.util.*
import kotlin.math.absoluteValue

@Composable
fun WorkoutHistoryScreen(state: State, dispatch: Dispatch) = ConfirmationHandler {
    BackHandler {
        dispatch(doNavigateBack())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Workout History")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            LazyColumn {
                item {
                    CalendarView(
                        dateRingColor = { date ->
                            val workoutOnDate = state.workoutHistory.firstOrNull {
                                Instant.ofEpochMilli(it.startTime).atZone(
                                    TimeZone.getDefault().toZoneId()
                                ).toLocalDate() == date
                            }
                            workoutOnDate?.let { workout ->
                                Color(workout.templateId.let {
                                    ColorUtils.HSLToColor(
                                        floatArrayOf(
                                            (it.toFloat().absoluteValue + 20) % 360f,
                                            0.9f,
                                            0.75f
                                        )
                                    )
                                })
                            }
                        }
                    )
                }
                itemsWithDividers(state.workoutHistory) { workout ->
                    WorkoutRow(workout, remove = {
                        showConfirmDialog("Are you sure you want to delete this workout?") {
                            dispatch(doRemoveWorkout(workout.id))
                        }
                    })
                }
            }
        }
    )
}

@Composable
fun WorkoutRow(workout: Workout, remove: () -> Unit) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        val weekDay = workout.startTime.format("EEEE")
        val time = workout.startTime.format("HH:mm")
        val date = workout.startTime.format("yyyy-MM-dd")
        val duration =
            if (workout.endTime == null) "Ongoing" else formatDuration(workout.endTime - workout.startTime)
        val nExercises = workout.exercises.size
        val nSets = workout.exercises.sumOf { it.sets.size }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "$weekDay $duration", fontSize = 22.sp)
                Text(text = "$date $time", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column {
                Text(text = "$nExercises exercises")
                Text(text = "$nSets sets")
            }
            Spacer(modifier = Modifier.weight(1f))
            SimpleStringOverflowMenu {
                "Delete" does remove
            }
        }
        if (expanded) {
            Card(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    for (exercise in workout.exercises) {
                        Text(text = exercise.name, fontSize = 16.sp)
                        for (set in exercise.sets) {
                            Text(
                                text = exercise.template!!.prettyPrintSet(set),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
            var showDebug: Boolean by remember { mutableStateOf(false) }
            Button(onClick = { showDebug = !showDebug }) {
                Text(text = "Toggle Debug")
            }
            if (showDebug) {
                Text(
                    text = workout.toString(),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}