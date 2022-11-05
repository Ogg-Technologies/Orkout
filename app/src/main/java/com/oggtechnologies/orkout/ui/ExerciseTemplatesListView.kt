package com.oggtechnologies.orkout.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oggtechnologies.orkout.model.store.ExerciseTemplate
import com.oggtechnologies.orkout.model.store.name

@Composable
fun ExerciseTemplatesListView(
    exerciseTemplates: List<ExerciseTemplate>,
    onItemClick: (ExerciseTemplate) -> Unit
) {
    LazyColumn {
        itemsWithDividers(exerciseTemplates) { exerciseTemplate ->
            ExerciseTemplateListItem(exerciseTemplate, onClick = { onItemClick(exerciseTemplate) })
        }
    }
}

@Composable
private fun ExerciseTemplateListItem(exerciseTemplate: ExerciseTemplate, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(16.dp)
    ) {
        Text(exerciseTemplate.name, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        val fieldsString = exerciseTemplate.fields.map { it.name.first() }.joinToString("/")
        Text(fieldsString, fontSize = 15.sp, textAlign = TextAlign.End)
    }
}
