package com.oggtechnologies.orkout.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import com.oggtechnologies.orkout.model.store.doNavigateBack
import com.oggtechnologies.orkout.redux.Dispatch
import java.text.SimpleDateFormat
import java.util.*

fun Context.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun <I> LazyListScope.itemsWithDividers(
    items: List<I>,
    divider: @Composable () -> Unit = { Divider() },
    itemContent: @Composable LazyItemScope.(item: I) -> Unit,
) = itemsIndexedWithDividers(items, divider) { _, item ->
    itemContent(item)
}

fun <I> LazyListScope.itemsIndexedWithDividers(
    items: List<I>,
    divider: @Composable () -> Unit = { Divider() },
    itemContent: @Composable LazyItemScope.(index: Int, item: I) -> Unit,
) {
    items.forEachIndexed { index, item ->
        item {
            if (index != 0) divider()
            itemContent(index, item)
        }
    }
}

@Composable
fun BackButton(dispatch: Dispatch) {
    IconButton(
        onClick = { dispatch(doNavigateBack()) },
    ) {
        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
    }
}

fun Long.format(f: String): String = SimpleDateFormat(f, Locale.ENGLISH).format(this)

fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val totalMinutes = totalSeconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val seconds = totalSeconds % 60
    if (hours != 0L) return "${hours}h${minutes}m${seconds}s"
    if (totalMinutes != 0L) return "${minutes}m${seconds}s"
    return "${seconds}s"
}

private data class ConfirmationDialogState(
    val title: String,
    val onConfirm: () -> Unit,
)

class ConfirmationHandlerScope(
    val showConfirmDialog: (title: String, onConfirm: () -> Unit) -> Unit,
    val hideConfirmDialog: () -> Unit,
)

@Composable
fun ConfirmationHandler(content: @Composable ConfirmationHandlerScope.() -> Unit) {
    var confirmDialogState: ConfirmationDialogState? by remember { mutableStateOf(null) }
    confirmDialogState?.let {
        AlertDialog(
            title = { Text(it.title) },
            onDismissRequest = { confirmDialogState = null },
            confirmButton = {
                Button(onClick = { it.onConfirm(); confirmDialogState = null }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { confirmDialogState = null }) {
                    Text("No")
                }
            }
        )
    }
    ConfirmationHandlerScope(
        showConfirmDialog = { title, onConfirm ->
            confirmDialogState = ConfirmationDialogState(title, onConfirm)
        },
        hideConfirmDialog = { confirmDialogState = null }
    ).content()
}

@Composable
fun SimpleStringOverflowMenu(
    content: SimpleStringOverflowMenuScope.() -> Unit,
) {
    SimpleStringOverflowMenu(SimpleStringOverflowMenuScope().apply(content).items)
}

class SimpleStringOverflowMenuScope {
    val items = mutableListOf<SimpleStringOverflowMenuItem>()
    infix fun String.does(action: () -> Unit) {
        items.add(SimpleStringOverflowMenuItem(this, action))
    }
}

data class SimpleStringOverflowMenuItem(val title: String, val action: () -> Unit)

@Composable
fun SimpleStringOverflowMenu(
    items: List<SimpleStringOverflowMenuItem>,
) {
    Box {
        var isExpanded by remember { mutableStateOf(false) }
        IconButton(onClick = { isExpanded = !isExpanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            for (item in items) {
                DropdownMenuItem(onClick = {
                    item.action()
                    isExpanded = false
                }) {
                    Text(text = item.title)
                }
            }
        }
    }
}
