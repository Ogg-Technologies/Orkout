package com.oggtechnologies.orkout.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oggtechnologies.orkout.model.store.Exercise
import com.oggtechnologies.orkout.model.store.prettyPrintSet
import com.oggtechnologies.orkout.model.store.template

@Composable
fun ExerciseInfoCardView(
    header: String,
    exercise: Exercise,
    modifier: Modifier = Modifier
) = Card(
    modifier = modifier
        .padding(18.dp)
) {
    Column(
        modifier = Modifier
            .padding(18.dp)
    ) {
        Text(header)
        Column(
            modifier = Modifier.padding(start = 20.dp)
        ) {
            exercise.sets.forEachIndexed { index, exerciseSet ->
                val setDataString = exercise.template!!.prettyPrintSet(exerciseSet)
                Text("Set ${index + 1}: $setDataString")
            }
        }
    }
}
