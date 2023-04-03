package com.oggtechnologies.orkout.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oggtechnologies.orkout.model.store.ExerciseTemplate
import com.oggtechnologies.orkout.model.store.name

@Composable
fun SearchableExerciseTemplatesListView(
    exerciseTemplates: List<ExerciseTemplate>,
    onItemClick: (ExerciseTemplate) -> Unit,
    getLastPerformedTime: (ExerciseTemplate) -> Long?,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        var searchQuery: String by remember { mutableStateOf("") }
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            trailingIcon = {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Clear search query",
                    modifier = Modifier.clickable { searchQuery = "" }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
        )
        Divider()
        ExerciseTemplatesListView(
            exerciseTemplates.filterBySearchQuery(searchQuery).sortedByDescending(getLastPerformedTime),
            onItemClick
        )
    }
}

private fun List<ExerciseTemplate>.filterBySearchQuery(searchQuery: String): List<ExerciseTemplate> =
    filter { it.name.contains(searchQuery, ignoreCase = true) }

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
