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
    
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF35654d))
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF2a503d)).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Texas Poker", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "Round ${gameState.gameRound}", color = Color.White, fontSize = 14.sp)
        }
        
        // Center
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI Players
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                gameState.players.filter { it.type == PlayerType.AI }.take(3).forEach { player ->
                    CompactPlayerPanel(
                        player = player,
                        isCurrentPlayer = gameState.currentPlayerIndex == gameState.players.indexOf(player)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PotDisplay(pot = gameState.pot)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Community Cards
            if (gameState.communityCards.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    gameState.communityCards.forEach { card ->
                        CardView(card = card, width = 40.dp, height = 56.dp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (gameState.currentStage) {
                    GameStage.PREFLOP -> "Pre-flop"
                    GameStage.FLOP -> "Flop"
                    GameStage.TURN -> "Turn"
                    GameStage.RIVER -> "River"
                    GameStage.SHOWDOWN -> "Showdown"
                },
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp
            )
            
            if (gameState.message.isNotEmpty()) {
                Text(
                    text = gameState.message,
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            if (showContinueButton && gameState.winner != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(
                    text = "Next Hand",
                    onClick = {
                        showContinueButton = false
                        controller.startNewHand()
                    },
                    backgroundColor = Color(0xFF28a745)
                )
            }
        }
        
        // Bottom - Human Player + Buttons
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF2a503d)).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            humanPlayer?.let { player ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        player.hand.forEach { card ->
                            CardView(card = card, width = 45.dp, height = 65.dp)
                        }
                    }
                    Column {
                        Text(
                            text = "You",
                            color = if (isHumanTurn) Color.Yellow else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "$${player.chips}", color = Color.Green, fontSize = 16.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
                Text(text = "Waiting...", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun CompactPlayerPanel(
    player: com.poker.game.models.Player,
    isCurrentPlayer: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(
            if (isCurrentPlayer) Color(0x40FFD700) else Color(0x60000000),
            RoundedCornerShape(4.dp)
        ).padding(4.dp)
    ) {
        Row {
            if (player.isDealer) Text("D ", color = Color.White, fontSize = 8.sp)
            if (player.isSmallBlind) Text("SB ", color = Color.Cyan, fontSize = 8.sp)
            if (player.isBigBlind) Text("BB ", color = Color.Yellow, fontSize = 8.sp)
        }
        Text(text = player.name, color = Color.White, fontSize = 10.sp)
        Text(text = "$${player.chips}", color = Color.Green, fontSize = 9.sp)
        if (player.currentBet > 0) {
            Text(text = "$${player.currentBet}", color = Color.Yellow, fontSize = 8.sp)
        }
        // Hidden cards - use Box with preferred size
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier.size(20.dp, 28.dp).background(Color(0xFF1a4785), RoundedCornerShape(2.dp))
                )
            }
        }
        if (player.status == PlayerStatus.FOLDED) {
            Text("Fold", color = Color.Red, fontSize = 8.sp)
        }
    }
}
