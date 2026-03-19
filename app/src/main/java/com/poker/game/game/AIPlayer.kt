package com.poker.game.game

import com.poker.game.models.*

/**
 * 简单 AI 策略
 */
class SimpleAI(private val player: Player) {
    
    /**
     * 决定 AI 的行动
     */
    fun decide(gameState: GameState): PlayerAction {
        val currentPlayer = gameState.getCurrentPlayer() ?: return PlayerAction.FOLD
        if (currentPlayer.id != player.id) return PlayerAction.FOLD
        
        val hand = player.hand
        val communityCards = gameState.communityCards
        val pot = gameState.pot
        val toCall = getCallAmount(gameState)
        
        // 手牌评估
        val handStrength = evaluateHandStrength(hand, communityCards)
        val potOdds = calculatePotOdds(toCall, pot)
        
        return when {
            // 弃牌条件
            handStrength < 0.2 && toCall > player.chips / 2 -> PlayerAction.FOLD
            
            // 检查是否已经 all-in
            player.chips == 0 -> PlayerAction.ALL_IN
            
            // 弃牌
            toCall > player.chips -> PlayerAction.FOLD
            
            // 过牌条件
            toCall == 0 -> {
                if (handStrength > 0.6 && player.chips > gameState.bigBlind * 3) {
                    // 好牌可以下注
                    val raiseAmount = calculateRaiseAmount(gameState)
                    if (raiseAmount > 0) PlayerAction.RAISE else PlayerAction.CHECK
                } else {
                    PlayerAction.CHECK
                }
            }
            
            // 跟注或加注
            else -> {
                when {
                    // 好牌加注
                    handStrength > 0.7 -> {
                        val raiseAmount = calculateRaiseAmount(gameState)
                        if (raiseAmount > toCall && player.chips >= raiseAmount) {
                            PlayerAction.RAISE
                        } else if (player.chips <= toCall) {
                            PlayerAction.ALL_IN
                        } else {
                            PlayerAction.CALL
                        }
                    }
                    // 中等牌力看赔率
                    handStrength > 0.4 -> {
                        if (potOdds > handStrength) {
                            if (player.chips <= toCall) PlayerAction.ALL_IN
                            else PlayerAction.CALL
                        } else {
                            // 尝试便宜地看更多牌
                            if (toCall <= player.chips / 10) PlayerAction.CALL
                            else PlayerAction.FOLD
                        }
                    }
                    // 差牌弃牌
                    else -> {
                        if (toCall <= player.chips / 20) PlayerAction.CALL  // 便宜跟注
                        else PlayerAction.FOLD
                    }
                }
            }
        }
    }
    
    /**
     * 获取需要跟注的金额
     */
    private fun getCallAmount(gameState: GameState): Int {
        val maxBet = gameState.players
            .filter { it.status == PlayerStatus.ACTIVE || it.status == PlayerStatus.ALL_IN }
            .maxOfOrNull { it.currentBet } ?: 0
        return maxBet - player.currentBet
    }
    
    /**
     * 计算加注金额
     */
    private fun calculateRaiseAmount(gameState: GameState): Int {
        val toCall = getCallAmount(gameState)
        val minRaise = gameState.minRaise
        val raiseAmount = toCall + minRaise
        
        return if (player.chips >= raiseAmount) raiseAmount else 0
    }
    
    /**
     * 评估手牌强度 (0-1)
     */
    private fun evaluateHandStrength(hand: List<Card>, communityCards: List<Card>): Double {
        if (hand.size < 2) return 0.0
        
        // 前翻牌圈 - 只有手牌
        if (communityCards.isEmpty()) {
            return evaluatePreFlop(hand)
        }
        
        // 计算7张牌的牌型
        val allCards = hand + communityCards
        val evaluation = HandEvaluator.evaluate(allCards)
        
        // 根据牌型给分
        return when (evaluation.handType) {
            HandType.ROYAL_FLUSH -> 1.0
            HandType.STRAIGHT_FLUSH -> 0.95
            HandType.FOUR_OF_A_KIND -> 0.9
            HandType.FULL_HOUSE -> 0.85
            HandType.FLUSH -> 0.8
            HandType.STRAIGHT -> 0.7
            HandType.THREE_OF_A_KIND -> 0.6
            HandType.TWO_PAIR -> 0.5
            HandType.PAIR -> 0.4
            HandType.HIGH_CARD -> 0.2
        }
    }
    
    /**
     * 评估翻牌前手牌
     */
    private fun evaluatePreFlop(hand: List<Card>): Double {
        if (hand.size != 2) return 0.0
        
        val (c1, c2) = hand
        val isPair = c1.rank == c2.rank
        val isSuited = c1.suit == c2.suit
        val highRank = maxOf(c1.rank.value, c2.rank.value)
        val lowRank = minOf(c1.rank.value, c2.rank.value)
        
        // 口袋对子
        return when {
            c1.rank == Rank.ACE -> 0.95
            c1.rank == Rank.KING -> 0.85
            c1.rank == Rank.QUEEN -> 0.75
            c1.rank == Rank.JACK -> 0.7
            c1.rank == Rank.TEN -> 0.65
            isPair -> 0.55 + (highRank - 9) * 0.02
            // 同花连牌
            isSuited && highRank >= 10 -> 0.6
            isSuited && highRank >= 8 && lowRank >= 5 -> 0.5
            // 不同花高牌
            highRank >= 12 -> 0.45
            highRank >= 10 -> 0.35
            else -> 0.25
        }
    }
    
    /**
     * 计算底池赔率
     */
    private fun calculatePotOdds(callAmount: Int, pot: Int): Double {
        if (callAmount == 0) return 1.0
        return callAmount.toDouble() / (pot + callAmount)
    }
}
