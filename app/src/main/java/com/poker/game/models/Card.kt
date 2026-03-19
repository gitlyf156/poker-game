package com.poker.game.models

import kotlin.random.Random

/**
 * 扑克牌花色
 */
enum class Suit(val symbol: String, val color: Int) {
    SPADES("♠", 0xFF1A1A1A.toInt()),      // 黑桃
    HEARTS("♥", 0xFFD40000.toInt()),      // 红心
    CLUBS("♣", 0xFF1A1A1A.toInt()),       // 梅花
    DIAMONDS("♦", 0xFFD40000.toInt())    // 方块
}

/**
 * 扑克牌点数
 */
enum class Rank(val value: Int, val symbol: String) {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A")
}

/**
 * 一张扑克牌
 */
data class Card(
    val suit: Suit,
    val rank: Rank
) {
    val isRed: Boolean get() = suit == Suit.HEARTS || suit == Suit.DIAMONDS
    
    override fun toString(): String = "${rank.symbol}${suit.symbol}"
    
    companion object {
        /**
         * 创建一副标准52张牌
         */
        fun createDeck(): List<Card> {
            return Suit.values().flatMap { suit ->
                Rank.values().map { rank ->
                    Card(suit, rank)
                }
            }
        }
        
        /**
         * 洗牌
         */
        fun shuffle(deck: List<Card>): List<Card> {
            return deck.shuffled(Random)
        }
    }
}
