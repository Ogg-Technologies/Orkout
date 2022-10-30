package com.oggtechnologies.orkout.model.store

import com.example.gshop.redux.AsyncThunk
import com.oggtechnologies.orkout.redux.Action
import kotlinx.coroutines.delay
import java.util.*

fun generateId(): ID = UUID.randomUUID().leastSignificantBits.toInt()

fun doDelayedDispatch(
    action: Action,
    delay: Long = 1000,
) = AsyncThunk { _, dispatch ->
    delay(delay)
    dispatch(action)
}

