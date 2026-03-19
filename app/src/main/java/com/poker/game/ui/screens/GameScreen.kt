package com.poker.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poker.game.game.GameController
import com.poker.game.models.GameStage
import com.poker.game.models.PlayerAction
import com.poker.game.models.PlayerStatus
import com.poker.game.ui.components.*

/**
 * 游戏主界面
 */
@Composable
fun GameScreen(
    controller: GameController,
    onBackToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState = controller.gameState
    val humanPlayer = gameState.players.firstOrNull()
    val isHumanTurn = gameState.getCurrentPlayer()?.type == com.poker.game.models.PlayerType.HUMAN
    
    var showContinueButton by remember { mutableStateOf(false) }
    
    // 检测是否需要显示继续按钮
    LaunchedEffect(gameState.currentStage, gameState.winner) {
        if (gameState.currentStage == GameStage.SHOWDOWN || gameState.winner != null) {
            showContinueButton = true
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF35654d))
    ) {
        // 游戏结束显示
        if (gameState.isGameOver) {
            GameOverScreen(
                winner = gameState.winner?.name ?: "",
                onRestart = onBackToMenu,
                modifier = Modifier.fillMaxSize()
            )
            return@Box
        }
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ===== 顶部：AI 玩家区域 =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // 根据玩家数量动态布局
                val aiPlayers = gameState.players.filter { it.type == com.poker.game.models.PlayerType.AI }
                val playerCount = aiPlayers.size
                
                when {
                    playerCount <= 2 -> {
                        // 2人：左右各一个
                        aiPlayers.forEachIndexed { index, player ->
                            val align = if (index == 0) Alignment.TopStart else Alignment.TopEnd
                            val offset = if (index == 0) 60.dp else 60.dp
                            
                            PlayerPanel(
                                name = player.name,
                                chips = player.chips,
                                currentBet = player.currentBet,
                                hand = player.hand,
                                isActive = player.status == PlayerStatus.ACTIVE,
                                isDealer = player.isDealer,
                                isBigBlind = player.isBigBlind,
                                isSmallBlind = player.isSmallBlind,
                                isFolded = player.hasFolded(),
                                isAllIn = player.isAllIn(),
                                isCurrentPlayer = gameState.currentPlayerIndex == gameState.players.indexOf(player),
                                showCards = gameState.currentStage == GameStage.SHOWDOWN,
                                playerIndex = player.id,
                                modifier = Modifier
                                    .align(align)
                                    .padding(start = if (index == 0) offset else 0.dp, end = if (index == 1) offset else 0.dp, top = 16.dp)
                            )
                        }
                    }
                    playerCount <= 4 -> {
                        // 3-4人：顶部一排
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            aiPlayers.forEach { player ->
                                PlayerPanel(
                                    name = player.name,
                                    chips = player.chips,
                                    currentBet = player.currentBet,
                                    hand = player.hand,
                                    isActive = player.status == PlayerStatus.ACTIVE,
                                    isDealer = player.isDealer,
                                    isBigBlind = player.isBigBlind,
                                    isSmallBlind = player.isSmallBlind,
                                    isFolded = player.hasFolded(),
                                    isAllIn = player.isAllIn(),
                                    isCurrentPlayer = gameState.currentPlayerIndex == gameState.players.indexOf(player),
                                    showCards = gameState.currentStage == GameStage.SHOWDOWN,
                                    playerIndex = player.id
                                )
                            }
                        }
                    }
                    else -> {
                        // 5+人：顶部两排
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                aiPlayers.take(4).forEach { player ->
                                    PlayerPanel(
                                        name = player.name,
                                        chips = player.chips,
                                        currentBet = player.currentBet,
                                        hand = player.hand,
                                        isActive = player.status == PlayerStatus.ACTIVE,
                                        isDealer = player.isDealer,
                                        isBigBlind = player.isBigBlind,
                                        isSmallBlind = player.isSmallBlind,
                                        isFolded = player.hasFolded(),
                                        isAllIn = player.isAllIn(),
                                        isCurrentPlayer = gameState.currentPlayerIndex == gameState.players.indexOf(player),
                                        showCards = gameState.currentStage == GameStage.SHOWDOWN,
                                        playerIndex = player.id,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                            if (aiPlayers.size > 4) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    aiPlayers.drop(4).forEach { player ->
                                        PlayerPanel(
                                            name = player.name,
                                            chips = player.chips,
                                            currentBet = player.currentBet,
                                            hand = player.hand,
                                            isActive = player.status == PlayerStatus.ACTIVE,
                                            isDealer = player.isDealer,
                                            isBigBlind = player.isBigBlind,
                                            isSmallBlind = player.isSmallBlind,
                                            isFolded = player.hasFolded(),
                                            isAllIn = player.isAllIn(),
                                            isCurrentPlayer = gameState.currentPlayerIndex == gameState.players.indexOf(player),
                                            showCards = gameState.currentStage == GameStage.SHOWDOWN,
                                            playerIndex = player.id,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // ===== 中间：公共牌和底池 =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 底池
                    PotDisplay(pot = gameState.pot)
                    
                    // 公共牌
                    if (gameState.communityCards.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            gameState.communityCards.forEach { card ->
                                CardView(
                                    card = card,
                                    width = 60.dp,
                                    height = 85.dp
                                )
                            }
                        }
                    }
                    
                    // 游戏阶段
                    Text(
                        text = when (gameState.currentStage) {
                            GameStage.PREFLOP -> "翻牌前"
                            GameStage.FLOP -> "翻牌"
                            GameStage.TURN -> "转牌"
                            GameStage.RIVER -> "河牌"
                            GameStage.SHOWDOWN -> "摊牌"
                        },
                        color = Color(0xFFCCCCCC),
                        fontSize = 16.sp
                    )
                    
                    // 消息
                    GameMessage(message = gameState.message)
                    
                    // 继续按钮
                    if (showContinueButton && gameState.winner != null) {
                        ActionButton(
                            text = "继续",
                            onClick = {
                                showContinueButton = false
                                controller.startNewHand()
                            },
                            backgroundColor = Color(0xFF28a745)
                        )
                    }
                }
            }
            
            // ===== 底部：人类玩家 =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 玩家手牌
                    humanPlayer?.let { player ->
                        PlayerPanel(
                            name = player.name,
                            chips = player.chips,
                            currentBet = player.currentBet,
                            hand = player.hand,
                            isActive = player.status == PlayerStatus.ACTIVE,
                            isDealer = player.isDealer,
                            isBigBlind = player.isBigBlind,
                            isSmallBlind = player.isSmallBlind,
                            isFolded = player.hasFolded(),
                            isAllIn = player.isAllIn(),
                            isCurrentPlayer = isHumanTurn,
                            showCards = true,
                            playerIndex = 0
                        )
                    }
                    
                    // 动作按钮
                    if (isHumanTurn && !showContinueButton) {
                        val currentBet = gameState.players
                            .filter { it.status == PlayerStatus.ACTIVE }
                            .maxOfOrNull { it.currentBet } ?: 0
                        
                        ActionButtonGroup(
                            availableActions = controller.getAvailableActions(),
                            onAction = { action ->
                                if (action == PlayerAction.RAISE) {
                                    // 简化处理：最小加注
                                    controller.playerAction(action, gameState.minRaise)
                                } else {
                                    controller.playerAction(action)
                                }
                            },
                            currentBet = currentBet - (humanPlayer?.currentBet ?: 0),
                            playerChips = humanPlayer?.chips ?: 0
                        )
                    }
                    
                    // 返回菜单
                    if (!isHumanTurn && !showContinueButton) {
                        Text(
                            text = "等待电脑行动...",
                            color = Color(0xFFAAAAAA),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 游戏结束画面
 */
@Composable
private fun GameOverScreen(
    winner: String,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color(0xDD000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "游戏结束",
                color = Color.Yellow,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "🏆 $winner 获胜！",
                color = Color.White,
                fontSize = 28.sp
            )
            
            ActionButton(
                text = "返回主菜单",
                onClick = onRestart,
                backgroundColor = Color(0xFF4a90d9)
            )
        }
    }
}
