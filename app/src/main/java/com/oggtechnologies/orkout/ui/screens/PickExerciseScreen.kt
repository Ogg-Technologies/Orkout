package com.oggtechnologies.orkout.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import com.oggtechnologies.orkout.model.store.ExerciseTemplate
import com.oggtechnologies.orkout.model.store.State
import com.oggtechnologies.orkout.model.store.doNavigateBack
import com.oggtechnologies.orkout.model.store.getExerciseTemplatesSortedByRecency
import com.oggtechnologies.orkout.redux.Dispatch
import com.oggtechnologies.orkout.ui.BackButton
import com.oggtechnologies.orkout.ui.views.SearchableExerciseTemplatesListView

@Composable
fun PickExerciseScreen(state: State, dispatch: Dispatch, onExercisePicked: (ExerciseTemplate) -> Unit) {
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
            SearchableExerciseTemplatesListView(
                exerciseTemplates = state.getExerciseTemplatesSortedByRecency(),
                onItemClick = {
                    onExercisePicked(it)
                    dispatch(doNavigateBack())
                },
            )
        }
    )
}