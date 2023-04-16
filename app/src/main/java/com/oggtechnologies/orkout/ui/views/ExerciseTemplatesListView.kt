package com.oggtechnologies.orkout.ui.views

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
import com.oggtechnologies.orkout.model.store.fieldsToString
import com.oggtechnologies.orkout.ui.itemsWithDividers

@Composable
fun SearchableExerciseTemplatesListView(
    exerciseTemplates: List<ExerciseTemplate>,
    onItemClick: (ExerciseTemplate) -> Unit,
    templateRowContent: @Composable (ExerciseTemplate) -> Unit =
        { DefaultTemplateRowContent(it) }
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
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            itemsWithDividers(exerciseTemplates.filterBySearchQuery(searchQuery)) { exerciseTemplate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(exerciseTemplate) }
                ) {
                    templateRowContent(exerciseTemplate)
                }
            }
        }
    }
}

private fun List<ExerciseTemplate>.filterBySearchQuery(searchQuery: String): List<ExerciseTemplate> =
    filter { it.name.contains(searchQuery, ignoreCase = true) }

@Composable
private fun DefaultTemplateRowContent(exerciseTemplate: ExerciseTemplate) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(exerciseTemplate.name, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(exerciseTemplate.fieldsToString(), fontSize = 15.sp, textAlign = TextAlign.End)
    }
}
