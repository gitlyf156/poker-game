package com.poker.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poker.game.models.PlayerAction

/**
 * 动作按钮
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color(0xFF4a90d9)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) backgroundColor else Color.Gray)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

/**
 * 动作按钮组
 */
@Composable
fun ActionButtonGroup(
    availableActions: List<PlayerAction>,
    onAction: (PlayerAction) -> Unit,
    currentBet: Int,
    playerChips: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 弃牌按钮
        if (PlayerAction.FOLD in availableActions) {
            ActionButton(
                text = "弃牌",
                onClick = { onAction(PlayerAction.FOLD) },
                backgroundColor = Color(0xFFdc3545)
            )
        }
        
        // 过牌按钮
        if (PlayerAction.CHECK in availableActions) {
            ActionButton(
                text = "过牌",
                onClick = { onAction(PlayerAction.CHECK) },
                backgroundColor = Color(0xFF6c757d)
            )
        }
        
        // 跟注按钮
        if (PlayerAction.CALL in availableActions) {
            val callAmount = currentBet
            ActionButton(
                text = "跟注 $$callAmount",
                onClick = { onAction(PlayerAction.CALL) },
                backgroundColor = Color(0xFF28a745)
            )
        }
        
        // 加注按钮
        if (PlayerAction.RAISE in availableActions) {
            val minRaise = currentBet + 100
            ActionButton(
                text = "加注",
                onClick = { onAction(PlayerAction.RAISE) },
                backgroundColor = Color(0xFFffc107),
                modifier = Modifier.background(Color(0xFF212529), RoundedCornerShape(8.dp))
            )
        }
        
        // 全下按钮
        if (PlayerAction.ALL_IN in availableActions && playerChips > 0) {
            ActionButton(
                text = "全下 $$playerChips",
                onClick = { onAction(PlayerAction.ALL_IN) },
                backgroundColor = Color(0xFFdc3545)
            )
        }
    }
}

/**
 * 筹码显示组件
 */
@Composable
fun ChipDisplay(
    amount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0x80000000), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 筹码图标
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFFffd700), shape = RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFFb8860b), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("$", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$amount",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 底池显示
 */
@Composable
fun PotDisplay(
    pot: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xA0000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "底池",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFffd700), shape = RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFb8860b), shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$pot",
                    color = Color.Yellow,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 游戏消息显示
 */
@Composable
fun GameMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    if (message.isNotEmpty()) {
        Box(
            modifier = modifier
                .background(Color(0xE0000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}
