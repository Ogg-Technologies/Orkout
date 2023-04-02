package com.oggtechnologies.orkout.model.database

import com.oggtechnologies.orkout.App
import com.oggtechnologies.orkout.model.store.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object DBView {
    fun addOrUpdateExerciseTemplate(template: ExerciseTemplate) {
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().insertExerciseTemplate(
                ExerciseTemplateEntity(
                    id = template.id,
                    name = template.name,
                    hasWeight = SetDataField.Weight in template.fields,
                    hasReps = SetDataField.Reps in template.fields,
                    hasTime = SetDataField.Time in template.fields,
                    hasDistance = SetDataField.Distance in template.fields,
                )
            )
        }
    }

    fun getExerciseTemplates(): Flow<List<ExerciseTemplate>> {
        return App.db.appDao().loadExerciseTemplates().map { list ->
            list.map {
                ExerciseTemplate(
                    id = it.id,
                    name = it.name,
                    fields = listOfNotNull(
                        if (it.hasWeight) SetDataField.Weight else null,
                        if (it.hasReps) SetDataField.Reps else null,
                        if (it.hasTime) SetDataField.Time else null,
                        if (it.hasDistance) SetDataField.Distance else null,
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun addWorkout(workout: Workout) {
        println("Adding workout: $workout")
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().insertWorkout(
                WorkoutEntity(
                    id = workout.id,
                    startTime = workout.startTime,
                    endTime = workout.endTime,
                    workoutTemplate = null,
                )
            )
            workout.exercises.forEach { exercise ->
                addExercise(workout.id, exercise)
            }
        }
    }

    fun addExercise(workoutId: Int, exercise: Exercise) {
        MainScope().launch(Dispatchers.IO) {
            val exerciseIndex = App.db.appDao().getNextExerciseIndex(workoutId)
            App.db.appDao().insertExercise(
                ExerciseEntity(
                    id = exercise.id,
                    listIndex = exerciseIndex,
                    workout = workoutId,
                    exerciseTemplate = exercise.template!!.id,
                )
            )
            exercise.sets.forEach { set ->
                addSet(exercise.id, set)
            }
        }
    }

    fun addSet(exerciseId: Int, set: ExerciseSet) {
        MainScope().launch(Dispatchers.IO) {
            val setIndex = App.db.appDao().getNextSetIndex(exerciseId)
            App.db.appDao().insertSet(
                SetEntity(
                    id = set.id,
                    listIndex = setIndex,
                    exercise = exerciseId,
                    weight = set.weight,
                    reps = set.reps,
                    time = set.time,
                    distance = set.distance,
                )
            )
        }
    }

    fun removeWorkout(workoutId: Int) {
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().deleteWorkout(workoutId)
        }
    }

    fun removeExercise(exerciseId: Int) {
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().deleteExercise(exerciseId)
        }
    }

    fun removeSet(setId: Int) {
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().deleteSet(setId)
        }
    }

    fun setWorkoutTimes(workoutId: Int, startTime: Long, endTime: Long) {
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().updateWorkoutTimes(
                WorkoutTimes(
                    id = workoutId,
                    startTime = startTime,
                    endTime = endTime,
                )
            )
        }
    }

    fun editSet(exerciseId: Int, setIndex: Int, set: ExerciseSet) {
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().updateSet(
                SetEntity(
                    id = set.id,
                    listIndex = setIndex,
                    exercise = exerciseId,
                    weight = set.weight,
                    reps = set.reps,
                    time = set.time,
                    distance = set.distance,
                )
            )
        }
    }

    fun getWorkouts(): Flow<List<Workout>> {
        return App.db.appDao().loadFullWorkouts().map { list ->
            list.map { fullWorkout ->
                Workout(
                    id = fullWorkout.workout.id,
                    templateId = fullWorkout.fullWorkoutTemplate?.workoutTemplate?.id ?: 0,
                    startTime = fullWorkout.workout.startTime,
                    endTime = fullWorkout.workout.endTime,
                    exercises = fullWorkout.fullExercises.map { fullExercise ->
                        Exercise(
                            id = fullExercise.exercise.id,
                            templateId = fullExercise.exerciseTemplate.id,
                            sets = fullExercise.sets.map { set ->
                                ExerciseSet(
                                    id = set.id,
                                    weight = set.weight,
                                    reps = set.reps,
                                    time = set.time,
                                    distance = set.distance,
                                )
                            }
                        ).also { print(fullExercise.sets) }
                    }
                )
            }
        }.flowOn(Dispatchers.IO)
    }
}

fun prepopulateData() {
    infix fun String.has(fields: List<SetDataField>): ExerciseTemplate =
        ExerciseTemplate(this, fields)

    val exerciseTemplates = listOf(
        "Bench press" has listOf(SetDataField.Weight, SetDataField.Reps),
        "Squat" has listOf(SetDataField.Weight, SetDataField.Reps),
        "Incline dumbbell press" has listOf(SetDataField.Weight, SetDataField.Reps),
        "Machine chest flyes" has listOf(SetDataField.Weight, SetDataField.Reps),
        "Dumbbell lateral raises" has listOf(SetDataField.Weight, SetDataField.Reps),
        "Decline crunches" has listOf(SetDataField.Reps),
        "Weighted hyperextensions" has listOf(SetDataField.Weight, SetDataField.Reps),
        "Machine triceps extensions" has listOf(SetDataField.Weight, SetDataField.Reps),
        "Machine biceps curls" has listOf(SetDataField.Weight, SetDataField.Reps),
        "Plank" has listOf(SetDataField.Time),
        "Treadmill running" has listOf(SetDataField.Time, SetDataField.Distance),
    )

    for (template in exerciseTemplates) {
        DBView.addOrUpdateExerciseTemplate(template)
    }
}