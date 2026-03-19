package com.poker.game.models

/**
 * 玩家动作类型
 */
enum class PlayerAction {
    FOLD,      // 弃牌
    CHECK,     // 过牌
    CALL,      // 跟注
    RAISE,     // 加注
    ALL_IN     // 全下
}

/**
 * 玩家状态
 */
enum class PlayerStatus {
    ACTIVE,    // 游戏中
    FOLDED,    // 已弃牌
    ALL_IN,    // 全下
    OUT        // 已出局（筹码为0）
}

/**
 * 玩家类型
 */
enum class PlayerType {
    HUMAN,     // 人类玩家
    AI         // AI 对手
}

/**
 * 玩家
 */
data class Player(
    val id: Int,
    val name: String,
    val type: PlayerType,
    var chips: Int = 2000,           // 当前筹码
    var currentBet: Int = 0,         // 当前回合下注额
    var hand: List<Card> = emptyList(),  // 手牌
    var status: PlayerStatus = PlayerStatus.ACTIVE,
    var isDealer: Boolean = false,  // 是否为庄家
    var isBigBlind: Boolean = false, // 是否为大盲
    var isSmallBlind: Boolean = false // 是否为小盲
) {
    /**
     * 是否还能下注
     */
    fun canBet(): Boolean = status == PlayerStatus.ACTIVE && chips > 0
    
    /**
     * 是否已经 all-in
     */
    fun isAllIn(): Boolean = status == PlayerStatus.ALL_IN
    
    /**
     * 是否已弃牌
     */
    fun hasFolded(): Boolean = status == PlayerStatus.FOLDED
    
    /**
     * 是否出局
     */
    fun isOut(): Boolean = status == PlayerStatus.OUT || chips <= 0
    
    /**
     * 执行下注
     */
    fun bet(amount: Int): Int {
        val actualBet = minOf(amount, chips)
        chips -= actualBet
        currentBet += actualBet
        if (chips == 0) {
            status = PlayerStatus.ALL_IN
        }
        return actualBet
    }
    
    /**
     * 回收筹码（赢钱时）
     */
    fun winChips(amount: Int) {
        chips += amount
        if (chips > 0 && status == PlayerStatus.ALL_IN) {
            status = PlayerStatus.ACTIVE
        }
    }
    
    /**
     * 重置回合状态
     */
    fun resetForNewRound() {
        currentBet = 0
        hand = emptyList()
        status = PlayerStatus.ACTIVE
    }
    
    /**
     * 重置整局游戏
     */
    fun resetForNewGame() {
        chips = 2000
        currentBet = 0
        hand = emptyList()
        status = PlayerStatus.ACTIVE
        isDealer = false
        isBigBlind = false
        isSmallBlind = false
    }
}
