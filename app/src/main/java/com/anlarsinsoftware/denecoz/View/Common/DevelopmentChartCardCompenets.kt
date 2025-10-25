package com.anlarsinsoftware.denecoz.View.Common


import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anlarsinsoftware.denecoz.Model.Student.NetScoreHistoryPoint

// ============================================
// UTILITY FUNCTIONS
// ============================================
object ChartUtils {
    fun formatDate(date: java.util.Date): String {
        return try {
            val sdf = java.text.SimpleDateFormat("d MMM", java.util.Locale("tr"))
            sdf.format(date)
        } catch (e: Exception) {
            "N/A"
        }
    }

    fun createSmoothPath(points: List<Offset>, bottomY: Float): Pair<Path, Path> {
        val smoothPath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val current = points[i]
                    val next = points[i + 1]

                    val controlPoint1X = current.x + (next.x - current.x) / 3
                    val controlPoint1Y = current.y
                    val controlPoint2X = current.x + 2 * (next.x - current.x) / 3
                    val controlPoint2Y = next.y

                    cubicTo(
                        controlPoint1X, controlPoint1Y,
                        controlPoint2X, controlPoint2Y,
                        next.x, next.y
                    )
                }
            }
        }

        val fillPath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, bottomY)
                lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val current = points[i]
                    val next = points[i + 1]

                    val controlPoint1X = current.x + (next.x - current.x) / 3
                    val controlPoint1Y = current.y
                    val controlPoint2X = current.x + 2 * (next.x - current.x) / 3
                    val controlPoint2Y = next.y

                    cubicTo(
                        controlPoint1X, controlPoint1Y,
                        controlPoint2X, controlPoint2Y,
                        next.x, next.y
                    )
                }

                lineTo(points.last().x, bottomY)
                close()
            }
        }

        return Pair(smoothPath, fillPath)
    }

    fun calculatePoints(
        values: List<Float>,
        paddingLeft: Float,
        paddingTop: Float,
        chartWidth: Float,
        chartHeight: Float
    ): List<Offset> {
        val minV = (values.minOrNull() ?: 0f) - 5f
        val maxV = (values.maxOrNull() ?: 100f) + 5f
        val valueRange = (maxV - minV).coerceAtLeast(0.0001f)
        val n = values.size
        val stepX = if (n > 1) chartWidth / (n - 1) else chartWidth

        return values.mapIndexed { i, v ->
            val x = paddingLeft + stepX * i
            val y = paddingTop + chartHeight - ((v - minV) / valueRange) * chartHeight
            Offset(x, y)
        }
    }

    fun findNearestPoint(
        tapX: Float,
        chartData: List<NetScoreHistoryPoint>,
        paddingLeft: Float,
        chartWidth: Float,
        threshold: Float
    ): NetScoreHistoryPoint? {
        val stepX = if (chartData.size > 1) {
            chartWidth / (chartData.size - 1)
        } else chartWidth

        val pointsX = chartData.indices.map { i ->
            paddingLeft + stepX * i
        }

        val nearestIndex = pointsX.withIndex()
            .minByOrNull { kotlin.math.abs(it.value - tapX) }
            ?.index

        return if (nearestIndex != null &&
            kotlin.math.abs(pointsX[nearestIndex] - tapX) < threshold
        ) {
            chartData[nearestIndex]
        } else {
            null
        }
    }
}

// ============================================
// CANVAS DRAWING EXTENSIONS
// ============================================
object ChartDrawing {
    fun DrawScope.drawYAxis(
        paddingLeft: Float,
        paddingTop: Float,
        paddingBottom: Float,
        chartHeight: Float,
        minValue: Float,
        maxValue: Float
    ) {
        // Y ekseni çizgisi
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, size.height - paddingBottom),
            strokeWidth = 1.5f
        )

        // Grid ve etiketler
        val ySteps = 5
        val valueRange = maxValue - minValue
        for (i in 0..ySteps) {
            val yValue = minValue + (valueRange * i / ySteps)
            val y = paddingTop + chartHeight - (chartHeight * i / ySteps)

            // Grid çizgisi
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(paddingLeft, y),
                end = Offset(size.width - 20.dp.toPx(), y),
                strokeWidth = 1f
            )

            // Etiket
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    alpha = 150
                }
                drawText(
                    String.format("%.0f", yValue),
                    paddingLeft - 8.dp.toPx(),
                    y + 5.dp.toPx(),
                    paint
                )
            }
        }
    }

    fun DrawScope.drawXAxis(
        chartData: List<NetScoreHistoryPoint>,
        paddingLeft: Float,
        paddingBottom: Float,
        paddingRight: Float,
        chartWidth: Float
    ) {
        // X ekseni çizgisi
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(paddingLeft, size.height - paddingBottom),
            end = Offset(size.width - paddingRight, size.height - paddingBottom),
            strokeWidth = 1.5f
        )

        // Tarih etiketleri
        val stepX = if (chartData.size > 1) chartWidth / (chartData.size - 1) else chartWidth
        chartData.forEachIndexed { index, point ->
            val x = paddingLeft + stepX * index
            val displayDate = ChartUtils.formatDate(point.date)

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    alpha = 150
                }
                drawText(
                    displayDate,
                    x,
                    size.height - paddingBottom + 25.dp.toPx(),
                    paint
                )
            }
        }
    }

    fun DrawScope.drawDataPoints(
        points: List<Offset>,
        selectedIndex: Int?,
        glowColor: Color
    ) {
        points.forEachIndexed { index, p ->
            val isSelected = index == selectedIndex

            // Glow efekti
            drawCircle(
                color = glowColor.copy(alpha = 0.15f),
                radius = if (isSelected) 25f else 18f,
                center = p
            )
            drawCircle(
                color = glowColor.copy(alpha = 0.3f),
                radius = if (isSelected) 15f else 10f,
                center = p
            )
            // Ana nokta
            drawCircle(
                color = Color.White,
                radius = if (isSelected) 7f else 5f,
                center = p
            )
        }
    }

    fun DrawScope.drawTooltip(
        point: Offset,
        tooltipText: String,
        paddingBottom: Float
    ) {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 14.sp.toPx()
            isFakeBoldText = true
        }
        val textWidth = paint.measureText(tooltipText)
        val textHeight = 20.dp.toPx()

        val paddingH = 16.dp.toPx()
        val paddingV = 10.dp.toPx()
        val tooltipWidth = textWidth + paddingH * 2
        val tooltipHeight = textHeight + paddingV * 2
        val arrowHeight = 8.dp.toPx()

        val tooltipX = (point.x - tooltipWidth / 2)
            .coerceIn(10f, size.width - tooltipWidth - 10f)
        val tooltipY = (point.y - tooltipHeight - arrowHeight - 10f)
            .coerceAtLeast(10f)

        // Dış glow
        drawRoundRect(
            color = Color(0xFF6B4EFF).copy(alpha = 0.2f),
            topLeft = Offset(tooltipX - 2f, tooltipY - 2f),
            size = Size(tooltipWidth + 4f, tooltipHeight + 4f),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // Ana tooltip kutusu
        drawRoundRect(
            color = Color(0xFF1C1C3A).copy(alpha = 0.95f),
            topLeft = Offset(tooltipX, tooltipY),
            size = Size(tooltipWidth, tooltipHeight),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Gradient kenarlık
        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF6B4EFF), Color(0xFFA855F7))
            ),
            topLeft = Offset(tooltipX, tooltipY),
            size = Size(tooltipWidth, tooltipHeight),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 2f)
        )

        // Ok işareti
        val arrowPath = Path().apply {
            val arrowCenterX = point.x.coerceIn(
                tooltipX + 10f,
                tooltipX + tooltipWidth - 10f
            )
            moveTo(arrowCenterX - 6f, tooltipY + tooltipHeight)
            lineTo(arrowCenterX, tooltipY + tooltipHeight + arrowHeight)
            lineTo(arrowCenterX + 6f, tooltipY + tooltipHeight)
            close()
        }
        drawPath(
            path = arrowPath,
            color = Color(0xFF1C1C3A).copy(alpha = 0.95f)
        )

        // Tooltip metni
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                tooltipText,
                tooltipX + tooltipWidth / 2,
                tooltipY + tooltipHeight / 2 + 5.dp.toPx(),
                paint.apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        // Bağlantı çizgisi
        drawLine(
            color = Color(0xFF6B4EFF).copy(alpha = 0.3f),
            start = point,
            end = Offset(
                point.x.coerceIn(tooltipX + 10f, tooltipX + tooltipWidth - 10f),
                tooltipY + tooltipHeight + arrowHeight
            ),
            strokeWidth = 1.5f
        )
    }

    fun DrawScope.drawEmptyState(message: String) {
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 16.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = 150
            }
            drawText(
                message,
                size.width / 2,
                size.height / 2,
                paint
            )
        }
    }
}

// ============================================
// MAIN CHART COMPONENT
// ============================================
@Composable
fun DevelopmentChartCard(
    examType: String,
    chartData: List<NetScoreHistoryPoint>,
    onShowReportClicked: () -> Unit
) {
    var selectedPoint by remember { mutableStateOf<NetScoreHistoryPoint?>(null) }

    val gradientBackground = Brush.verticalGradient(
        listOf(
            Color(0xFF1C1C3A),
            Color(0xFF6B4EFF).copy(alpha = 0.3f),
            Color(0xFF0A0A14)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(gradientBackground)
            .padding(2.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF101018).copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Başlık
                Text(
                    "$examType Gelişim Grafiği",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(18.dp))

                // Grafik Alanı
                ChartCanvas(
                    chartData = chartData,
                    selectedPoint = selectedPoint,
                    onPointSelected = { selectedPoint = it }
                )

                Spacer(Modifier.height(20.dp))

                // Buton
                ChartButton(
                    text = "$examType Analiz Raporunu Gör",
                    onClick = onShowReportClicked
                )
            }
        }
    }
}

@Composable
private fun ChartCanvas(
    chartData: List<NetScoreHistoryPoint>,
    selectedPoint: NetScoreHistoryPoint?,
    onPointSelected: (NetScoreHistoryPoint?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .pointerInput(chartData) {
                detectTapGestures(
                    onTap = { offset ->
                        if (chartData.size < 2) return@detectTapGestures

                        val paddingLeft = 50.dp.toPx()
                        val paddingRight = 20.dp.toPx()
                        val chartWidth = size.width - paddingLeft - paddingRight

                        val nearestPoint = ChartUtils.findNearestPoint(
                            tapX = offset.x,
                            chartData = chartData,
                            paddingLeft = paddingLeft,
                            chartWidth = chartWidth,
                            threshold = 30.dp.toPx()
                        )
                        onPointSelected(nearestPoint)
                    },
                    onDoubleTap = {
                        onPointSelected(null)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Boş durum kontrolü
            if (chartData.isEmpty()) {
               // ChartDrawing.drawEmptyState("Veri bulunamadı")
                return@Canvas
            }

            if (chartData.size < 2) {
               // ChartDrawing.drawEmptyState("En az 2 deneme gerekli")
                return@Canvas
            }

            // Padding değerleri
            val paddingLeft = 50.dp.toPx()
            val paddingBottom = 40.dp.toPx()
            val paddingTop = 20.dp.toPx()
            val paddingRight = 20.dp.toPx()

            val chartWidth = size.width - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop - paddingBottom

            // Değer hesaplamaları
            val values = chartData.map { it.net.toFloat() }
            val minV = (values.minOrNull() ?: 0f) - 5f
            val maxV = (values.maxOrNull() ?: 100f) + 5f

            // Noktaları hesapla
            val points = ChartUtils.calculatePoints(
                values = values,
                paddingLeft = paddingLeft,
                paddingTop = paddingTop,
                chartWidth = chartWidth,
                chartHeight = chartHeight
            )

            // Y ekseni çiz
            ChartDrawing.run {
                drawYAxis(
                    paddingLeft = paddingLeft,
                    paddingTop = paddingTop,
                    paddingBottom = paddingBottom,
                    chartHeight = chartHeight,
                    minValue = minV,
                    maxValue = maxV
                )
            }

            // X ekseni çiz
            ChartDrawing.run {
                drawXAxis(
                    chartData = chartData,
                    paddingLeft = paddingLeft,
                    paddingBottom = paddingBottom,
                    paddingRight = paddingRight,
                    chartWidth = chartWidth
                )
            }

            // Eğri ve dolgu çiz
            val (smoothPath, fillPath) = ChartUtils.createSmoothPath(
                points = points,
                bottomY = size.height - paddingBottom
            )

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF6B4EFF).copy(alpha = 0.35f), Color.Transparent)
                )
            )

            drawPath(
                path = smoothPath,
                color = Color(0xFF6B4EFF).copy(alpha = 0.3f),
                style = Stroke(width = 8f)
            )
            drawPath(
                path = smoothPath,
                color = Color(0xFF6B4EFF),
                style = Stroke(width = 3f)
            )

            // Veri noktalarını çiz
            val selectedIndex = selectedPoint?.let { chartData.indexOf(it) }
            ChartDrawing.run {
                drawDataPoints(
                    points = points,
                    selectedIndex = selectedIndex,
                    glowColor = Color(0xFF6B4EFF)
                )
            }

            // Tooltip çiz
            selectedPoint?.let { sp ->
                val index = chartData.indexOf(sp)
                val point = points.getOrNull(index)
                point?.let { p ->
                    val formattedDate = ChartUtils.formatDate(sp.date)
                    ChartDrawing.run {
                        drawTooltip(
                            point = p,
                            tooltipText = "Net: ${sp.net} • $formattedDate",
                            paddingBottom = paddingBottom
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF6B4EFF), Color(0xFFA855F7))
                )
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}