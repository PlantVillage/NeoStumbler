package xyz.malkki.neostumbler.extensions

import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

inline fun <reified A, B> Collection<Flow<A>>.combineAny(
    crossinline combiner: suspend (Array<A?>) -> B
): Flow<B> = channelFlow {
    val values = arrayOfNulls<A?>(size)

    forEachIndexed { index, flow ->
        launch {
            flow.collect {
                values[index] = it

                send(combiner(values.copyOf()))
            }
        }
    }
}

fun <T> Flow<T>.buffer(window: Duration): Flow<List<T>> = channelFlow {
    val items: MutableList<T> = mutableListOf<T>()
    var finished = false

    launch {
        collect { items.add(it) }

        finished = true
    }

    while (true) {
        delay(window)

        send(items.toList())
        items.clear()

        if (finished) {
            break
        }
    }
}

fun <A, B, C> Flow<A>.combineWithLatestFrom(other: Flow<B>, combiner: (A, B?) -> C): Flow<C> =
    channelFlow {
        var otherValue: B? = null

        launch { other.collect { otherValue = it } }

        collect { send(combiner.invoke(it, otherValue)) }
    }

/**
 * Emits null if the flow does not emit another value within the specified duration. Note that this
 * will cause the flow to emit null as the last value (i.e. it probably makes sense to use this
 * operator only for flows that never complete)
 */
fun <T> Flow<T>.maxAge(duration: Duration): Flow<T?> = transformLatest { value ->
    emit(value)
    delay(duration)
    emit(null)
}

fun <T> Flow<T>.pairwise(): Flow<Pair<T, T>> =
    scan(Pair<T?, T?>(null, null)) { pair, value -> pair.second to value }
        .filter { it.first != null && it.second != null }
        .map { it.first!! to it.second!! }
