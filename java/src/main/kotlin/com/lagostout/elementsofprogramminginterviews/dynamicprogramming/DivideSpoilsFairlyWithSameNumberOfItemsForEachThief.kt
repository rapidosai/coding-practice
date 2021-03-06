package com.lagostout.elementsofprogramminginterviews.dynamicprogramming
import com.lagostout.common.takeFrom
import com.lagostout.elementsofprogramminginterviews.dynamicprogramming.DivideSpoilsFairly.Spoils
import com.lagostout.elementsofprogramminginterviews.dynamicprogramming.DivideSpoilsFairlyWithSameNumberOfItemsForEachThief.SpoilsSplit.EqualSplit
import com.lagostout.elementsofprogramminginterviews.dynamicprogramming.DivideSpoilsFairlyWithSameNumberOfItemsForEachThief.SpoilsSplit.UnequalSplit
import kotlin.math.absoluteValue

/* Problem 17.6.6 page 325 */

object DivideSpoilsFairlyWithSameNumberOfItemsForEachThief {

    sealed class SpoilsSplit {

        object UnequalSplit : SpoilsSplit()

        data class EqualSplit(
                val first: Spoils = Spoils(),
                val second: Spoils = Spoils()) : SpoilsSplit()

        fun toList(): List<Spoils> {
            return when (this) {
                UnequalSplit -> emptyList()
                is EqualSplit -> listOf(first, second)
            }
        }

    }

    /* If we wanted to allow the thieves to split the spoils evenly when
    there's an odd number of items, we would run the following method n - 1
    times, excluding one of the items each time.  Then we select the spoils
    split with the least spread between the thieves' portions. */

    fun computeWithRecursionAndBruteForce(items: List<Int>):
            SpoilsSplit {
        fun compute(itemIndex: Int,
                    firstThiefSpoilsValue: Int,
                    secondThiefSpoilsValue: Int,
                    firstThiefItemCount: Int,
                    secondThiefItemCount: Int): SpoilsSplit {
            return when {
                listOf(firstThiefItemCount, secondThiefItemCount)
                        .any { it >= items.size / 2 + 1 } -> UnequalSplit
                itemIndex > items.lastIndex ->
                    EqualSplit(Spoils(), Spoils())
                else -> {
                    val item = items[itemIndex]
                    listOf(
                        Triple(compute(itemIndex + 1,
                            firstThiefSpoilsValue + item,
                            secondThiefSpoilsValue,
                            firstThiefItemCount + 1,
                            secondThiefItemCount), {
                                r: EqualSplit ->
                            r.copy(first = r.first.add(item))
                        }, { r: EqualSplit ->
                            (firstThiefSpoilsValue + r.first.value + item -
                                    (secondThiefSpoilsValue + r.second.value))
                                    .absoluteValue
                        }),
                        Triple(compute(itemIndex + 1,
                            firstThiefSpoilsValue,
                            secondThiefSpoilsValue + item,
                            firstThiefItemCount,
                            secondThiefItemCount + 1), {
                                r: EqualSplit ->
                            r.copy(second = r.second.add(item))
                        }, { r: EqualSplit ->
                            (secondThiefSpoilsValue + r.second.value + item -
                                    (firstThiefSpoilsValue + r.first.value))
                                    .absoluteValue
                        })
                    ).filter {
                        // One of the two splits will always be an equal split.
                        // As such, this filter will always leave at least one
                        // split in the list.
                        it.first != UnequalSplit
                    }.map {
                        Triple(it.first as EqualSplit, it.second, it.third)
                    }.sortedBy {
                        it.third(it.first)
                    }.let {
                        it.first().let {
                            it.second(it.first)
                        }
                    }
                }
            }
        }
        return compute(0, 0, 0, 0, 0)
    }

    data class Key(val itemIndex: Int, val firstThiefSpoilsValue: Int,
                   val secondThiefSpoilsValue: Int, val firstThiefItemCount: Int,
                   val secondThiefItemCount: Int)

    fun computeWithRecursionAndMemoization(items: List<Int>): SpoilsSplit {
        val cache = mutableMapOf<Key, SpoilsSplit>()
        fun compute(itemIndex: Int,
                    firstThiefSpoilsValue: Int,
                    secondThiefSpoilsValue: Int,
                    firstThiefItemCount: Int,
                    secondThiefItemCount: Int): SpoilsSplit {
            val key = Key(itemIndex, firstThiefSpoilsValue, secondThiefSpoilsValue,
                firstThiefItemCount, secondThiefItemCount)
            return cache[key]?.also {
//                println("hit: key: $key, value: $it")
            } ?: when {
                listOf(firstThiefItemCount, secondThiefItemCount)
                        .any { it >= items.size / 2 + 1 } -> UnequalSplit
                itemIndex > items.lastIndex ->
                    EqualSplit(Spoils(), Spoils())
                else -> {
                    val item = items[itemIndex]
                    listOf(
                        Triple(compute(itemIndex + 1,
                            firstThiefSpoilsValue + item,
                            secondThiefSpoilsValue,
                            firstThiefItemCount + 1,
                            secondThiefItemCount), { r: EqualSplit ->
                            r.copy(first = r.first.add(item))
                        }, { r: EqualSplit ->
                            (firstThiefSpoilsValue + r.first.value + item -
                                    (secondThiefSpoilsValue + r.second.value))
                                    .absoluteValue
                        }),
                        Triple(compute(itemIndex + 1,
                            firstThiefSpoilsValue,
                            secondThiefSpoilsValue + item,
                            firstThiefItemCount,
                            secondThiefItemCount + 1), { r: EqualSplit ->
                            r.copy(second = r.second.add(item))
                        }, { r: EqualSplit ->
                            (secondThiefSpoilsValue + r.second.value + item -
                                    (firstThiefSpoilsValue + r.first.value))
                                    .absoluteValue
                        })
                    ).filter {
                        it.first != UnequalSplit
                    }.map {
                        Triple(it.first as EqualSplit, it.second, it.third)
                    }.sortedBy {
                        it.third(it.first)
                    }.let {
                        it.first().let {
                            it.second(it.first)
                        }
                    }
                }
            }.also {
                cache[key] = it
            }
        }
        return compute(0, 0, 0, 0, 0)
    }

    fun computeBottomUpWithMemoization(items: List<Int>): SpoilsSplit {
        return when {
            items.isEmpty() -> EqualSplit()
            else -> {
                val cache = MutableList<Set<EqualSplit>>(items.size) {
                    emptySet()
                }
                items[0].let {
                    cache[0] = setOf(EqualSplit(Spoils(it), Spoils()),
                        EqualSplit(Spoils(), Spoils(it)))
                }
                items.withIndex().takeFrom(1).forEach { (index, item) ->
                    cache[index] = cache[index - 1].flatMap {
                        setOf(it.copy(first = it.first.add(item)),
                            it.copy(second = it.second.add(item))).also {
                        }
                    }.filter {
                        listOf(it.first.items.size, it.second.items.size).all {
                            it < items.size / 2 + 1
                        }
                    }.toSet()
                }
                cache[items.lastIndex].sortedBy {
                    (it.first.value - it.second.value).absoluteValue
                }.first()
            }
        }
    }

}