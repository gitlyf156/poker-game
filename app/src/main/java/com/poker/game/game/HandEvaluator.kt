package com.poker.game.models

/**
 * 牌型类型
 */
enum class HandType(val rank: Int, val displayName: String) {
    HIGH_CARD(1, "高牌"),
    PAIR(2, "一对"),
    TWO_PAIR(3, "两对"),
    THREE_OF_A_KIND(4, "三条"),
    STRAIGHT(5, "顺子"),
    FLUSH(6, "同花"),
    FULL_HOUSE(7, "葫芦"),
    FOUR_OF_A_KIND(8, "四条"),
    STRAIGHT_FLUSH(9, "同花顺"),
    ROYAL_FLUSH(10, "皇家同花顺")
}

/**
 * 牌型评估结果
 */
data class HandEvaluation(
    val handType: HandType,
    val mainCards: List<Card>,           // 构成牌型的牌
    val kickers: List<Card>,              // 踢脚牌
    val value: Long                       // 用于比较的值
) : Comparable<HandEvaluation> {
    override fun compareTo(other: HandEvaluation): Int {
        return when {
            handType.rank != other.handType.rank -> handType.rank.compareTo(other.handType.rank)
            value != other.value -> value.compareTo(other.value)
            else -> 0
        }
    }
}

/**
 * 牌型评估器
 */
object HandEvaluator {
    
    /**
     * 评估7张牌中最大的5张牌型
     */
    fun evaluate(cards: List<Card>): HandEvaluation {
        if (cards.size < 5 || cards.size > 7) {
            throw IllegalArgumentException("需要5-7张牌，当前: ${cards.size}")
        }
        
        // 获取所有可能的5张牌组合
        val combinations = cards.combinations(5)
        
        // 找出最大的牌型
        var bestHand: HandEvaluation? = null
        
        for (combo in combinations) {
            val evaluation = evaluate5CardHand(combo.sortedByDescending { it.rank.value })
            if (bestHand == null || evaluation > bestHand) {
                bestHand = evaluation
            }
        }
        
        return bestHand ?: evaluate5CardHand(cards.take(5).sortedByDescending { it.rank.value })
    }
    
    /**
     * 评估5张牌的牌型
     */
    private fun evaluate5CardHand(cards: List<Card>): HandEvaluation {
        val isFlush = cards.map { it.suit }.toSet().size == 1
        val isStraight = isSequential(cards.map { it.rank.value })
        
        // 统计每种点数的牌数
        val rankCounts = cards.groupBy { it.rank }.mapValues { it.value.size }
        val sortedCounts = rankCounts.values.sortedDescending()
        
        return when {
            // 皇家同花顺
            isFlush && isStraight && cards.first().rank == Rank.ACE -> {
                HandEvaluation(HandType.ROYAL_FLUSH, cards, emptyList(), 1000000000)
            }
            // 同花顺
            isFlush && isStraight -> {
                val straightValue = cards.first().rank.value.toLong()
                HandEvaluation(HandType.STRAIGHT_FLUSH, cards, emptyList(), 900000000 + straightValue)
            }
            // 四条
            sortedCounts.first() == 4 -> {
                val fourCardRank = rankCounts.entries.first { it.value == 4 }.key
                val kicker = cards.filter { it.rank != fourCardRank }.sortedByDescending { it.rank.value }
                val value = (fourCardRank.value * 1000000 + kicker[0].rank.value * 10000 + kicker[1].rank.value).toLong()
                HandEvaluation(HandType.FOUR_OF_A_KIND, 
                    cards.filter { it.rank == fourCardRank }, 
                    kicker, 
                    800000000 + value)
            }
            // 葫芦
            sortedCounts.first() == 3 && sortedCounts[1] == 2 -> {
                val threeRank = rankCounts.entries.first { it.value == 3 }.key
                val pairRank = rankCounts.entries.first { it.value == 2 }.key
                val value = (threeRank.value * 10000 + pairRank.value).toLong()
                HandEvaluation(HandType.FULL_HOUSE,
                    cards.filter { it.rank == threeRank || it.rank == pairRank },
                    emptyList(),
                    700000000 + value)
            }
            // 同花
            isFlush -> {
                val value = cards.sumOf { it.rank.value * Math.pow(100.0, 4 - cards.indexOf(it)).toLong() }
                HandEvaluation(HandType.FLUSH, cards, emptyList(), 600000000 + value)
            }
            // 顺子
            isStraight -> {
                val straightValue = cards.first().rank.value.toLong()
                HandEvaluation(HandType.STRAIGHT, cards, emptyList(), 500000000 + straightValue)
            }
            // 三条
            sortedCounts.first() == 3 -> {
                val threeRank = rankCounts.entries.first { it.value == 3 }.key
                val kickers = cards.filter { it.rank != threeRank }.sortedByDescending { it.rank.value }
                val value = (threeRank.value * 1000000 + kickers[0].rank.value * 10000 + kickers[1].rank.value).toLong()
                HandEvaluation(HandType.THREE_OF_A_KIND,
                    cards.filter { it.rank == threeRank },
                    kickers,
                    400000000 + value)
            }
            // 两对
            sortedCounts[0] == 2 && sortedCounts[1] == 2 -> {
                val pairs = rankCounts.entries.filter { it.value == 2 }.sortedByDescending { it.key.value }
                val kicker = cards.filter { it.rank != pairs[0].key && it.rank != pairs[1].key }
                val value = (pairs[0].key.value * 100000 + pairs[1].key.value * 1000 + kicker[0].rank.value).toLong()
                HandEvaluation(HandType.TWO_PAIR,
                    cards.filter { it.rank == pairs[0].key || it.rank == pairs[1].key },
                    kicker,
                    300000000 + value)
            }
            // 一对
            sortedCounts.first() == 2 -> {
                val pairRank = rankCounts.entries.first { it.value == 2 }.key
                val kickers = cards.filter { it.rank != pairRank }.sortedByDescending { it.rank.value }
                val value = (pairRank.value * 1000000 + kickers[0].rank.value * 10000 + 
                           kickers[1].rank.value * 100 + kickers[2].rank.value).toLong()
                HandEvaluation(HandType.PAIR,
                    cards.filter { it.rank == pairRank },
                    kickers,
                    200000000 + value)
            }
            // 高牌
            else -> {
                val value = cards.sumOf { it.rank.value * Math.pow(100.0, 4 - cards.indexOf(it)).toLong() }
                HandEvaluation(HandType.HIGH_CARD, cards, emptyList(), value)
            }
        }
    }
    
    /**
     * 检查是否是顺子（A可以当1用）
     */
    private fun isSequential(ranks: List<Int>): Boolean {
        if (ranks.size != 5) return false
        
        val sortedRanks = ranks.sorted()
        
        // 普通顺子
        if (sortedRanks[4] - sortedRanks[0] == 4 && sortedRanks.toSet().size == 5) {
            return true
        }
        
        // A-2-3-4-5 顺子（A当1用）
        if (sortedRanks == listOf(2, 3, 4, 5, 14)) {
            return true
        }
        
        return false
    }
    
    /**
     * 比较两组牌的输赢
     */
    fun compareHands(hand1: List<Card>, hand2: List<Card>): Int {
        val eval1 = evaluate(hand1)
        val eval2 = evaluate(hand2)
        return eval1.compareTo(eval2)
    }
}
