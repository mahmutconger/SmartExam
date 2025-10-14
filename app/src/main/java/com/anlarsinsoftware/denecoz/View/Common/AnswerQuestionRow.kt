package com.anlarsinsoftware.denecoz.View.Common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anlarsinsoftware.denecoz.R

@Composable
fun AnswerQuestionRow(
    displayIndex: Int,
    selected: Int?,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7FF)),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) { /* Bu boş kalabilir, asıl tıklama Box'ta */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.question_label, displayIndex), modifier = Modifier.weight(0.35f))
            Spacer(modifier = Modifier.width(8.dp))
            Row(modifier = Modifier.weight(0.65f), horizontalArrangement = Arrangement.SpaceBetween) {
                for (i in 0 until 5) {
                    val letter = ('A' + i)
                    val isSelected = selected == i
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable(enabled = enabled) { onSelect(i) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = letter.toString(), color = if (isSelected) Color.White else Color.Black)
                    }
                }
            }
        }
    }
}