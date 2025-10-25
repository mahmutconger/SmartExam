package com.anlarsinsoftware.denecoz.View.Common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AnimatedBottomBar(
    items: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Colors
    val capsuleGradient = Brush.horizontalGradient(
        listOf(Color(0xFF6B4EFF), Color(0xFFA855F7))
    )
    val unselectedIconColor = Color.White.copy(alpha = 0.85f)
    val glassBackground = Color(0xFF1A1A1A).copy(alpha = 0.35f)


    // Animated position for sliding capsule
    val indicatorOffset = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        indicatorOffset.animateTo(
            selectedIndex.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Glass background container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(glassBackground)
                .blur(20.dp)
        ) {
            // Stronger gradient overlay for glass depth
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                // Scale animation for bounce effect
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "scale"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                onItemSelected(index)
                            }
                        }
                ) {
                    if (isSelected) {
                        // Animated capsule background
                        Box(
                            modifier = Modifier
                                .height(50.dp)
                                .widthIn(min = 120.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(capsuleGradient)
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = item.second,
                                    contentDescription = item.first,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = item.first,
//                                    color = Color.White,
//                                    fontSize = 15.sp,
//                                    fontWeight = FontWeight.SemiBold
//                                )
                            }
                        }
                    } else {
                        // Unselected icon with glow effect
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(50.dp)
                        ) {
                            // Subtle glow
                            Icon(
                                imageVector = item.second,
                                contentDescription = item.first,
                                tint = unselectedIconColor.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .blur(4.dp)
                            )
                            Icon(
                                imageVector = item.second,
                                contentDescription = item.first,
                                tint = unselectedIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}