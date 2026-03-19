package com.poker.game.game

import com.poker.game.models.*

/**
 * 游戏控制器 - 管理整局游戏
 */
class GameController {
    private val deck = Deck()
    private val aiPlayers = mutableMapOf<Int, SimpleAI>()
    var gameState = GameState()
    
    /**
     * 初始化游戏
     */
    fun initGame(playerCount: Int) {
        gameState = GameState(
            players = createPlayers(playerCount),
            smallBlind = 50,
            bigBlind = 100,
            minRaise = 100
        )
        
        // 初始化 AI
        gameState.players.filter { it.type == PlayerType.AI }.forEach { player ->
            aiPlayers[player.id] = SimpleAI(player)
        }
        
        startNewHand()
    }
    
    /**
     * 创建玩家
     */
    private fun createPlayers(count: Int): List<Player> {
        val players = mutableListOf<Player>()
        
        // 人类玩家（始终在0号位）
        players.add(Player(
            id = 0,
            name = "你",
            type = PlayerType.HUMAN,
            chips = 2000
        ))
        
        // AI 玩家
        for (i in 1 until count) {
            players.add(Player(
                id = i,
                name = "电脑${i}",
                type = PlayerType.AI,
                chips = 2000
            ))
        }
        
        return players
    }
    
    /**
     * 开始新的一手牌
     */
    fun startNewHand() {
        // 检查是否有玩家破产
        if (gameState.players.count { it.chips > 0 } <= 1) {
            gameState.isGameOver = true
            val winner = gameState.players.maxByOrNull { it.chips }
            gameState.winner = winner
            gameState.message = "游戏结束！${winner?.name}获胜！"
            return
        }
        
        // 重置牌堆
        deck.initialize()
        deck.shuffle()
        
        // 重置玩家状态
        gameState.players.forEach { it.resetForNewRound() }
        
        // 移动庄家按钮
        gameState.dealerIndex = (gameState.dealerIndex + 1) % gameState.players.size
        
        // 设置大小盲
        setBlinds()
        
        // 重置底池
        gameState.resetPot()
        gameState.communityCards = emptyList()
        gameState.currentStage = GameStage.PREFLOP
        gameState.gameRound++
        
        // 发手牌
        dealHands()
        
        // 翻牌前下注
        gameState.currentPlayerIndex = (gameState.dealerIndex + 3) % gameState.players.size
        gameState.message = "翻牌前下注"
        
        // 小盲注
        val smallBlindPlayer = gameState.getSmallBlindPlayer()
        smallBlindPlayer?.bet(gameState.smallBlind)
        
        // 大盲注
        val bigBlindPlayer = gameState.getBigBlindPlayer()
        bigBlindPlayer?.bet(gameState.bigBlind)
        
        gameState.lastAggressorIndex = gameState.players.indexOf(bigBlindPlayer)
        
        // 收集盲注
        gameState.collectPot()
        
        // 进入翻牌前下注阶段
        processBettingRound()
    }
    
    /**
     * 设置大小盲位置
     */
    private fun setBlinds() {
        gameState.players.forEach {
            it.isSmallBlind = false
            it.isBigBlind = false
            it.isDealer = false
        }
        
        gameState.players.getOrNull(gameState.dealerIndex)?.isDealer = true
        
        val sbIndex = (gameState.dealerIndex + 1) % gameState.players.size
        val bbIndex = (gameState.dealerIndex + 2) % gameState.players.size
        
        gameState.players.getOrNull(sbIndex)?.isSmallBlind = true
        gameState.players.getOrNull(bbIndex)?.isBigBlind = true
    }
    
    /**
     * 发手牌
     */
    private fun dealHands() {
        // 每人发2张
        for (i in 0 until 2) {
            gameState.players.forEach { player ->
                val card = deck.deal()
                if (card != null) {
                    player.hand = player.hand + card
                }
            }
        }
    }
    
    /**
     * 玩家执行动作
     */
    fun playerAction(action: PlayerAction, raiseAmount: Int = 0): Boolean {
        val player = gameState.getCurrentPlayer() ?: return false
        if (player.type != PlayerType.HUMAN) return false
        
        return executeAction(player, action, raiseAmount)
    }
    
    /**
     * AI 执行动作
     */
    fun aiAction(): Boolean {
        val player = gameState.getCurrentPlayer() ?: return false
        if (player.type != PlayerType.AI) return false
        
        val ai = aiPlayers[player.id] ?: return false
        val action = ai.decide(gameState)
        
        // AI 延迟响应
        return executeAction(player, action, 0)
    }
    
    /**
     * 执行动作
     */
    private fun executeAction(player: Player, action: PlayerAction, raiseAmount: Int): Boolean {
        val maxBet = gameState.players
            .filter { it.status == PlayerStatus.ACTIVE || it.status == PlayerStatus.ALL_IN }
            .maxOfOrNull { it.currentBet } ?: 0
        
        when (action) {
            PlayerAction.FOLD -> {
                player.status = PlayerStatus.FOLDED
                gameState.message = "${player.name} 弃牌"
            }
            PlayerAction.CHECK -> {
                if (player.currentBet < maxBet) return false
                gameState.message = "${player.name} 过牌"
            }
            PlayerAction.CALL -> {
                val callAmount = maxBet - player.currentBet
                player.bet(callAmount)
                gameState.message = "${player.name} 跟注 $callAmount"
            }
            PlayerAction.RAISE -> {
                val callAmount = maxBet - player.currentBet
                val totalRaise = callAmount + raiseAmount
                player.bet(totalRaise)
                gameState.minRaise = raiseAmount
                gameState.lastAggressorIndex = gameState.players.indexOf(player)
                gameState.message = "${player.name} 加注到 ${player.currentBet}"
            }
            PlayerAction.ALL_IN -> {
                player.bet(player.chips)
                gameState.message = "${player.name} 全下！"
            }
        }
        
        gameState.collectPot()
        return true
    }
    
    /**
     * 处理下注轮
     */
    private fun processBettingRound() {
        // 移到下一个玩家
        moveToNextPlayer()
        
        // 检查下注是否完成
        if (isBettingRoundComplete()) {
            advanceStage()
        }
    }
    
    /**
     * 移到下一个玩家
     */
    private fun moveToNextPlayer() {
        var nextIndex = (gameState.currentPlayerIndex + 1) % gameState.players.size
        var attempts = 0
        
        while (attempts < gameState.players.size) {
            val nextPlayer = gameState.players.getOrNull(nextIndex)
            if (nextPlayer != null && nextPlayer.status == PlayerStatus.ACTIVE) {
                gameState.currentPlayerIndex = nextIndex
                return
            }
            nextIndex = (nextIndex + 1) % gameState.players.size
            attempts++
        }
        
        // 没有活跃玩家了
        gameState.currentPlayerIndex = -1
    }
    
    /**
     * 检查下注轮是否完成
     */
    private fun isBettingRoundComplete(): Boolean {
        val activePlayers = gameState.players.filter { 
            it.status == PlayerStatus.ACTIVE || it.status == PlayerStatus.ALL_IN 
        }
        
        if (activePlayers.size <= 1) return true  // 只有一人存活
        
        // 检查是否所有人都已下注相同
        val maxBet = activePlayers.maxOfOrNull { it.currentBet } ?: 0
        if (activePlayers.any { it.currentBet < maxBet }) return false
        
        // 检查是否所有人都已行动（除了最后加注者）
        val lastAggressor = gameState.players.getOrNull(gameState.lastAggressorIndex)
        if (lastAggressor != null) {
            val indexAfterAggressor = (gameState.lastAggressorIndex + 1) % gameState.players.size
            if (gameState.currentPlayerIndex == indexAfterAggressor) return true
        }
        
        return false
    }
    
    /**
     * 进入下一阶段
     */
    private fun advanceStage() {
        when (gameState.currentStage) {
            GameStage.PREFLOP -> {
                // 发翻牌
                gameState.communityCards = deck.deal(3)
                gameState.currentStage = GameStage.FLOP
                gameState.message = "翻牌: ${gameState.communityCards.joinToString(" ")}"
            }
            GameStage.FLOP -> {
                // 发转牌
                gameState.communityCards = gameState.communityCards + deck.deal(1)
                gameState.currentStage = GameStage.TURN
                gameState.message = "转牌: ${gameState.communityCards.last()}"
            }
            GameStage.TURN -> {
                // 发河牌
                gameState.communityCards = gameState.communityCards + deck.deal(1)
                gameState.currentStage = GameStage.RIVER
                gameState.message = "河牌: ${gameState.communityCards.last()}"
            }
            GameStage.RIVER -> {
                // 摊牌
                gameState.currentStage = GameStage.SHOWDOWN
                determineWinner()
                return
            }
            GameStage.SHOWDOWN -> {
                startNewHand()
                return
            }
        }
        
        // 重置下注相关
        gameState.players.forEach { it.currentBet = 0 }
        gameState.lastAggressorIndex = -1
        gameState.currentPlayerIndex = (gameState.dealerIndex + 1) % gameState.players.size
        
        // 找到第一个可行动的玩家
        while (gameState.players.getOrNull(gameState.currentPlayerIndex)?.status != PlayerStatus.ACTIVE) {
            gameState.currentPlayerIndex = (gameState.currentPlayerIndex + 1) % gameState.players.size
        }
    }
    
    /**
     * 确定赢家
     */
    private fun determineWinner() {
        val activePlayers = gameState.players.filter { 
            it.status != PlayerStatus.FOLDED 
        }
        
        if (activePlayers.size == 1) {
            // 只有一人没弃牌
            val winner = activePlayers.first()
            winner.winChips(gameState.pot)
            gameState.winner = winner
            gameState.message = "${winner.name} 获胜！底池: ${gameState.pot}"
            return
        }
        
        // 比较牌型
        var bestEvaluation: HandEvaluation? = null
        var winner: Player? = null
        
        for (player in activePlayers) {
            val allCards = player.hand + gameState.communityCards
            val evaluation = HandEvaluator.evaluate(allCards)
            
            if (bestEvaluation == null || evaluation > bestEvaluation) {
                bestEvaluation = evaluation
                winner = player
            }
        }
        
        winner?.let {
            it.winChips(gameState.pot)
            gameState.winner = it
            gameState.winningHand = bestEvaluation?.handType
            gameState.message = "${it.name} 获胜！${bestEvaluation?.handType?.displayName}"
        }
    }
    
    /**
     * 继续游戏（下注轮完成后的自动处理）
     */
    fun continueGame() {
        if (gameState.isGameOver) return
        
        // 如果当前是摊牌阶段，显示结果后开始新牌
        if (gameState.currentStage == GameStage.SHOWDOWN) {
            gameState.message = "点击继续下一局"
            return
        }
        
        // 检查是否需要处理 AI
        val currentPlayer = gameState.getCurrentPlayer()
        if (currentPlayer != null && currentPlayer.type == PlayerType.AI) {
            // AI 自动行动
            if (aiAction()) {
                continueGame()
            }
        } else if (currentPlayer != null) {
            // 人类玩家行动
            processBettingRound()
        }
    }
    
    /**
     * 获取玩家可以执行的动作
     */
    fun getAvailableActions(): List<PlayerAction> {
        val player = gameState.getCurrentPlayer() ?: return emptyList()
        if (player.type != PlayerType.HUMAN) return emptyList()
        
        val actions = mutableListOf<PlayerAction>()
        val maxBet = gameState.players
            .filter { it.status == PlayerStatus.ACTIVE || it.status == PlayerStatus.ALL_IN }
            .maxOfOrNull { it.currentBet } ?: 0
        val toCall = maxBet - player.currentBet
        
        // 可以过牌
        if (toCall == 0 || player.currentBet == maxBet) {
            actions.add(PlayerAction.CHECK)
        }
        
        // 可以跟注
        if (toCall > 0 && player.chips > 0) {
            actions.add(PlayerAction.CALL)
            if (toCall < player.chips) {
                actions.add(PlayerAction.RAISE)
            }
        }
        
        // 可以全下
        if (player.chips > 0) {
            actions.add(PlayerAction.ALL_IN)
        }
        
        // 始终可以弃牌
        actions.add(PlayerAction.FOLD)
        
        return actions
    }
}
