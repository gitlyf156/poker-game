package com.poker.game.models

/**
 * 游戏阶段
 */
enum class GameStage {
    PREFLOP,   // 翻牌前
    FLOP,      // 翻牌（发3张公共牌）
    TURN,      // 转牌（发1张公共牌）
    RIVER,     // 河牌（发1张公共牌）
    SHOWDOWN   // 摊牌
}

/**
 * 游戏状态
 */
data class GameState(
    var players: List<Player> = emptyList(),
    var communityCards: List<Card> = emptyList(),  // 公共牌
    var pot: Int = 0,                               // 底池
    var sidePots: List<Int> = emptyList(),         // 边池（用于 all-in 情况）
    var currentStage: GameStage = GameStage.PREFLOP,
    var currentPlayerIndex: Int = 0,               // 当前行动的玩家索引
    var dealerIndex: Int = 0,                      // 庄家位置
    var smallBlind: Int = 50,
    var bigBlind: Int = 100,
    var minRaise: Int = 100,
    var lastAggressorIndex: Int = -1,              // 最后加注者
    var gameRound: Int = 0,                        // 当前第几手牌
    var deck: List<Card> = emptyList(),            // 牌堆
    var winner: Player? = null,                    // 赢家
    var winningHand: HandType? = null,             // 获胜牌型
    var message: String = "",                      // 显示消息
    var isGameOver: Boolean = false                // 游戏是否结束
) {
    /**
     * 获取当前玩家
     */
    fun getCurrentPlayer(): Player? {
        return players.getOrNull(currentPlayerIndex)
    }
    
    /**
     * 获取庄家
     */
    fun getDealer(): Player? {
        return players.getOrNull(dealerIndex)
    }
    
    /**
     * 获取大盲位玩家
     */
    fun getBigBlindPlayer(): Player? {
        return players.getOrNull((dealerIndex + 1) % players.size)
    }
    
    /**
     * 获取小盲位玩家
     */
    fun getSmallBlindPlayer(): Player? {
        return players.getOrNull((dealerIndex + 2) % players.size)
    }
    
    /**
     * 获取活跃玩家数量
     */
    fun getActivePlayerCount(): Int {
        return players.count { it.status == PlayerStatus.ACTIVE }
    }
    
    /**
     * 获取所有可行动的玩家
     */
    fun getPlayersWhoCanAct(): List<Player> {
        return players.filter { 
            it.status == PlayerStatus.ACTIVE && 
            it.currentBet < players.filter { p -> p.status == PlayerStatus.ACTIVE }.maxOfOrNull { p -> p.currentBet } ?: 0
        }
    }
    
    /**
     * 检查是否所有人都已行动
     */
    fun isBettingRoundComplete(): Boolean {
        val activePlayers = players.filter { it.status == PlayerStatus.ACTIVE || it.status == PlayerStatus.ALL_IN }
        if (activePlayers.size <= 1) return true
        if (activePlayers.any { it.currentBet < activePlayers.maxOf { p -> p.currentBet } }) return false
        return true
    }
    
    /**
     * 重置底池和公共牌
     */
    fun resetPot() {
        pot = 0
        sidePots = emptyList()
        communityCards = emptyList()
        players.forEach { it.currentBet = 0 }
    }
    
    /**
     * 收集底池
     */
    fun collectPot() {
        players.forEach { player ->
            pot += player.currentBet
            player.currentBet = 0
        }
    }
}
