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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poker.game.game.GameController
import com.poker.game.models.GameStage
import com.poker.game.models.PlayerAction
import com.poker.game.models.PlayerStatus
import com.poker.game.models.PlayerType
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
    val isHumanTurn = gameState.getCurrentPlayer()?.type == PlayerType.HUMAN
    
    var showContinueButton by remember { mutableStateOf(false) }
    
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
            // ===== 顶部：返回按钮和回合信息 =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2a503d))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← 返回",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = "第 ${gameState.gameRound} 手",
                    color = Color.Yellow,
                    fontSize = 14.sp
                )
            }
            
            // ===== AI 玩家 - 紧凑布局 =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                gameState.players.filter { it.type == PlayerType.AI }.forEach { player ->
                    CompactPlayerPanel(
                        player = player,
                        isCurrentPlayer = gameState.currentPlayerIndex == gameState.players.indexOf(player),
                        showCards = gameState.currentStage == GameStage.SHOWDOWN
                    )
                }
            }
            
            // ===== 中间：公共牌和底池 =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PotDisplay(pot = gameState.pot)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 公共牌
                if (gameState.communityCards.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        gameState.communityCards.forEach { card ->
                            CardView(card = card, width = 45.dp, height = 65.dp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 阶段
                Text(
                    text = when (gameState.currentStage) {
                        GameStage.PREFLOP -> "翻牌前"
                        GameStage.FLOP -> "翻牌"
                        GameStage.TURN -> "转牌"
                        GameStage.RIVER -> "河牌"
                        GameStage.SHOWDOWN -> "摊牌"
                    },
                    color = Color(0xFFCCCCCC),
                    fontSize = 14.sp
                )
                
                // 消息
                if (gameState.message.isNotEmpty()) {
                    Text(
                        text = gameState.message,
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // 继续按钮
                if (showContinueButton && gameState.winner != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionButton(
                        text = "继续下一局",
                        onClick = {
                            showContinueButton = false
                            controller.startNewHand()
                        },
                        backgroundColor = Color(0xFF28a745)
                    )
                }
            }
            
            // ===== 底部：人类玩家 + 按钮 =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2a503d))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 玩家信息
                humanPlayer?.let { player ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 手牌
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            player.hand.forEach { card ->
                                CardView(card = card, width = 40.dp, height = 56.dp)
                            }
                        }
                        Column {
                            Text(
                                text = "你",
                                color = if (isHumanTurn) Color.Yellow else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${player.chips}",
                                color = Color.Green,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 动作按钮区域 - 保证显示
                if (isHumanTurn && !showContinueButton) {
                    val maxBet = gameState.players.filter { it.status == PlayerStatus.ACTIVE || it.status == PlayerStatus.ALL_IN }.maxOfOrNull { it.currentBet } ?: 0
                    val toCall = maxBet - (humanPlayer?.currentBet ?: 0)
                    
                    ActionButtonGroup(
                        availableActions = controller.getAvailableActions(),
                        onAction = { action ->
                            if (action == PlayerAction.RAISE) {
                                controller.playerAction(action, gameState.minRaise)
                            } else {
                                controller.playerAction(action)
                            }
                        },
                        currentBet = toCall,
                        playerChips = humanPlayer?.chips ?: 0
                    )
                } else if (!showContinueButton) {
                    Text(
                        text = if (gameState.getCurrentPlayer()?.type == PlayerType.AI) "等待电脑行动..." else "游戏进行中",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * 紧凑的玩家面板
 */
@Composable
private fun CompactPlayerPanel(
    player: com.poker.game.models.Player,
    isCurrentPlayer: Boolean,
    showCards: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                if (isCurrentPlayer) Color(0x40FFD700) else Color(0x60000000),
                RoundedCornerShape(4.dp)
            )
            .padding(4.dp)
    ) {
        Row {
            if (player.isDealer) Text("D ", color = Color.White, fontSize = 8.sp)
            if (player.isSmallBlind) Text("SB ", color = Color.Cyan, fontSize = 8.sp)
            if (player.isBigBlind) Text("BB ", color = Color.Yellow, fontSize = 8.sp)
        }
        Text(
            text = player.name,
            color = Color.White,
            fontSize = 10.sp
        )
        Text(
            text = "$${player.chips}",
            color = Color.Green,
            fontSize = 9.sp
        )
        if (player.currentBet > 0) {
            Text(
                text = "$${player.currentBet}",
                color = Color.Yellow,
                fontSize = 8.sp
            )
        }
        // 隐藏的手牌
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .size(24.dp, 34.dp)
                        .background(Color(0xFF1a4785), RoundedCornerShape(2.dp))
                )
            }
        }
        if (player.status == com.poker.game.models.PlayerStatus.FOLDED) {
            Text("弃牌", color = Color.Red, fontSize = 8.sp)
        }
    }
}

@Composable
private fun Modifier.size(width: Int, height: Int): Modifier {
    return this.then(Modifier.requiredSize(width.dp, height.dp))
}

private fun Modifier.requiredSize(width: Int, height: Int): Modifier {
    return this.then(androidx.compose.foundation.layout.size(width.dp, height.dp))
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
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "🏆 $winner 获胜！",
                color = Color.White,
                fontSize = 22.sp
            )
            ActionButton(
                text = "返回主菜单",
                onClick = onRestart,
                backgroundColor = Color(0xFF4a90d9)
            )
        }
    }
}
