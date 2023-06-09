package com.oggtechnologies.orkout.model.store

import com.oggtechnologies.orkout.redux.Action
import com.oggtechnologies.orkout.redux.AsyncThunk
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

fun generateId(): Int = UUID.randomUUID().leastSignificantBits.toInt()

fun doDelayedDispatch(
    action: Action,
    delay: Long = 1000,
) = AsyncThunk { _, dispatch ->
    delay(delay)
    dispatch(action)
}

fun <T> List<T>.remove(index: Int): List<T> = filterIndexed { i, _ -> i != index }

fun <T> List<T>.set(index: Int, newValue: T): List<T> =
    mapIndexed { i, value -> if (i == index) newValue else value }

fun <T> List<T>.edit(index: Int, transform: (T) -> T): List<T> =
    mapIndexed { i, value -> if (i == index) transform(value) else value }

fun Long.asMillisToLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), TimeZone.getDefault().toZoneId())