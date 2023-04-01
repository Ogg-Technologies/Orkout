package com.oggtechnologies.orkout.model.database

import androidx.core.content.edit
import com.oggtechnologies.orkout.App
import com.oggtechnologies.orkout.model.store.Screen
import com.oggtechnologies.orkout.model.store.State
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val STATE_KEY = "state"

object JsonDatabase {

    @Serializable
    data class RelevantState(
        val activeWorkoutId: Int? = null,
        val navigationStack: List<Screen> = listOf(Screen.Main),
    )

    fun saveRelevantState(state: State) {
        val relevantState = RelevantState(
            activeWorkoutId = state.activeWorkoutId,
            navigationStack = state.navigationStack,
        )
        writeJsonState(Json.encodeToString(relevantState))
    }

    fun readRelevantState(): State? {
        val json = readJsonState() ?: return null
        return try {
            val relevantState = Json.decodeFromString(RelevantState.serializer(), json)
            State(
                activeWorkoutId = relevantState.activeWorkoutId,
                navigationStack = relevantState.navigationStack,
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun writeJsonState(json: String) {
        App.prefs.edit { putString(STATE_KEY, json) }
    }

    private fun readJsonState(): String? {
        return App.prefs.getString(STATE_KEY, null)
    }

}