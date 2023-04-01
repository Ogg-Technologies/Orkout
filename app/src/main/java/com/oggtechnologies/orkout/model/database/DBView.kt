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
            workout.exercises.forEachIndexed { exerciseIndex, exercise ->
                addExercise(workout.id, exerciseIndex, exercise)
            }
        }
    }

    fun addExercise(workoutId: Int, exerciseIndex: Int, exercise: Exercise) {
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().insertExercise(
                ExerciseEntity(
                    id = exercise.id,
                    index = exerciseIndex,
                    workout = workoutId,
                    exerciseTemplate = exercise.template!!.id,
                )
            )
            exercise.sets.forEachIndexed { index, set ->
                addSet(exercise.id, index, set)
            }
        }
    }

    fun addSet(exerciseId: Int, setIndex: Int, set: ExerciseSet) {
        MainScope().launch(Dispatchers.IO) {
            App.db.appDao().insertSet(
                SetEntity(
                    id = set.id,
                    index = setIndex,
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
                    index = setIndex,
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