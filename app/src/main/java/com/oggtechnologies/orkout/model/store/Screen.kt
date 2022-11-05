package com.oggtechnologies.orkout.model.store

import com.example.gshop.redux.Thunk
import com.oggtechnologies.orkout.redux.Action
import kotlinx.serialization.Serializable

val State.currentScreen get() = navigationStack.last()

@Serializable
sealed class Screen {
    @Serializable
    object Main : Screen()

    @Serializable
    object ExerciseTemplates : Screen()

    @Serializable
    data class EditExerciseTemplate(val exerciseTemplate: ExerciseTemplate) : Screen()

    @Serializable
    object WorkoutHistory : Screen()

    @Serializable
    object ActiveWorkout : Screen()

    @Serializable
    object PickExercise : Screen()

    @Serializable
    data class EditExercise(val exerciseIndex: Int) : Screen()
}

/**
 * Defines which screens can be navigated to from the current screen.
 * You can always go back to the previous screen.
 */
infix fun Screen.canNavigateTo(destination: Screen): Boolean {
    return when (this) {
        is Screen.Main -> destination in listOf(
            Screen.ActiveWorkout,
            Screen.WorkoutHistory,
            Screen.ExerciseTemplates
        )
        is Screen.ExerciseTemplates -> destination is Screen.EditExerciseTemplate
        is Screen.ActiveWorkout -> destination is Screen.PickExercise || destination is Screen.EditExercise
        is Screen.PickExercise -> destination is Screen.EditExercise
        else -> false
    }
}

const val SCREEN_CHANGE_DELAY: Long = 40
private fun doScreenChangeDispatch(action: Action) = doDelayedDispatch(action, SCREEN_CHANGE_DELAY)

sealed interface NavAction : Action {
    data class Goto(val screen: Screen) : NavAction
    object Back : NavAction
    object Home : NavAction
}

fun doNavigateTo(screen: Screen) = doScreenChangeDispatch(NavAction.Goto(screen))
fun doNavigateBack() = doScreenChangeDispatch(NavAction.Back)
fun doNavigateHome() = doScreenChangeDispatch(NavAction.Home)

typealias NavigationStack = List<Screen>

fun NavigationStack.editLast(block: (Screen) -> Screen): NavigationStack =
    dropLast(1) + block(last())

fun navigationReducer(
    navigationStack: NavigationStack,
    action: Action,
): NavigationStack = when (action) {
    is NavAction.Goto -> if (navigationStack.last() canNavigateTo action.screen) navigationStack + action.screen else navigationStack
    is NavAction.Back -> if (navigationStack.isNotEmpty()) navigationStack.dropLast(1) else navigationStack
    is NavAction.Home -> listOf(Screen.Main)
    else -> navigationStack
}

sealed interface ScreenAction : Action {
    data class SetExerciseTemplateName(val name: String) : ScreenAction
    data class ToggleExerciseTemplateField(val field: SetDataField) : ScreenAction
}

fun Screen.EditExerciseTemplate.editTemplate(block: ExerciseTemplate.() -> ExerciseTemplate): Screen.EditExerciseTemplate =
    copy(exerciseTemplate = exerciseTemplate.block())

fun ExerciseTemplate.toggleField(field: SetDataField): ExerciseTemplate {
    return if (field in fields) {
        copy(fields = fields - field)
    } else {
        copy(fields = fields + field)
    }
}

fun screenReducer(
    screen: Screen,
    action: ScreenAction,
): Screen =
    when (screen) {
        is Screen.EditExerciseTemplate -> when (action) {
            is ScreenAction.SetExerciseTemplateName -> screen.editTemplate { copy(name = action.name) }
            is ScreenAction.ToggleExerciseTemplateField -> screen.editTemplate { toggleField(action.field) }
            else -> screen
        }
        else -> screen
    }
