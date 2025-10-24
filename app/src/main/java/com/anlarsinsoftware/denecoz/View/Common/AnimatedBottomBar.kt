package com.anlarsinsoftware.denecoz.View.Common


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AnimatedBottomBar(
    items: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activeColor = Brush.horizontalGradient(
        listOf(Color(0xFF4F5DFF), Color(0xFF9B8CFF))
    )
    val inactiveColor = Color.Gray.copy(alpha = 0.6f)

    // animasyonlu pozisyon
    val indicatorOffset = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        indicatorOffset.animateTo(
            selectedIndex.toFloat(),
            animationSpec = tween(durationMillis = 400)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val curveRadius = 50f
            val curveWidth = width / items.size
            val selectedCenterX = curveWidth * (indicatorOffset.value + 0.5f)

            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(selectedCenterX - curveRadius * 1.5f, 0f)
                quadraticBezierTo(
                    selectedCenterX,
                    -curveRadius,
                    selectedCenterX + curveRadius * 1.5f,
                    0f
                )
                lineTo(width, 0f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            clipPath(path) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4F5DFF), Color(0xFF9B8CFF))
                    ),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(40f, 40f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val animatedY by animateDpAsState(
                    targetValue = if (isSelected) (-10).dp else 0.dp,
                    animationSpec = tween(400)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(y = animatedY)
                        .clickable {
                            coroutineScope.launch { onItemSelected(index) }
                        }
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(brush = activeColor)
                        )
                    }
                    Icon(
                        imageVector = item.second,
                        contentDescription = item.first,
                        tint = if (isSelected) Color.White else inactiveColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
