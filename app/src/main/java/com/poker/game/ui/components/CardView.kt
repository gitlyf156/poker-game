package com.poker.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poker.game.models.Card

/**
 * 扑克牌组件
 */
@Composable
fun CardView(
    card: Card?,
    modifier: Modifier = Modifier,
    width: Dp = 70.dp,
    height: Dp = 100.dp,
    isHidden: Boolean = false
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(
                color = if (isHidden || card == null) Color(0xFF1a4785) else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isHidden || card == null) Color(0xFF0d2d52) else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isHidden || card == null) {
            // 牌背 - 蓝色斜纹
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFF1a4785),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        } else {
            // 牌面
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 左上角
                Column {
                    Text(
                        text = card.rank.symbol,
                        color = Color(card.suit.color),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.suit.symbol,
                        color = Color(card.suit.color),
                        fontSize = 14.sp
                    )
                }
                
                // 中间大符号
                Text(
                    text = card.suit.symbol,
                    color = Color(card.suit.color),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                // 右下角（旋转180度）
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.graphicsLayer { rotationZ = 180f }
                    ) {
                        Text(
                            text = card.rank.symbol,
                            color = Color(card.suit.color),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = card.suit.symbol,
                            color = Color(card.suit.color),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 玩家信息面板
 */
@Composable
fun PlayerPanel(
    name: String,
    chips: Int,
    currentBet: Int,
    hand: List<Card>,
    isActive: Boolean,
    isDealer: Boolean,
    isBigBlind: Boolean,
    isSmallBlind: Boolean,
    isFolded: Boolean,
    isAllIn: Boolean,
    isCurrentPlayer: Boolean,
    showCards: Boolean,
    modifier: Modifier = Modifier,
    playerIndex: Int // 0 = 人类玩家
) {
    Box(
        modifier = modifier
            .background(
                color = when {
                    isCurrentPlayer -> Color(0x80FFD700)
                    isFolded -> Color(0x40000000)
                    else -> Color(0x60000000)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 位置标识
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isDealer) Text("D", color = Color.White, fontSize = 12.sp)
                if (isSmallBlind) Text("SB", color = Color.Cyan, fontSize = 12.sp)
                if (isBigBlind) Text("BB", color = Color.Yellow, fontSize = 12.sp)
            }
            
            // 玩家名称
            Text(
                text = name,
                color = Color.White,
                fontWeight = if (playerIndex == 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (playerIndex == 0) 16.sp else 14.sp
            )
            
            // 筹码
            Text(
                text = "$$chips",
                color = Color(0xFF00FF00),
                fontSize = 14.sp
            )
            
            // 当前下注
            if (currentBet > 0) {
                Text(
                    text = "$$currentBet",
                    color = Color.Yellow,
                    fontSize = 12.sp
                )
            }
            
            // All-in 标识
            if (isAllIn) {
                Text(
                    text = "ALL-IN",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            // 手牌
            if (playerIndex == 0 || showCards) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    hand.forEach { card ->
                        CardView(
                            card = card,
                            width = 45.dp,
                            height = 65.dp,
                            isHidden = !showCards && playerIndex != 0
                        )
                    }
                }
            } else if (!isFolded) {
                // 隐藏的手牌
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(2) {
                        CardView(
                            card = null,
                            width = 45.dp,
                            height = 65.dp,
                            isHidden = true
                        )
                    }
                }
            }
        }
    }
}
