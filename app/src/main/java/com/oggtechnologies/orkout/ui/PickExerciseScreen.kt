package com.oggtechnologies.orkout.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import com.oggtechnologies.orkout.model.store.AddExercise
import com.oggtechnologies.orkout.model.store.State
import com.oggtechnologies.orkout.model.store.doNavigateBack
import com.oggtechnologies.orkout.model.store.doStartExercise
import com.oggtechnologies.orkout.redux.Dispatch

@Composable
fun PickExerciseScreen(state: State, dispatch: Dispatch) {
    BackHandler {
        dispatch(doNavigateBack())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pick Exercise")
                },
                navigationIcon = {
                    BackButton(dispatch)
                }
            )
        },
        content = {
            ExerciseTemplatesListView(state.exerciseTemplates, onItemClick = {
                dispatch(doStartExercise(it))
                dispatch(doNavigateBack())
            })
        }
    )
}