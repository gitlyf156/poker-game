package com.poker.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 主菜单屏幕
 */
@Composable
fun MenuScreen(
    onStartGame: (Int) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlayers by remember { mutableIntStateOf(4) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF35654d))
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // 标题
            Text(
                text = "♠️ 德州扑克 ♥️",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // 副标题
            Text(
                text = "TEXAS HOLD'EM POKER",
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 人数选择
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "选择玩家人数",
                    color = Color.White,
                    fontSize = 18.sp
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    (2..9).forEach { count ->
                        PlayerCountButton(
                            count = count,
                            isSelected = count == selectedPlayers,
                            onClick = { selectedPlayers = count }
                        )
                    }
                }
                
                Text(
                    text = "当前: $selectedPlayers 人局",
                    color = Color.Yellow,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 开始按钮 - 大一点，更明显
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF4a90d9))
                    .clickable { onStartGame(selectedPlayers) }
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶️ 开始游戏",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // 退出按钮
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Transparent)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .clickable { onExit() }
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "退出",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 游戏信息
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💰 初始筹码: \$2000",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
                Text(
                    text = "🎯 盲注: \$50/\$100",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
                Text(
                    text = "👤 你 vs 电脑对手",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * 人数选择按钮
 */
@Composable
private fun PlayerCountButton(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFffd700) else Color(0xFF2a503d))
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFFff8c00) else Color(0xFF1a3a2a),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count",
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
