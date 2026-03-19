package com.poker.game.game

import com.poker.game.models.Card

/**
 * 牌组管理
 */
class Deck {
    private var cards: MutableList<Card> = mutableListOf()
    
    /**
     * 初始化一副新牌
     */
    fun initialize() {
        cards = Card.createDeck().toMutableList()
    }
    
    /**
     * 洗牌
     */
    fun shuffle() {
        cards.shuffle()
    }
    
    /**
     * 发一张牌
     */
    fun deal(): Card? {
        return if (cards.isNotEmpty()) cards.removeAt(0) else null
    }
    
    /**
     * 发多张牌
     */
    fun deal(count: Int): List<Card> {
        return (0 until minOf(count, cards.size)).mapNotNull { deal() }
    }
    
    /**
     * 剩余牌数
     */
    fun remaining(): Int = cards.size
    
    /**
     * 是否还有牌
     */
    fun isEmpty(): Boolean = cards.isEmpty()
}
